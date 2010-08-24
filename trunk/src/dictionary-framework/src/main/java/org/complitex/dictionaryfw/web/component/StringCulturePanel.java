/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionaryfw.dao.LocaleBean;
import org.complitex.dictionaryfw.entity.StringCulture;

/**
 *
 * @author Artem
 */
public class StringCulturePanel extends Panel {

    @EJB(name = "LocaleDao")
    private LocaleBean localeDao;

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
            final MarkupContainer[] toUpdate){
        add(new ListView<StringCulture>("strings", model) {

            @Override
            protected void populateItem(ListItem<StringCulture> item) {
                StringCulture culture = item.getModelObject();

                Label lang = new Label("lang", new Locale(culture.getLocale()).getDisplayLanguage(getLocale()));
                item.add(lang);

                boolean isSystemLocale = false;
                if (new Locale(culture.getLocale()).getLanguage().equalsIgnoreCase(new Locale(localeDao.getSystemLocale()).getLanguage())) {
                    isSystemLocale = true;
                }

                InputPanel<String> inputPanel = new InputPanel("inputPanel", new PropertyModel<String>(culture, "value"),
                        String.class, required && isSystemLocale, labelModel, enabled, toUpdate);
                item.add(inputPanel);

                WebMarkupContainer requiredContainer = new WebMarkupContainer("bookFieldRequired");
                requiredContainer.setVisible(isSystemLocale);
                item.add(requiredContainer);
            }
        });
    }
}
