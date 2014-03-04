/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public interface IConverter<T> extends Serializable {

    String toString(T object);

    T toObject(String value);
}
