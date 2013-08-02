/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

import org.complitex.dictionary.entity.Gender;

/**
 *
 * @author Artem
 */
public class GenderConverter implements IConverter<Gender> {

    @Override
    public Gender toObject(String value) {
        return Enum.valueOf(Gender.class, value);
    }

    @Override
    public String toString(Gender object) {
        return object.name();
    }
}
