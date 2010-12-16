package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import java.text.DateFormatSymbols;
import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 16:22:44
 */
public class MonthDropDownChoice extends DropDownChoice<Integer>{
    private final String[] MONTHS = DateFormatSymbols.getInstance(getLocale()).getMonths();

    public MonthDropDownChoice(String id){
        super(id);        
        setChoices(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        setChoiceRenderer(new IChoiceRenderer<Integer>(){

            @Override
            public Object getDisplayValue(Integer object) {
                return MONTHS[object-1];
            }

            @Override
            public String getIdValue(Integer object, int index) {
                return object.toString();
            }
        });
    }

     public MonthDropDownChoice(String id, IModel<Integer> model) {
        this(id);
        setModel(model);
    }
}
