/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.service;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.SimpleObjectInfo;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Locale;

/**
 *
 * @author Artem
 */
@Stateless
public class AddressRendererBean {

    @EJB
    private StrategyFactory strategyFactory;

    @Transactional
    public String displayAddress(String addressEntity, long addressId, Locale locale, String[] addressUnits) {
        if (addressUnits == null || addressUnits.length == 0) {
            throw new IllegalArgumentException("Address units list is null or empty.");
        }
        if (Strings.isEmpty(addressEntity)) {
            throw new IllegalArgumentException("Address entity is null or empty.");
        }

        IStrategy strategy = strategyFactory.getStrategy(addressEntity);
        SimpleObjectInfo info = strategy.findParentInSearchComponent(addressId, null);
        SearchComponentState addressComponentState = new SearchComponentState();
        if (info != null) {
            addressComponentState = strategy.getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
        }
        DomainObject addressObject = strategy.findById(addressId, true);
        if (addressObject != null) {
            addressComponentState.put(addressEntity, addressObject);
        }

        StringBuilder addressLabel = new StringBuilder("");
        boolean isFirst = true;
        for (int i = 0; i < addressUnits.length; i++) {
            String addressUnit = addressUnits[i];
            DomainObject currentAddressObject = addressComponentState.get(addressUnit);
            if (currentAddressObject != null && currentAddressObject.getId() != null && currentAddressObject.getId() > 0) {
                if (!isFirst) {
                    addressLabel.append(", ");
                }
                if (isFirst) {
                    isFirst = false;
                }
                IStrategy currentStrategy = strategyFactory.getStrategy(addressUnit);
                addressLabel.append(currentStrategy.displayDomainObject(currentAddressObject, locale));
            }
        }
        addressLabel.append(".");
        return addressLabel.toString();
    }

    @Transactional
    public String displayAddress(String addressEntity, long addressId, Locale locale) {
        return displayAddress(addressEntity, addressId, locale, new String[]{"city", "street", "building", "apartment", "room"});
    }

    public String displayBuildingSimple(long buildingId, Locale locale) {
        return displayAddress("building", buildingId, locale, new String[]{"street", "building"});
    }
}
