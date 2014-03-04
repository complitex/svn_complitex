/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.type;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.Gender;
import org.complitex.dictionary.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public final class GenderPanel extends Panel {

    private static final String RESOURCE_BUNDLE = GenderPanel.class.getName();
    private static final List<Gender> GENDERS = Arrays.asList(Gender.values());

    public GenderPanel(String id, IModel<Gender> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id);

        RadioChoice<Gender> gender = new RadioChoice<Gender>("gender", model, GENDERS, new IChoiceRenderer<Gender>() {

            @Override
            public Object getDisplayValue(Gender gender) {
                return display(gender, getLocale());
            }

            @Override
            public String getIdValue(Gender gender, int index) {
                return gender.name();
            }
        });
        gender.setSuffix("");
        gender.setRequired(required);
        gender.setEnabled(enabled);
        gender.setLabel(labelModel);
        add(gender);
    }

    public static String display(Gender gender, Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, resourceKey(gender), locale);
    }

    private static String resourceKey(Gender gender) {
        return gender.getDeclaringClass().getSimpleName() + "." + gender.name();
    }
}
