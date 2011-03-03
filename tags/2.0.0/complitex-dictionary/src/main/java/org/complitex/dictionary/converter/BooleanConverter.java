/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

/**
 *
 * @author Artem
 */
public class BooleanConverter extends AbstractConverter<Boolean> {

    @Override
    public Boolean toObject(String bool) {
        return Boolean.valueOf(bool);
    }
}
