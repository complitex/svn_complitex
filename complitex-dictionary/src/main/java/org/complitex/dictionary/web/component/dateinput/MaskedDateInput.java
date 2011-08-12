/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.dateinput;

import java.util.Date;
import java.util.Locale;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.resources.WebCommonResourceInitializer;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsStatement;

/**
 *
 * @author Artem
 */
public class MaskedDateInput extends DatePicker<Date> {

    public MaskedDateInput(String id, IModel<Date> model) {
        super(id, model, Date.class);
    }

    public MaskedDateInput(String id, IModel<Date> model, boolean enabled) {
        super(id, model, Date.class, enabled);
    }

    public MaskedDateInput(String id) {
        super(id, Date.class);
    }

    public MaskedDateInput(String id, boolean enabled) {
        super(id, Date.class, enabled);
    }

    @Override
    protected void init(boolean enabled) {
        super.init(enabled);
        if (enabled) {
            setShowOn(ShowOnEnum.BUTTON);
        }
    }

    @Override
    public JsStatement statement() {
        return super.statement().chain("mask_dateinput").chain("placeholder");
    }

    @Override
    public void contribute(WiQueryResourceManager wiQueryResourceManager) {
        super.contribute(wiQueryResourceManager);

        wiQueryResourceManager.addJavaScriptResource(MaskedDateInput.class, "jquery.masked_dateinput.js");
        wiQueryResourceManager.addJavaScriptResource(WebCommonResourceInitializer.PLACEHOLDER_JS);

        // If locale is null or current locale is US: no translation is needed.
        Locale locale = getLocale();
        if (locale != null && !locale.equals(Locale.US)) {
            wiQueryResourceManager.addJavaScriptResource(new MaskedDateInputLanguageResourceReference(locale));
        }

        wiQueryResourceManager.addCssResource(MaskedDateInput.class, "jquery.masked_dateinput.css");
    }
}
