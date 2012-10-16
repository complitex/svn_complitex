package org.complitex.dictionary.util;

import com.google.common.base.CaseFormat;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.TextLabel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.09.12 18:22
 */
public class PageUtil {
    public static ArrowOrderByBorder[] newSorting(String prefix, DataProvider dataProvider, DataView dataView,
                                                  Component refreshComponent, boolean camelToUnderscore, String... properties){
        ArrowOrderByBorder[] arrowOrderByBorders = new ArrowOrderByBorder[properties.length];

        for (int i = 0; i < properties.length; i++) {
            String p = properties[i];

            if(camelToUnderscore){
                p = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, p);
            }

            arrowOrderByBorders[i] = new ArrowOrderByBorder(prefix + p, p, dataProvider, dataView, refreshComponent);
        }

        return arrowOrderByBorders;
    }

    public static ArrowOrderByBorder[] newSorting(String prefix, DataProvider dataProvider, DataView dataView,
                                                  Component refreshComponent, String... properties){
        return newSorting(prefix, dataProvider, dataView, refreshComponent, false, properties);
    }

    public static TextLabel[] newTextLabels(String... properties){
        TextLabel[] textLabels = new TextLabel[properties.length];

        for (int i = 0; i < properties.length; i++){
            textLabels[i] = new TextLabel(properties[i]);
        }

        return textLabels;
    }

    public static TextField[] newTextFields(String prefix, String... properties){
        TextField[] textFields = new TextField[properties.length];

        for (int i = 0; i < properties.length; i++){
            textFields[i] = new TextField(prefix + properties[i]);
        }

        return textFields;
    }

    public static TextField[] newRequiredTextFields(String... properties){
        TextField[] textFields = newTextFields("", properties);

        for (TextField textField : textFields){
            textField.setRequired(true);
        }

        return textFields;
    }

    public static DatePicker[] newDatePickers(boolean required, String... properties){
        DatePicker[] datePickers = new DatePicker[properties.length];

        for (int i = 0; i < properties.length; i++){
            datePickers[i] = new DatePicker(properties[i]);
            datePickers[i].setRequired(required);
        }

        return datePickers;
    }

    public static PageParameters newPageParameters(String key, Object value){
        PageParameters pageParameters = new PageParameters();
        pageParameters.add(key, value);

        return pageParameters;
    }


}
