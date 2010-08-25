/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.entity.example.ComparisonType;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.entity.Status;

/**
 *
 * @author Artem
 */
@Stateless
public class AddressResolver extends AbstractBean {

    public static class InternalAddress {

        private Long city;

        private Long street;

        private Long building;

        private Long apartment;

        public InternalAddress(Long city, Long street, Long building, Long apartment) {
            this.city = city;
            this.street = street;
            this.building = building;
            this.apartment = apartment;
        }

        public Long getApartment() {
            return apartment;
        }

        public Long getBuilding() {
            return building;
        }

        public Long getCity() {
            return city;
        }

        public Long getStreet() {
            return street;
        }

        public boolean isCorrect() {
            return city != null && street != null && building != null && apartment != null;
        }
    }

    @EJB
    private StrategyFactory strategyFactory;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private RequestPaymentBean requestPaymentBean;

    public InternalAddress resolveAddress(String city, String street, String building, String apartment, long organizationId) {
        Long cityId = null;
        Long streetId = null;
        Long buildingId = null;
        Long apartmentId = null;

        Map<String, Long> ids = Maps.newHashMap();

        Strategy cityStrategy = strategyFactory.getStrategy("city");
        DomainObjectExample cityExample = new DomainObjectExample();
        cityExample.setComparisonType(ComparisonType.EQUALITY.name());
        cityStrategy.configureExample(cityExample, ids, city);
        cityExample.setParentEntity(null);

        int count = cityStrategy.count(cityExample);
        if (count == 1) {
            cityId = cityStrategy.find(cityExample).get(0).getId();
            ids.put("city", cityId);
        } else {
            cityId = addressCorrectionBean.findCity(city, organizationId);
            if (cityId != null) {
                ids.put("city", cityId);
            }
        }
        if (cityId == null) {
            return new InternalAddress(cityId, streetId, buildingId, apartmentId);
        }

        Strategy streetStrategy = strategyFactory.getStrategy("street");
        DomainObjectExample streetExample = new DomainObjectExample();
        streetExample.setComparisonType(ComparisonType.EQUALITY.name());
        streetStrategy.configureExample(cityExample, ids, city);

        count = streetStrategy.count(streetExample);
        if (count == 1) {
            streetId = streetStrategy.find(streetExample).get(0).getId();
            ids.put("street", streetId);
        } else {
            streetId = addressCorrectionBean.findStreet(city, street, organizationId);
            if (streetId != null) {
                ids.put("street", streetId);
            }
        }
        if (streetId == null) {
            return new InternalAddress(cityId, streetId, buildingId, apartmentId);
        }

        Strategy buildingStrategy = strategyFactory.getStrategy("building");
        DomainObjectExample buildingExample = new DomainObjectExample();
        buildingExample.setComparisonType(ComparisonType.EQUALITY.name());
        buildingStrategy.configureExample(cityExample, ids, city);

        count = buildingStrategy.count(buildingExample);
        if (count == 1) {
            buildingId = buildingStrategy.find(buildingExample).get(0).getId();
            ids.put("building", buildingId);
        } else {
            buildingId = addressCorrectionBean.findBuilding(city, street, building, organizationId);
            if (buildingId != null) {
                ids.put("building", buildingId);
            }
        }
        if (buildingId == null) {
            return new InternalAddress(cityId, buildingId, buildingId, apartmentId);
        }

        Strategy apartmentStrategy = strategyFactory.getStrategy("apartment");
        DomainObjectExample apartmentExample = new DomainObjectExample();
        apartmentExample.setComparisonType(ComparisonType.EQUALITY.name());
        apartmentStrategy.configureExample(cityExample, ids, city);

        count = apartmentStrategy.count(apartmentExample);
        if (count == 1) {
            apartmentId = apartmentStrategy.find(apartmentExample).get(0).getId();
        } else {
            apartmentId = addressCorrectionBean.findApartment(city, street, building, apartment, organizationId);
        }

        return new InternalAddress(cityId, apartmentId, buildingId, apartmentId);
    }

    public RequestPayment correctAddress(RequestPayment requestPayment, long cityId, long streetId, long buildingId, long apartmentId) {
        long organizationId = requestPayment.getOrganizationId();

        String city = requestPayment.getnName();
        String street = requestPayment.getVulName();
        String building = requestPayment.getBldNum();
        String apartment = requestPayment.getFlat();

        if (requestPayment.getCityId() == null) {
            addressCorrectionBean.insertCity(city, street, building, apartment, cityId, organizationId);
        } else if (requestPayment.getStreetId() == null) {
            addressCorrectionBean.insertStreet(city, street, building, apartment, streetId, organizationId);
        } else if (requestPayment.getBuildingId() == null) {
            addressCorrectionBean.insertBuilding(city, street, building, apartment, streetId, organizationId);
        } else if (requestPayment.getApartmentId() == null) {
            addressCorrectionBean.insertApartment(city, street, building, apartment, streetId, organizationId);
        }

        requestPayment.setCityId(cityId);
        requestPayment.setStreetId(streetId);
        requestPayment.setBuildingId(buildingId);
        requestPayment.setApartmentId(apartmentId);
        requestPayment.setStatus(Status.RESOLVED);
        return requestPayment;
    }
}
