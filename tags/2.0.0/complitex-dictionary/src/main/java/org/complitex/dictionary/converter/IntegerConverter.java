/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

/**
 *
 * @author Artem
 */
public class IntegerConverter extends AbstractConverter<Integer> {

    @Override
    public Integer toObject(String integer) {
        return Integer.valueOf(integer);
    }
}
