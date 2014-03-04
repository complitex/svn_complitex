/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

/**
 *
 * @author Artem
 */
public class StringConverter implements IConverter<String> {

    @Override
    public String toString(String object) {
        return object;
    }

    @Override
    public String toObject(String value) {
        return value;
    }
}
