package org.complitex.dictionary.converter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 13.12.12 13:52
 */
public class BigDecimalConverter extends org.apache.wicket.util.convert.converter.BigDecimalConverter {
    private int maximumFractionDigits = 3;

    public BigDecimalConverter() {
    }

    public BigDecimalConverter(int maximumFractionDigits) {
        this.maximumFractionDigits = maximumFractionDigits;
    }

    @Override
    public String convertToString(BigDecimal value, Locale locale) {
        NumberFormat fmt = getNumberFormat(locale);
        if (fmt != null){
            fmt.setMinimumFractionDigits(maximumFractionDigits);
            fmt.setMaximumFractionDigits(maximumFractionDigits);

            return fmt.format(value);
        }
        return value.toString();

    }
}
