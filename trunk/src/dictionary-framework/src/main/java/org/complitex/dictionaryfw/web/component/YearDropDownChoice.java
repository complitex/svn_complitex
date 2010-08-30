package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.markup.html.form.DropDownChoice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.08.2010 12:36:30
 */
public class YearDropDownChoice extends DropDownChoice<Integer> {
    public YearDropDownChoice(String id){
        this(id, 1991, Calendar.getInstance().get(Calendar.YEAR));        
    }

    public YearDropDownChoice(String id, int start, int end) {
        super(id);

        List<Integer> years = new ArrayList<Integer>();
        for (int i = end; i >= start; --i){
            years.add(i);
        }

        setChoices(years);
    }
}
