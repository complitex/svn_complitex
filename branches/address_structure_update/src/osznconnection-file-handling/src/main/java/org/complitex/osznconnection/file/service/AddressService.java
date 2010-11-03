/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
import org.complitex.osznconnection.information.strategy.street.StreetStrategy;

/**
 * Класс разрешает адрес.
 * @author Artem
 */
@Stateless
public class AddressService extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB(beanName = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    /**
     * Разрешить переход "ОСЗН адрес -> локальная адресная база"
     * Алгоритм:
     * Сначала пытаемся поискать город в таблице коррекций по названию города, пришедшего из ОСЗН и id ОСЗН.
     * Если не успешно, то пытаемся поискать по локальной адресной базе.
     * Если успешно, то записать коррекцию в таблицу коррекций.
     * Если город в итоге нашли, то проставляем его в internalCityId, иначе проставляем статус RequestStatus.CITY_UNRESOLVED_LOCALLY
     * и выходим, т.к. без города искать далее не имеет смысла.
     *
     * Это алгоритм применяется и к поиску домов и с незначительными поправками к поиску улиц.
     *
     * @param payment
     */
    private void resolveLocalAddress(Payment payment) {
        //осзн id
        long organizationId = payment.getOrganizationId();

        //Связывание города
        Long cityId;
        String city = (String) payment.getField(PaymentDBF.N_NAME);

        Correction cityCorrection = addressCorrectionBean.findCorrectionCity(city, organizationId);

        if (cityCorrection != null) {
            cityId = cityCorrection.getObjectId();
        } else {
            cityId = addressCorrectionBean.findInternalCity(city);

            if (cityId != null) {
                cityCorrection = addressCorrectionBean.insertCorrectionCity(city, cityId, organizationId,
                        OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }

        if (cityId != null) {
            payment.setInternalCityId(cityId);
        } else {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
            return;
        }

        //Связывание улицы
        Long streetId;
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);

        Correction streetCorrection = addressCorrectionBean.findCorrectionStreet(cityCorrection, street);

        if (streetCorrection != null) {
            streetId = streetCorrection.getObjectId();
        } else {
            streetId = addressCorrectionBean.findInternalStreet(street, cityId);

            if (streetId != null) {
                streetCorrection = addressCorrectionBean.insertCorrectionStreet(cityCorrection, street, streetId);
            }
        }

        //Если нашли улицу, то достаем из базы весь объект, берем тип улицы и проставляем в internalStreetTypeId
        if (streetId != null) {
            DomainObject streetObject = strategyFactory.getStrategy("street").findById(streetId);

            payment.setInternalStreetId(streetId);
            payment.setInternalStreetTypeId(StreetStrategy.getStreetType(streetObject));
            payment.setInternalCityId(streetObject.getParentId());
        } else {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
            return;
        }

        //Связывание дома
        Long buildingId;
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);

        final Correction buildingCorrection = addressCorrectionBean.findCorrectionBuilding(streetCorrection,
                buildingNumber, buildingCorp);

        if (buildingCorrection != null) {
            buildingId = buildingCorrection.getObjectId();
        } else {
            buildingId = addressCorrectionBean.findInternalBuilding(buildingNumber, buildingCorp, streetId, cityId);
            if (buildingId != null) {
                addressCorrectionBean.insertCorrectionBuilding(streetCorrection, buildingNumber, buildingCorp, buildingId);
            }
        }

        if (buildingId != null) {
            payment.setInternalBuildingId(buildingId);
            DomainObject buildingObject = strategyFactory.getStrategy("building").findById(buildingId);
            payment.setInternalCityId(buildingObject.getParentId());

            for (Attribute attribute : buildingObject.getAttributes()) {
                if (attribute.getAttributeTypeId().equals(BuildingStrategy.STREET)) {
                    payment.setInternalStreetId(attribute.getValueId());
                    break;
                }
            }
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
        } else {
            payment.setStatus(RequestStatus.BUILDING_UNRESOLVED_LOCALLY);
        }
    }

    /**
     * разрешить переход "локальная адресная база -> адрес центра начислений"
     * Алгоритм:
     * Пытаемся найти коррекцию(строковое полное название и код) по id города в локальной адресной базе и текущему ЦН.
     * Если не нашли, то проставляем статус RequestStatus.CITY_UNRESOLVED и выходим, т.к. без города продолжать не имеет смысла.
     * Если нашли, то проставляем в payment информацию из коррекции(полное название, код) посредством метода
     * adapter.prepareCity() в адаптере для взаимодействия с ЦН.
     * См. org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter - адаптер по умолчанию.
     * Квартиры не ищем, а проставляем напрямую, обрезая пробелы.
     * Алгоритм аналогичен для поиска остальных составляющих адреса.
     * @param payment
     */
    @Transactional
    public void resolveOutgoingAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
        Correction cityData = addressCorrectionBean.findOutgoingCity(calculationCenterId, payment.getInternalCityId());
        if (cityData == null) {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }
        adapter.prepareCity(payment, cityData.getCorrection(), cityData.getCode());

        //поиск района
        Correction districtData = addressCorrectionBean.findOutgoingDistrict(calculationCenterId, payment.getOrganizationId());
        if (districtData == null) {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
            return;
        }
        adapter.prepareDistrict(payment, districtData.getCorrection(), districtData.getCode());

        //поиск типа улицы
        if (payment.getInternalStreetTypeId() != null) {
            Correction streetTypeData = addressCorrectionBean.findOutgoingStreetType(calculationCenterId,
                    payment.getInternalStreetTypeId());
            if (streetTypeData == null) {
                payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
                return;
            }
            adapter.prepareStreetType(payment, streetTypeData.getCorrection(), streetTypeData.getCode());
        }

        //поиск улицы
        Correction streetData = addressCorrectionBean.findOutgoingStreet(calculationCenterId,
                payment.getInternalStreetId());
        if (streetData == null) {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED);
            return;
        }
        adapter.prepareStreet(payment, streetData.getCorrection(), streetData.getCode());

        //поиск дома
        BuildingCorrection buildingData = addressCorrectionBean.findOutgoingBuilding(calculationCenterId,
                payment.getInternalBuildingId());
        if (buildingData == null) {
            payment.setStatus(RequestStatus.BUILDING_UNRESOLVED);
            return;
        }
        adapter.prepareBuilding(payment, buildingData.getCorrection(), buildingData.getCorrectionCorp(), buildingData.getCode());

//        Correction apartmentData = addressCorrectionBean.findOutgoingApartment(calculationCenterId,
//                payment.getInternalApartmentId());
//        if (apartmentData == null) {
//            payment.setStatus(Status.APARTMENT_UNRESOLVED);
//            return;
//        }
        //квартиры не ищем, а проставляем напрямую, обрезая пробелы.
        adapter.prepareApartment(payment, null, null);
        payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
    }

    /**
     * Разрешен ли адрес
     * @param payment
     * @return
     */
    public boolean isAddressResolved(Payment payment) {
        /*
         * Адрес считаем разрешенным, если статус payment записи не входит в список статусов, указывающих на то что адрес не разрешен локально,
         * не входит в список статусов, указывающих на то что адрес не разрешен в ЦН, и не равен RequestStatus.ADDRESS_CORRECTED,
         * который указывает на то, что адрес откорректировали в UI.
         * См. RequestStatus
         */
        return !payment.getStatus().isLocalAddressCorrected() && !payment.getStatus().isOutgoingAddressCorrected()
                && (payment.getStatus() != RequestStatus.ADDRESS_CORRECTED);
    }

    /**
     * разрешить адрес по схеме "ОСЗН адрес -> локальная адресная база -> адрес центра начислений"
     * @param payment
     * @param calculationCenterId
     * @param adapter
     */
    @Transactional
    public void resolveAddress(Payment payment, long calculationCenterId, ICalculationCenterAdapter adapter) {
//        if (!isAddressResolved(payment)) {
        //разрешить адрес локально
        resolveLocalAddress(payment);
        //если адрес локально разрешен, разрешить адрес для ЦН.
        if (!payment.getStatus().isLocalAddressCorrected()) {
            resolveOutgoingAddress(payment, calculationCenterId, adapter);
        }
//        }
    }

    /**
     * Корректирование адреса из UI.
     * Алгоритм:
     * Если у payment записи id города NULL и откорректированный город не NULL, то
     * вставить коррекцию для города в таблицу коррекций городов, коррекировать payment(PaymentBean.correctCity())
     * и benefit записи соответствующие данному payment(BenefitBean.addressCorrected()).
     *
     * Алгоритм аналогичен для других составляющих адреса.
     *
     * @param payment
     * @param cityId Откорректированный город
     * @param streetId Откорректированная улица
     * @param streetTypeId Откорректированный тип улицы
     * @param buildingId Откорректированный дом
     */
    @Transactional
    public void correctLocalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId) {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);

        boolean corrected = false;

        if (payment.getInternalCityId() == null) {
            if (cityId != null) {
                addressCorrectionBean.insertCorrectionCity(city, cityId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
                paymentBean.correctCity(requestFileId, city, cityId);

                corrected = true;
            }
        } else if (payment.getInternalStreetId() == null) {
            if (streetId != null && cityId != null) {
                Correction cityCorrection = addressCorrectionBean.findCorrectionCity(city, organizationId);

                addressCorrectionBean.insertCorrectionStreet(cityCorrection, street, streetId);

                paymentBean.correctStreet(requestFileId, cityId, street, streetId, streetTypeId);

                corrected = true;
            }

        } else if (payment.getInternalBuildingId() == null) {
            if (streetId != null && cityId != null && buildingId != null) {
                Correction cityCorrection = addressCorrectionBean.findCorrectionCity(city, organizationId);
                Correction streetCorrection = addressCorrectionBean.findCorrectionStreet(cityCorrection, street); //todo add one query

                addressCorrectionBean.insertCorrectionBuilding(streetCorrection, buildingNumber, buildingCorp, buildingId);
                paymentBean.correctBuilding(requestFileId, cityId, streetId, buildingNumber, buildingCorp, buildingId);

                corrected = true;
            }
        }

        if (corrected) {
            benefitBean.addressCorrected(payment.getId());
        }
    }
}
