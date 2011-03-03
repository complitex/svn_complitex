/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

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
