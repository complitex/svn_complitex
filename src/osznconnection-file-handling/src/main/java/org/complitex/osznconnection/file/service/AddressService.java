/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;

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
        Long cityId = null;
        Long streetId = null;
        Long buildingId = null;

        //Связывание города
        String city = (String) payment.getField(PaymentDBF.N_NAME);

        ObjectCorrection cityCorrection = addressCorrectionBean.findCorrectionCity(city, organizationId);

        if (cityCorrection != null){
            cityId = cityCorrection.getInternalObjectId();
        }

        if (cityId == null) {
            cityId = addressCorrectionBean.findInternalCity(city);

            if (cityId != null) {
                addressCorrectionBean.insertCorrectionCity(city, cityId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }

        if (cityId != null) {
            payment.setInternalCityId(cityId);
        } else {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);
            return;
        }

        //Связывание улицы
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);

        ObjectCorrection streetCorrection = addressCorrectionBean.findCorrectionStreet(cityId, street, organizationId);

        if (streetCorrection != null){
            streetId = streetCorrection.getInternalObjectId();

            //коррекция города
            if (streetCorrection.getCorrectionParentId() != null){
                cityId = streetCorrection.getCorrectionParentId();
                payment.setInternalCityId(cityId);
            }
        }

        if (streetId == null) {
            streetId = addressCorrectionBean.findInternalStreet(street, cityId, null);

            if (streetId != null) {
                addressCorrectionBean.insertCorrectionStreet(street, streetId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }

        //Если нашли улицу, то достаем из базы весь объект, берем тип улицы и проставляем в internalStreetTypeId
        if (streetId != null) {
            payment.setInternalStreetId(streetId);
            Strategy streetStrategy = strategyFactory.getStrategy("street");
            DomainObject streetObject = streetStrategy.findById(streetId);
            payment.setInternalStreetTypeId(streetObject.getEntityTypeId());
        } else {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED_LOCALLY);
            return;
        }

        //Связывание дома
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);

        ObjectCorrection buildingCorrection = addressCorrectionBean.findCorrectionBuilding(cityId, streetId,
                buildingNumber, buildingCorp, organizationId);

        if (buildingCorrection != null){
            buildingId = buildingCorrection.getInternalObjectId();

            //коррекция улицы и города
            if (buildingCorrection.getCorrectionParentId() != null){
                streetId = buildingCorrection.getCorrectionParentId();

                DomainObject streetObject = strategyFactory.getStrategy("street").findById(streetId);

                cityId = streetObject.getParentId();

                payment.setInternalStreetTypeId(streetObject.getEntityTypeId());
                payment.setInternalStreetId(streetId);
                payment.setInternalCityId(cityId);
            }
        }

        if (buildingId == null) {
            buildingId = addressCorrectionBean.findInternalBuilding(buildingNumber, buildingCorp, streetId, cityId);
            if (buildingId != null) {
                addressCorrectionBean.insertCorrectionBuilding(buildingNumber, buildingCorp, buildingId, organizationId,
                        OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            }
        }

        if (buildingId != null) {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
            payment.setInternalBuildingId(buildingId);
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
        ObjectCorrection cityData = addressCorrectionBean.findOutgoingCity(calculationCenterId, payment.getInternalCityId());
        if (cityData == null) {
            payment.setStatus(RequestStatus.CITY_UNRESOLVED);
            return;
        }
        adapter.prepareCity(payment, cityData.getCorrection(), cityData.getCode());

        //поиск района
        ObjectCorrection districtData = addressCorrectionBean.findOutgoingDistrict(calculationCenterId, payment.getOrganizationId());
        if (districtData == null) {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
            return;
        }
        adapter.prepareDistrict(payment, districtData.getCorrection(), districtData.getCode());

        //поиск типа улицы
        if (payment.getInternalStreetTypeId() != null) {
            EntityTypeCorrection streetTypeData = addressCorrectionBean.findOutgoingStreetType(calculationCenterId,
                    payment.getInternalStreetTypeId());
            if (streetTypeData == null) {
                payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
                return;
            }
            adapter.prepareStreetType(payment, streetTypeData.getCorrection(), streetTypeData.getCode());
        }

        //поиск улицы
        ObjectCorrection streetData = addressCorrectionBean.findOutgoingStreet(calculationCenterId,
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

//        ObjectCorrection apartmentData = addressCorrectionBean.findOutgoingApartment(calculationCenterId,
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
     * @param apartmentId Откорректированная квартира
     */
    @Transactional
    public void correctLocalAddress(Payment payment, Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId) {
        long organizationId = payment.getOrganizationId();
        long requestFileId = payment.getRequestFileId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String buildingNumber = (String) payment.getField(PaymentDBF.BLD_NUM);
        String buildingCorp = (String) payment.getField(PaymentDBF.CORP_NUM);
//        String apartment = (String) payment.getField(PaymentDBF.FLAT);

        boolean corrected = false;
        if ((payment.getInternalCityId() == null) && (cityId != null)) {
            addressCorrectionBean.insertCorrectionCity(city, cityId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            paymentBean.correctCity(requestFileId, city, cityId);
            corrected = true;
        } else if ((payment.getInternalStreetId() == null) && (streetId != null)) {
            addressCorrectionBean.insertCorrectionStreet(street, streetId, organizationId, OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            paymentBean.correctStreet(requestFileId, cityId, street, streetId, streetTypeId);
            corrected = true;
        } else if ((payment.getInternalBuildingId() == null) && (buildingId != null)) {
            addressCorrectionBean.insertCorrectionBuilding(buildingNumber, buildingCorp, buildingId, organizationId,
                    OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
            paymentBean.correctBuilding(requestFileId, cityId, streetId, buildingNumber, buildingCorp, buildingId);
            corrected = true;
        }
//        else if ((payment.getInternalApartmentId() == null) && (apartmentId != null)) {
//            addressCorrectionBean.insertCorrectionApartment(apartment, apartmentId, organizationId);
//            paymentBean.correctApartment(requestFileId, cityId, streetId, buildingId, apartment, apartmentId);
//            corrected = true;
//        }
        if (corrected) {
            benefitBean.addressCorrected(payment.getId());
        }
    }
}
