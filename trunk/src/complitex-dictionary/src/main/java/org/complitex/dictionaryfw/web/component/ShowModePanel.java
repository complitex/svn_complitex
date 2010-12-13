/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Artem
 */
public final class ShowModePanel extends Panel {

    private IModel<ShowMode> model;

    public ShowModePanel(String id, IModel<ShowMode> model) {
        super(id);
        this.model = model;
        init();
    }

    private void init() {
        IChoiceRenderer renderer = new EnumChoiceRenderer(this);
        List<ShowMode> choices = Arrays.asList(ShowMode.values());
        RadioChoice showBooksMode = new RadioChoice("showBooksMode", model, choices, renderer);
        showBooksMode.setSuffix("");
        add(showBooksMode);
    }
}
