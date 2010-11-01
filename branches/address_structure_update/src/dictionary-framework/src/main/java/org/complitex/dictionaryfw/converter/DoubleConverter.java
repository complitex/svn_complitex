/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.converter;

/**
 *
 * @author Artem
 */
public class DoubleConverter extends AbstractConverter<Double> {

    @Override
    public Double toObject(String value) {
        return Double.valueOf(value);
    }
}
