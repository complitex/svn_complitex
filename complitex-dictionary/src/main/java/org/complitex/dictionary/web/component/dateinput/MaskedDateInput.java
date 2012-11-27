/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.dateinput;

import java.util.Date;
import java.util.Locale;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.web.component.DatePicker;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.datepicker.DateOption;
import static org.complitex.dictionary.util.DateUtil.*;

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

    public MaskedDateInput(String id) {
        super(id, Date.class);
        options = new MaskedDateInputOptions(this);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (isEnabled()) {
            setShowOn(ShowOnEnum.BUTTON);
        }
    }

    public MaskedDateInput setMaxDate(Date maxDate) {
        Date max = getEndOfDay(maxDate);
        DateOption maxDateOption = new DateOption(max);
        super.setMaxDate(maxDateOption);
        options.setMaxDate(maxDateOption);
        return this;
    }

    public MaskedDateInput setMinDate(Date minDate) {
        Date min = getBeginOfDay(minDate);
        DateOption minDateOption = new DateOption(min);
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
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.renderJavaScriptReference(new PackageResourceReference(MaskedDateInput.class, "jquery.masked_dateinput.js"));

        // If locale is null or current locale is US: no translation is needed.
        Locale locale = getLocale();
        if (locale != null && !locale.equals(Locale.US)) {
            response.renderJavaScriptReference(new MaskedDateInputLanguageResourceReference(locale));
        }

        response.renderCSSReference(new PackageResourceReference(MaskedDateInput.class, "jquery.masked_dateinput.css"));
    }
}
