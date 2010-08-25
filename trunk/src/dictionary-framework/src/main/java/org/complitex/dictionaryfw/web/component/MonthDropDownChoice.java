package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.text.DateFormatSymbols;
import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 16:22:44
 */
public class MonthDropDownChoice extends DropDownChoice<Integer>{
    private final String[] MONTHS = DateFormatSymbols.getInstance(getLocale()).getMonths();

    public MonthDropDownChoice(String id){
        this(id, null);
    }

    public MonthDropDownChoice(String id, IModel<Integer> model) {
        super(id);
        setModel(model != null ? model : new Model<Integer>());
        setChoices(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        setChoiceRenderer(new IChoiceRenderer<Integer>(){

            @Override
            public Object getDisplayValue(Integer object) {
                return MONTHS[object];
            }

            @Override
            public String getIdValue(Integer object, int index) {
                return object.toString();
            }
        });
    }
}
