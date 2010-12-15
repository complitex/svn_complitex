package org.complitex.template.web.component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.service.LocaleBean;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class LocalePicker extends Panel {

    @EJB(name = "LocaleBean")
    private LocaleBean localeBean;

    public LocalePicker(String id) {
        super(id);

        List<Locale> locales = Lists.newArrayList(Iterables.transform(localeBean.getAllLocales(),
                new Function<org.complitex.dictionary.entity.Locale, Locale>() {

                    @Override
                    public Locale apply(org.complitex.dictionary.entity.Locale locale) {
                        return localeBean.convert(locale);
                    }
                }));

        IModel<Locale> model = new Model<Locale>() {

            @Override
            public Locale getObject() {
                return getSession().getLocale();
            }

            @Override
            public void setObject(Locale locale) {
                getSession().setLocale(locale);
            }
        };

        IChoiceRenderer<Locale> renderer = new ChoiceRenderer<Locale>() {

            @Override
            public Object getDisplayValue(Locale locale) {
                return locale.getDisplayName(getLocale());
            }
        };

        add(new DropDownChoice<Locale>("localeDropDown", model, locales, renderer) {

            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        });
    }
}
