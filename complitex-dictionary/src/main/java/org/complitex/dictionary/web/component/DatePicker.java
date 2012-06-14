/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import java.util.Date;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author Artem
 */
public class DatePicker<T> extends org.odlabs.wiquery.ui.datepicker.DatePicker<T> {

    private static final String RELATIVE_IMAGE_SRC = "images/calendar.gif";
    private static IConverter<Date> CONVERTER = new PatternDateConverter("dd.MM.yyyy", true);

    public DatePicker(String id, boolean enabled) {
        super(id);
        init(enabled);
    }

    public DatePicker(String id) {
        super(id);
        init(true);
    }

    public DatePicker(String id, Class<T> type, boolean enabled) {
        super(id, type);
        init(enabled);
    }

    public DatePicker(String id, Class<T> type) {
        super(id, type);
        init(true);
    }

    public DatePicker(String id, IModel<T> model, boolean enabled) {
        super(id, model);
        init(enabled);
    }

    public DatePicker(String id, IModel<T> model) {
        super(id, model);
        init(true);
    }

    public DatePicker(String id, IModel<T> model, Class<T> type, boolean enabled) {
        super(id, model, type);
        init(enabled);
    }

    public DatePicker(String id, IModel<T> model, Class<T> type) {
        super(id, model, type);
        init(true);
    }

    protected void init(boolean enabled) {
        if (enabled) {
            setButtonImage(getImageUrl(getRequestCycle()));
            setShowOn(ShowOnEnum.BOTH);
            setButtonImageOnly(true);
        }
        setEnabled(enabled);
        setDateFormat("dd.mm.yy");
    }

    private static String getImageUrl(RequestCycle requestCycle) {
        return requestCycle.urlFor(new SharedResourceReference(RELATIVE_IMAGE_SRC), null).toString();
    }

    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        return (IConverter<C>) CONVERTER;
    }
}
