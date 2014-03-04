/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.convert.IConverter;

import java.util.Date;
import java.util.Locale;

import static org.complitex.dictionary.util.DateUtil.newDate;

/**
 *
 * @author Artem
 */
public class DatePicker<T> extends org.odlabs.wiquery.ui.datepicker.DatePicker<T> {
    public final static Date DEFAULT_END_DATE = newDate(31, 12, 2054);

    private static final String RELATIVE_IMAGE_SRC = "images/calendar2.png";

    private static IConverter<Date> CONVERTER = new PatternDateConverter("dd.MM.yyyy", true);

    private static IConverter<Date> NOT_NULL_CONVERTER = new PatternDateConverter("dd.MM.yyyy", true){
        @Override
        public Date convertToObject(String value, Locale locale) {
            return value != null && !value.isEmpty() ? super.convertToObject(value, locale) : DEFAULT_END_DATE;
        }

        @Override
        public String convertToString(Date value, Locale locale) {
            return !DEFAULT_END_DATE.equals(value) ? super.convertToString(value, locale) : null;
        }
    };

    {
        setConvertEmptyInputStringToNull(false);
    }

    private boolean nullable = true;

    public DatePicker(String id) {
        super(id);
    }

    public DatePicker(String id, Class<T> type) {
        super(id, type);
    }

    public DatePicker(String id, IModel<T> model) {
        super(id, model);
    }

    public DatePicker(String id, IModel<T> model, Class<T> type) {
        super(id, model, type);
    }


    public boolean isNullable() {
        return nullable;
    }

    public DatePicker<T> setNullable(boolean nullable) {
        this.nullable = nullable;

        return this;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (isEnabled()) {
            setButtonImage(getImageUrl(getRequestCycle()));
            setShowOn(ShowOnEnum.BOTH);
            setButtonImageOnly(true);
            setChangeYear(true);
        }
        setDateFormat("dd.mm.yy");
    }

    private static String getImageUrl(RequestCycle requestCycle) {
        return requestCycle.urlFor(new SharedResourceReference(RELATIVE_IMAGE_SRC), null).toString();
    }

    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        //noinspection unchecked
        return nullable ? (IConverter<C>) CONVERTER : (IConverter<C>) NOT_NULL_CONVERTER;
    }
}
