/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.service;

import java.util.Locale;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.search.SearchComponentState;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AddressRendererBean {

    @EJB
    private StrategyFactory strategyFactory;

    public String displayAddress(String addressEntity, long addressId, Locale locale, String[] addressUnits) {
        if (addressEntity == null || addressEntity.length() == 0) {
            throw new IllegalArgumentException("Address units list is null or empty.");
        }
        if (Strings.isEmpty(addressEntity)) {
            throw new IllegalArgumentException("Address entity is null or empty.");
        }

        IStrategy strategy = strategyFactory.getStrategy(addressEntity);
        Strategy.RestrictedObjectInfo info = strategy.findParentInSearchComponent(addressId, null);
        SearchComponentState addressComponentState = new SearchComponentState();
        if (info != null) {
            addressComponentState = strategy.getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
        }
        DomainObject addressObject = strategy.findById(addressId, true);
        if (addressObject != null) {
            addressComponentState.put(addressEntity, addressObject);
        }

        StringBuilder addressLabel = new StringBuilder("");
        for (int i = 0; i < addressUnits.length; i++) {
            String addressUnit = addressUnits[i];
            DomainObject currentAddressObject = addressComponentState.get(addressUnit);
            if (currentAddressObject != null) {
                IStrategy currentStrategy = strategyFactory.getStrategy(addressUnit);
                addressLabel.append(currentStrategy.displayDomainObject(currentAddressObject, locale));
                if (i < addressUnits.length - 1) {
                    addressLabel.append(", ");
                }
            }
        }
        return addressLabel.toString();
    }

    public String displayAddress(String addressEntity, long addressId, Locale locale) {
        return displayAddress(addressEntity, addressId, locale, new String[]{"city", "street", "building", "apartment", "room"});
    }
}
