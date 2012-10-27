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

    private static final String RELATIVE_IMAGE_SRC = "images/calendar2.png";
    private static IConverter<Date> CONVERTER = new PatternDateConverter("dd.MM.yyyy", true);

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

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (isEnabled()) {
            setButtonImage(getImageUrl(getRequestCycle()));
            setShowOn(ShowOnEnum.BOTH);
            setButtonImageOnly(true);
        }
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
