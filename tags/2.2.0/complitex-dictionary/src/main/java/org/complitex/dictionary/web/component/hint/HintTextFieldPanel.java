/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.hint;

import java.text.MessageFormat;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.resources.WebCommonResourceInitializer;

/**
 *
 * @author Artem
 */
public final class HintTextFieldPanel<T> extends Panel {

    public static final String HINT_ATTRIBUTE = "placeholder";
    public static final String HINT_CSS_CLASS = "placeholder";
    private static final String INIT_HINT_JAVASCRIPT = "$(document).ready(function()'{'"
            + "  $(''#{0}'').placeholder();"
            + "});";
    private TextField<T> textField;
    private Label script;

    public HintTextFieldPanel(String id, IModel<T> model, Class<T> type, IModel<String> placeholderModel) {
        super(id);
        init(model, type, placeholderModel, null);
    }

    public HintTextFieldPanel(String id, IModel<T> model, Class<T> type, IModel<String> placeholderModel, String textFieldValidatorKeyPrefix) {
        super(id);
        init(model, type, placeholderModel, textFieldValidatorKeyPrefix);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderJavaScriptReference(WebCommonResourceInitializer.PLACEHOLDER_JS);
    }

    private void init(IModel<T> model, Class<T> type, final IModel<String> placeholderModel, final String textFieldValidatorKeyPrefix) {
        textField = new TextField<T>("textField", model, type) {

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (isEnabledInHierarchy()) {
                    String value = placeholderModel.getObject();
                    if (!Strings.isEmpty(value)) {
                        tag.put(HINT_ATTRIBUTE, value);
                    }
                }
            }

            @Override
            public String getValidatorKeyPrefix() {
                if (!Strings.isEmpty(textFieldValidatorKeyPrefix)) {
                    return textFieldValidatorKeyPrefix;
                } else {
                    return super.getValidatorKeyPrefix();
                }
            }

            @Override
            public String[] getInputAsArray() {
                String[] requestInput = super.getInputAsArray();
                if (requestInput != null && requestInput.length > 0 && requestInput[0] != null) {
                    if (requestInput[0].trim().equals(placeholderModel.getObject())) {
                        return new String[]{""};
                    }
                }
                return requestInput;
            }
        };
        textField.setOutputMarkupId(true);
        add(textField);

        script = new Label("script");
        script.setEscapeModelStrings(false);
        add(script);
    }

    public TextField<T> getTextField() {
        return textField;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final String markupId = textField.getMarkupId();
        script.setDefaultModel(new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return MessageFormat.format(INIT_HINT_JAVASCRIPT, markupId);
            }
        });
    }
}
