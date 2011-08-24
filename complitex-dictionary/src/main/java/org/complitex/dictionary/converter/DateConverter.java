/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Artem
 */
public class DateConverter implements IConverter<Date> {

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public Date toObject(String date) {
        try {
            return DATE_FORMATTER.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString(Date date) {
        return date != null ? DATE_FORMATTER.format(date) : null;
    }
}
