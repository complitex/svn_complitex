package org.complitex.dictionary.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 12:36:30
 */
public class YearDropDownChoice extends DropDownChoice<Integer> {
    public YearDropDownChoice(String id){
        this(id, Calendar.getInstance().get(Calendar.YEAR) - 3, Calendar.getInstance().get(Calendar.YEAR));
    }

    public YearDropDownChoice(String id, int start, int end) {
        super(id);

        List<Integer> years = new ArrayList<Integer>();
        for (int i = end; i >= start; --i){
            years.add(i);
        }

        setChoices(years);
    }

    public YearDropDownChoice(String id, IModel<Integer> model) {        
        this(id);
        setModel(model);
    }
}
