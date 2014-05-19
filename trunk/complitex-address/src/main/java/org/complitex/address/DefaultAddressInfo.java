/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 *
 * @author Artem
 */
public class DefaultAddressInfo implements AddressInfo {

    private static final List<String> ADDRESSES = ImmutableList.of("country", "region", "city", "city_type", "district",
            "street", "street_type", "building", "apartment", "room");
    private static final List<String> ADDRESS_DESCRIPTIONS = ImmutableList.of("country", "region", "city", "district",
            "street", "building", "apartment", "room");

    @Override
    public List<String> getAddresses() {
        return ADDRESSES;
    }

    @Override
    public List<String> getAddressDescriptions() {
        return ADDRESS_DESCRIPTIONS;
    }
}
