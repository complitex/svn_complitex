/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.dateinput;

import org.apache.wicket.Component;
import org.odlabs.wiquery.core.options.IComplexOption;
import org.odlabs.wiquery.core.options.Options;
import org.odlabs.wiquery.ui.datepicker.DateOption;

/**
 *
 * @author Artem
 */
public class MaskedDateInputOptions extends Options {

    public MaskedDateInputOptions(Component owner) {
        super(owner);
    }

    public void setMaxDate(DateOption maxDate) {
        put("max_date", maxDate);
    }

    public DateOption getMaxDate() {
        IComplexOption maxDate = getComplexOption("max_date");

        if (maxDate != null && maxDate instanceof DateOption) {
            return (DateOption) maxDate;
        }

        return null;
    }

    public void setMinDate(DateOption minDate) {
        put("min_date", minDate);
    }

    public DateOption getMinDate() {
        IComplexOption minDate = getComplexOption("min_date");

        if (minDate != null && minDate instanceof DateOption) {
            return (DateOption) minDate;
        }

        return null;
    }

    public Options getOptions() {
        return this;
    }
}
