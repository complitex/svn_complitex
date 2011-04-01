/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import org.apache.wicket.Application;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author Artem
 */
public class DatePicker<T> extends org.odlabs.wiquery.ui.datepicker.DatePicker<T> {

    private static final String IMAGE_SRC = "resources/" + Application.class.getName() + "/images/calendar.gif";
    private static IConverter CONVERTER = new PatternDateConverter("dd.MM.yyyy", true);

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
            setButtonImage(IMAGE_SRC);
            setShowOn(ShowOnEnum.BOTH);
            setButtonImageOnly(true);
        }
        setEnabled(enabled);
        setDateFormat("dd.mm.yy");
    }

    @Override
    public IConverter getConverter(Class<?> type) {
        return CONVERTER;
    }
}
