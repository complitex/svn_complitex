package org.complitex.correction.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.apartment.ApartmentStrategy;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.*;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.LocaleBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Sknar
 */
@Stateless(name = "AddressService")
public class AddressService extends AbstractBean {
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private DistrictStrategy districtStrategy;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private BuildingStrategy buildingStrategy;

    @EJB
    private ApartmentStrategy apartmentStrategy;

    /**
     * Разрешить переход "ОСЗН адрес -> локальная адресная база"
     * Алгоритм:
     * Сначала пытаемся поискать город в таблице коррекций по названию города, пришедшего из ОСЗН и id ОСЗН.
     * Если не успешно, то пытаемся поискать по локальной адресной базе.
     * Если успешно, то записать коррекцию в таблицу коррекций.
     * Если город в итоге нашли, то проставляем его в internalCityId, иначе проставляем статус AddressLinkStatus.CITY_UNRESOLVED
     * и выходим, т.к. без города искать далее не имеет смысла.
     * Улицы ищем только в локальной адресной базе. Причем сначала ищем только по названию улицы. Если нашли ровно одну, т.е. существует только
     * один тип улицы для улицы с таким названием, то поиск успешен, по id улицы узнаем тип улицы, по id типа улицы находим(или создаем
     * если ничего не нашли) коррекцию для типа улицы, находим (или создаем) коррекцию для улицы. Далее обрабатываем дом по схеме аналогичной
     * схеме обработки города.
     * Если по названию улицы ничего не нашли, то проставляем статус AddressLinkStatus.STREET_UNRESOLVED и выходим.
     * Если же нашли более одной, то пытаемся поискать по названию улицы, номеру дома и корпуса(если есть). Если нашли ровно одну улицу, то
     * проставляем в payment id улицы и дома и выходим не создаваю никаких коррекций. Если не нашли ничего или более одной, то проставляем
     * статус AddressLinkStatus.STREET_UNRESOLVED и выходим.
     * Замечание: статус AddressLinkStatus.STREET_UNRESOLVED не позволяет корректировать улицы для payment, см.
     *      AddressLinkStatus.isAddressCorrectableForPayment().
     *
     * Это алгоритм применяется и к поиску домов и с незначительными поправками к поиску улиц.
     */
    public void resolveAddress(AddressLinkData request){
        Long organizationId = request.getOrganizationId();
        Long userOrganizationId = request.getUserOrganizationId();

        //Связывание города
        List<CityCorrection> cityCorrections = addressCorrectionBean.getCityCorrections(null, request.getCity(),
                organizationId, userOrganizationId);

        if (cityCorrections.size() == 1) {
            CityCorrection cityCorrection = cityCorrections.get(0);
            request.setCityId(cityCorrection.getObjectId());
        } else if (cityCorrections.size() > 1) {
            request.setStatus(AddressLinkStatus.MORE_ONE_CITY_CORRECTION);

            return;
        } else {
            List<Long> cityIds = addressCorrectionBean.getCityObjectIds(request.getCity());

            if (cityIds.size() == 1) {
                request.setCityId(cityIds.get(0));
            } else if (cityIds.size() > 1) {
                request.setStatus(AddressLinkStatus.MORE_ONE_CITY);

                return;
            } else {
                request.setStatus(AddressLinkStatus.CITY_UNRESOLVED);

                return;
            }
        }

        //Связывание типа улицы
        if(request.getStreetType() != null){
            List<StreetTypeCorrection> streetTypeCorrections = addressCorrectionBean.getStreetTypeCorrections(null,
                    request.getStreetType(), organizationId, userOrganizationId);

            if (streetTypeCorrections.size() == 1) {
                request.setStreetTypeId(streetTypeCorrections.get(0).getObjectId());
            } else if (streetTypeCorrections.size() > 1) {
                request.setStatus(AddressLinkStatus.MORE_ONE_STREET_TYPE_CORRECTION);

                return;
            } else {
                List<Long> streetTypeIds = addressCorrectionBean.getStreetTypeObjectIds(request.getStreetType());

                if (streetTypeIds.size() == 1) {
                    request.setStreetTypeId(streetTypeIds.get(0));
                } else if (streetTypeIds.size() > 1) {
                    request.setStatus(AddressLinkStatus.MORE_ONE_STREET_TYPE);

                    return;
                } else {
                    request.setStatus(AddressLinkStatus.STREET_TYPE_UNRESOLVED);

                    return;
                }
            }
        }

        //Связывание улицы
        List<StreetCorrection> streetCorrections = addressCorrectionBean.getStreetCorrections(request.getCityId(),
                request.getStreetTypeId(), request.getStreetCode(), null,  request.getStreet(),
                organizationId, userOrganizationId);

        if (streetCorrections.size() == 1){
            StreetCorrection streetCorrection = streetCorrections.get(0);

            request.setCityId(streetCorrection.getCityObjectId());
            request.setStreetTypeId(streetCorrection.getStreetTypeObjectId());
            request.setStreetId(streetCorrection.getObjectId());
        }else if (streetCorrections.size() > 1) {
            //сформируем множество названий
            Set<String> streetNames = Sets.newHashSet();

            for (StreetCorrection sc : streetCorrections) {
                String streetName = streetStrategy.getName(sc.getObjectId());

                if (!Strings.isEmpty(streetName)) {
                    streetNames.add(streetName);
                }
            }

            if (streetNames.size() == 1) { //нашли внутренее название улицы
                String streetName = Lists.newArrayList(streetNames).get(0);

                //находим ids улиц по внутреннему названию
                List<Long> streetIds = streetStrategy.getStreetObjectIds(request.getCityId(),
                        request.getStreetTypeId(), streetName);

                if (streetIds.size() == 1) { //нашли ровно одну улицу
                    Long streetObjectId = streetIds.get(0);
                    request.setStreetId(streetObjectId);

                    DomainObject streetObject = streetStrategy.findById(streetObjectId, true);
                    request.setStreetTypeId(StreetStrategy.getStreetType(streetObject));

                    //перейти к обработке дома
                } else if (streetIds.size() > 1) { // нашли больше одной улицы
                    //пытаемся найти по району
                    streetIds = streetStrategy.getStreetObjectIdsByDistrict(request.getCityId(),
                            request.getStreet(), organizationId);

                    if (streetIds.size() == 1) { //нашли ровно одну улицу по району
                        Long streetObjectId = streetIds.get(0);
                        request.setStreetId(streetObjectId);


                        DomainObject streetObject = streetStrategy.findById(streetObjectId, true);
                        request.setStreetTypeId(StreetStrategy.getStreetType(streetObject));

                        //перейти к обработке дома
                    } else {
                        // пытаемся искать дополнительно по номеру и корпусу дома
                        streetIds = streetStrategy.getStreetObjectIdsByBuilding(request.getCityId(), streetName,
                                request.getBuildingNumber(), request.getBuildingCorp());

                        if (streetIds.size() == 1) { //нашли ровно одну улицу с заданным номером и корпусом дома
                            Long streetObjectId = streetIds.get(0);
                            request.setStreetId(streetObjectId);

                            DomainObject streetObject = streetStrategy.findById(streetObjectId, true);
                            request.setStreetTypeId(StreetStrategy.getStreetType(streetObject));

                            //проставить дом для payment и выйти
                            List<Long> buildingIds = buildingStrategy.getBuildingObjectIds(request.getCityId(),
                                    streetObjectId,request.getBuildingNumber(),request.getBuildingCorp());

                            if (buildingIds.size() == 1) {
                                request.setBuildingId(buildingIds.get(0));
                            } else {
                                throw new IllegalStateException("Building id was not found.");
                            }
                            request.setStatus(AddressLinkStatus.ADDRESS_LINKED);

                            return;
                        } else { // по доп. информации, состоящей из номера и корпуса дома, не смогли однозначно определить улицу
                            request.setStatus(AddressLinkStatus.STREET_AND_BUILDING_UNRESOLVED);
                            return;
                        }
                    }
                } else {
                    throw new IllegalStateException("Street name `" + streetName + "` was not found.");
                }
            } else {
                throw new IllegalStateException("Street `" + request.getStreet() +
                        "` is mapped to more one internal street objects: " + streetNames);
            }
        } else { // в коррекциях не нашли ни одного соответствия на внутренние объекты улиц
            // ищем по внутреннему справочнику улиц
            List<Long> streetIds = streetStrategy.getStreetObjectIds(request.getCityId(),
                    request.getStreetTypeId(), request.getStreet());

            if (streetIds.size() == 1) { // нашли ровно одну улицу
                Long streetId = streetIds.get(0);
                request.setStreetId(streetId);

                DomainObject streetObject = streetStrategy.findById(streetId, true);
                request.setStreetTypeId(StreetStrategy.getStreetType(streetObject));

                // перейти к обработке дома
            } else if (streetIds.size() > 1) { // нашли более одной улицы
                //пытаемся найти по району
                streetIds = streetStrategy.getStreetObjectIdsByDistrict(request.getCityId(), request.getStreet(), organizationId);

                if (streetIds.size() == 1) { //нашли ровно одну улицу по району
                    Long streetId = streetIds.get(0);
                    request.setStreetId(streetId);

                    DomainObject streetObject = streetStrategy.findById(streetId, true);
                    request.setStreetTypeId(StreetStrategy.getStreetType(streetObject));
                    // перейти к обработке дома
                } else {
                    // пытаемся искать дополнительно по номеру и корпусу дома
                    streetIds = streetStrategy.getStreetObjectIdsByBuilding(request.getCityId(), request.getStreet(),
                            request.getBuildingNumber(), request.getBuildingCorp());

                    if (streetIds.size() == 1) {
                        Long streetId = streetIds.get(0);

                        //проставить дом для payment и выйти
                        List<Long> buildingIds = buildingStrategy.getBuildingObjectIds(request.getCityId(), streetId,
                                request.getBuildingNumber(), request.getBuildingCorp());

                        if (buildingIds.size() == 1) {
                            request.setBuildingId(buildingIds.get(0));

                            request.setStreetId(streetId);
                        } else {
                            throw new IllegalStateException("Building id was not found.");
                        }
                        request.setStatus(AddressLinkStatus.ADDRESS_LINKED);
                        return;
                    } else { // по доп. информации, состоящей из номера и корпуса дома, не смогли однозначно определить улицу
                        request.setStatus(AddressLinkStatus.STREET_AND_BUILDING_UNRESOLVED);
                        return;
                    }
                }
            } else { // не нашли ни одной улицы
                request.setStatus(AddressLinkStatus.STREET_UNRESOLVED);
                return;
            }
        }

        //Связывание дома
        List<BuildingCorrection> buildingCorrections = addressCorrectionBean.getBuildingCorrections(
                request.getStreetId(), null, request.getBuildingNumber(), request.getBuildingCorp(),
                organizationId, userOrganizationId);

        if (buildingCorrections.size() == 1) {
            request.setBuildingId(buildingCorrections.get(0).getObjectId());
        } else if (buildingCorrections.size() > 1) {
            request.setStatus(AddressLinkStatus.MORE_ONE_BUILDING_CORRECTION);
        } else {
            List<Long> buildingIds = buildingStrategy.getBuildingObjectIds(request.getCityId(),
                    request.getStreetId(), request.getBuildingNumber(), request.getBuildingCorp());

            if (buildingIds.size() == 1){
                request.setBuildingId(buildingIds.get(0));
            } else if (buildingIds.size() > 1) {
                request.setStatus(AddressLinkStatus.MORE_ONE_BUILDING);
            } else if (buildingIds.isEmpty()){
                request.setStatus(AddressLinkStatus.BUILDING_UNRESOLVED);
            }
        }

        if (request.getBuildingId() == null) {
            return;
        }

        //Связывание квартиры
        if (StringUtils.isNotEmpty(request.getApartment())) {
            List<ApartmentCorrection> apartmentCorrections = addressCorrectionBean.getApartmentCorrections(
                    request.getBuildingId(), null, null, request.getApartment(), organizationId, userOrganizationId);
            if (apartmentCorrections.size() == 1) {
                request.setApartmentId(apartmentCorrections.get(0).getObjectId());
            } else if (apartmentCorrections.size() > 1) {
                request.setStatus(AddressLinkStatus.MORE_ONE_APARTMENT_CORRECTION);
            } else {
                List<Long> apartmentIds = apartmentStrategy.getApartmentObjectIds(request.getBuildingId(),
                        request.getApartment());

                if (apartmentIds.size() == 1){
                    request.setApartmentId(apartmentIds.get(0));
                } else if (apartmentIds.size() > 1) {
                    request.setStatus(AddressLinkStatus.MORE_ONE_APARTMENT);
                } else if (apartmentIds.isEmpty()){
                    request.setStatus(AddressLinkStatus.APARTMENT_UNRESOLVED);
                }
            }

            if (request.getApartmentId() == null) {
                return;
            }
        }

        //Связанно с внутренней адресной базой
        request.setStatus(AddressLinkStatus.ADDRESS_LINKED);
    }
}
