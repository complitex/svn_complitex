/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.dateinput;

import java.util.Date;
import java.util.Locale;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.DatePicker;
import org.odlabs.wiquery.core.commons.WiQueryResourceManager;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.datepicker.DateOption;

/**
 *
 * @author Artem
 */
public class MaskedDateInput extends DatePicker<Date> {

    private final MaskedDateInputOptions options;

    public MaskedDateInput(String id, IModel<Date> model) {
        super(id, model, Date.class);
        options = new MaskedDateInputOptions(this);
    }

    public MaskedDateInput(String id, IModel<Date> model, boolean enabled) {
        super(id, model, Date.class, enabled);
        options = new MaskedDateInputOptions(this);
    }

    public MaskedDateInput(String id) {
        super(id, Date.class);
        options = new MaskedDateInputOptions(this);
    }

    public MaskedDateInput(String id, boolean enabled) {
        super(id, Date.class, enabled);
        options = new MaskedDateInputOptions(this);
    }

    @Override
    protected void init(boolean enabled) {
        super.init(enabled);
        if (enabled) {
            setShowOn(ShowOnEnum.BUTTON);
        }
    }

    public MaskedDateInput setMaxDate(Date maxDate) {
        DateOption maxDateOption = new DateOption(maxDate);
        super.setMaxDate(maxDateOption);
        options.setMaxDate(maxDateOption);
        return this;
    }

    public MaskedDateInput setMinDate(Date minDate) {
        DateOption minDateOption = new DateOption(minDate);
        super.setMinDate(minDateOption);
        options.setMinDate(minDateOption);
        return this;
    }

    @Override
    public org.odlabs.wiquery.ui.datepicker.DatePicker<Date> setMaxDate(DateOption maxDate) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public org.odlabs.wiquery.ui.datepicker.DatePicker<Date> setMinDate(DateOption minDate) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public DateOption getMaxDate() {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public DateOption getMinDate() {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    protected void detachModel() {
        super.detachModel();
        options.detach();
    }

    @Override
    public JsStatement statement() {
        return super.statement().chain("mask_dateinput", options.getOptions().getJavaScriptOptions());
    }

    @Override
    public void contribute(WiQueryResourceManager wiQueryResourceManager) {
        super.contribute(wiQueryResourceManager);

        wiQueryResourceManager.addJavaScriptResource(MaskedDateInput.class, "jquery.masked_dateinput.js");

        // If locale is null or current locale is US: no translation is needed.
        Locale locale = getLocale();
        if (locale != null && !locale.equals(Locale.US)) {
            wiQueryResourceManager.addJavaScriptResource(new MaskedDateInputLanguageResourceReference(locale));
        }

        wiQueryResourceManager.addCssResource(MaskedDateInput.class, "jquery.masked_dateinput.css");
    }
}
