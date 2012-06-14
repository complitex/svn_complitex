/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.type;

import java.util.Date;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.web.component.MonthDropDownChoice;
import org.complitex.dictionary.web.component.hint.HintTextFieldPanel;

/**
 *
 * @author Artem
 */
public final class Date2Panel extends FormComponentPanel<Date> {

    private Integer day;
    private Integer month;
    private Integer year;
    private final HintTextFieldPanel<Integer> dayField;
    private final MonthDropDownChoice monthField;
    private final HintTextFieldPanel<Integer> yearField;

    public Date2Panel(String id, final IModel<Date> model, boolean required, IModel<String> labelModel, boolean enabled) {
        super(id, model);
        setType(Date.class);
        setEnabled(enabled);
        setLabel(labelModel);
        setRequired(required);

        dayField = new HintTextFieldPanel<Integer>("day", new PropertyModel<Integer>(this, "day"), Integer.class,
                new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return getString("day.placeholder");
                    }
                }, Date2Panel.class.getSimpleName() + ".day");
        dayField.getTextField().setLabel(labelModel).add(new MinimumValidator<Integer>(1)).add(new MaximumValidator<Integer>(31)).
                add(AttributeModifier.replace("size", String.valueOf(2))).
                add(AttributeModifier.replace("maxlength", String.valueOf(2)));
        add(dayField);

        monthField = new MonthDropDownChoice("month", new PropertyModel<Integer>(this, "month")) {

            @Override
            public String getValidatorKeyPrefix() {
                return Date2Panel.class.getSimpleName() + ".month";
            }
        };
        monthField.setNullValid(true).setLabel(labelModel);
        add(monthField);

        yearField = new HintTextFieldPanel<Integer>("year", new PropertyModel<Integer>(this, "year"), Integer.class,
                new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return getString("year.placeholder");
                    }
                }, Date2Panel.class.getSimpleName() + ".year");
        yearField.getTextField().setLabel(labelModel).add(new MinimumValidator<Integer>(1900)).
                add(AttributeModifier.replace("size", String.valueOf(4))).
                add(AttributeModifier.replace("maxlength", String.valueOf(4)));
        add(yearField);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        Date date = getModelObject();
        if (date != null) {
            day = DateUtil.getDay(date);
            month = DateUtil.getMonth(date) + 1;
            year = DateUtil.getYear(date);
        }
    }

    @Override
    protected void convertInput() {
        Integer day = dayField.getTextField().getConvertedInput();
        Integer month = monthField.getConvertedInput();
        Integer year = yearField.getTextField().getConvertedInput();
        if (day != null && month != null && year != null) {
            try {
                Date date = DateUtil.newDate(day, month, year);
                setConvertedInput(date);
            } catch (Exception e) {
                ValidationError error = new ValidationError();
                String simpleName = Classes.simpleName(getType());
                error.addMessageKey("IConverter." + simpleName);
                error.addMessageKey("IConverter");
                error.setVariable("type", simpleName);
                error((IValidationError) error);
                setConvertedInput(null);
            }
        }
    }

    @Override
    public String getInput() {
        return ifnull(dayField.getTextField().getInput()) + "." + ifnull(monthField.getInput()) + "."
                + ifnull(yearField.getTextField().getInput());
    }

    private String ifnull(String value) {
        return Strings.isEmpty(value) ? "-" : value;
    }
}
