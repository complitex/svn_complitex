/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information;

import com.google.common.collect.ImmutableList;
import java.util.Collection;

/**
 *
 * @author Artem
 */
public final class BookEntities {

    private static final Collection<String> BOOK_ENTITIES = ImmutableList.of("country", "region", "city", "district", "street", "building", "apartment",
            "room");

    private BookEntities() {
    }

    public static Collection<String> getEntities() {
        return BOOK_ENTITIES;
    }
}
