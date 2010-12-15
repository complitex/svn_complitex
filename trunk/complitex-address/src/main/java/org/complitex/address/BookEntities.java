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
public final class BookEntities {

    private static final List<String> BOOK_ENTITIES = ImmutableList.of("country", "region", "city", "city_type", "district", "street", "street_type",
            "building");

    private static final List<String> BOOK_ENTITY_DESCRIPTIONS = ImmutableList.of("country", "region", "city", "district", "street", "building");

    private BookEntities() {
    }

    public static List<String> getEntities() {
        return BOOK_ENTITIES;
    }

    public static List<String> getEntityDescriptions() {
        return BOOK_ENTITY_DESCRIPTIONS;
    }
}
