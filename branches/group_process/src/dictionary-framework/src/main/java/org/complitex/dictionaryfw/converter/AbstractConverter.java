/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.converter;

/**
 *
 * @author Artem
 */
public abstract class AbstractConverter<T> implements IConverter<T> {

    @Override
    public String toString(T object) {
        return object.toString();
    }
}
