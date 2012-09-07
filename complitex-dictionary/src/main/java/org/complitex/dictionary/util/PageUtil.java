package org.complitex.dictionary.util;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.data.DataView;
import org.complitex.dictionary.web.component.TextLabel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.09.12 18:22
 */
public class PageUtil {
    public static ArrowOrderByBorder[] newSorting(String prefix, DataProvider dataProvider, DataView dataView,
                                                  Component refreshComponent, String... properties){
        ArrowOrderByBorder[] arrowOrderByBorders = new ArrowOrderByBorder[properties.length];

        for (int i = 0; i < properties.length; i++) {
            String p = properties[i];

            arrowOrderByBorders[i] = new ArrowOrderByBorder(prefix + p, p, dataProvider, dataView, refreshComponent);
        }

        return arrowOrderByBorders;
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
}
