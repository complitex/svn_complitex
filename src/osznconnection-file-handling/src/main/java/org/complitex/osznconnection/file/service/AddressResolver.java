/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.complitex.dictionaryfw.entity.example.ComparisonType;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.Status;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Map;
import org.complitex.dictionaryfw.mybatis.Transactional;

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

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(beanName = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @Transactional
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
        streetStrategy.configureExample(streetExample, ids, street);

        count = streetStrategy.count(streetExample);
        if (count == 1) {
            streetId = streetStrategy.find(streetExample).get(0).getId();
            ids.put("street", streetId);
        } else {
            streetId = addressCorrectionBean.findStreet(cityId, street, organizationId);
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
        buildingStrategy.configureExample(buildingExample, ids, building);

        count = buildingStrategy.count(buildingExample);
        if (count == 1) {
            buildingId = buildingStrategy.find(buildingExample).get(0).getId();
            ids.put("building", buildingId);
        } else {
            buildingId = addressCorrectionBean.findBuilding(streetId, building, organizationId);
            if (buildingId != null) {
                ids.put("building", buildingId);
            }
        }
        if (buildingId == null) {
            return new InternalAddress(cityId, streetId, buildingId, apartmentId);
        }

        Strategy apartmentStrategy = strategyFactory.getStrategy("apartment");
        DomainObjectExample apartmentExample = new DomainObjectExample();
        apartmentExample.setComparisonType(ComparisonType.EQUALITY.name());
        apartmentStrategy.configureExample(apartmentExample, ids, apartment);

        count = apartmentStrategy.count(apartmentExample);
        if (count == 1) {
            apartmentId = apartmentStrategy.find(apartmentExample).get(0).getId();
        } else {
            apartmentId = addressCorrectionBean.findApartment(buildingId, apartment, organizationId);
        }

        return new InternalAddress(cityId, streetId, buildingId, apartmentId);
    }

    @Transactional
    public Payment correctAddress(Payment payment, long cityId, long streetId, long buildingId, long apartmentId) {
        long organizationId = payment.getOrganizationId();

        String city = (String) payment.getField(PaymentDBF.N_NAME);
        String street = (String) payment.getField(PaymentDBF.VUL_NAME);
        String building = (String) payment.getField(PaymentDBF.BLD_NUM);
        String apartment = (String) payment.getField(PaymentDBF.FLAT);

        if (payment.getCityId() == null) {
            addressCorrectionBean.insertCity(city, cityId, organizationId);
        } else if (payment.getStreetId() == null) {
            addressCorrectionBean.insertStreet(street, streetId, organizationId);
        } else if (payment.getBuildingId() == null) {
            addressCorrectionBean.insertBuilding(building, buildingId, organizationId);
        } else if (payment.getApartmentId() == null) {
            addressCorrectionBean.insertApartment(apartment, apartmentId, organizationId);
        }

        payment.setCityId(cityId);
        payment.setStreetId(streetId);
        payment.setBuildingId(buildingId);
        payment.setApartmentId(apartmentId);
        payment.setStatus(Status.ACCOUNT_NUMBER_UNRESOLVED_LOCALLY);
        return payment;
    }
}
