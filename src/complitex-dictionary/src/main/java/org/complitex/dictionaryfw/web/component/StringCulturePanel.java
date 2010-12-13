/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionaryfw.entity.StringCulture;
import org.complitex.dictionaryfw.service.LocaleBean;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class StringCulturePanel extends Panel {

    @EJB(name = "LocaleBean")
    private LocaleBean localeBean;

    /**
     * For use in non-ajax environment
     * @param id
     * @param model
     * @param required
     * @param labelModel
     * @param enabled
     */
    public StringCulturePanel(String id, IModel<List<StringCulture>> model, final boolean required, final IModel<String> labelModel,
            final boolean enabled) {
        super(id);
        init(model, required, labelModel, enabled, null);
    }

    /**
     * For use in ajax environment
     * @param id
     * @param model
     * @param required
     * @param labelModel
     * @param enabled
     */
    public StringCulturePanel(String id, IModel<List<StringCulture>> model, final boolean required, final IModel<String> labelModel,
            final boolean enabled, MarkupContainer[] toUpdate) {
        super(id);
        init(model, required, labelModel, enabled, toUpdate);
    }

    protected void init(IModel<List<StringCulture>> model, final boolean required, final IModel<String> labelModel, final boolean enabled,
            final MarkupContainer[] toUpdate) {
        add(new ListView<StringCulture>("strings", model) {

            @Override
            protected void populateItem(ListItem<StringCulture> item) {
                StringCulture string = item.getModelObject();

                Label language = new Label("language", localeBean.convert(localeBean.getLocale(string.getLocaleId())).getDisplayLanguage(getLocale()));
                item.add(language);

                boolean isSystemLocale = false;
                if (localeBean.getLocale(string.getLocaleId()).isSystem()) {
                    isSystemLocale = true;
                }

                InputPanel<String> inputPanel = new InputPanel("inputPanel", new PropertyModel<String>(string, "value"),
                        String.class, required && isSystemLocale, labelModel, enabled, toUpdate);
                item.add(inputPanel);

                WebMarkupContainer requiredContainer = new WebMarkupContainer("bookFieldRequired");
                requiredContainer.setVisible(isSystemLocale);
                item.add(requiredContainer);
            }
        });
    }
}
