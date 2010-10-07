/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.converter;

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
