/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.dictionary.web.component.type.StringCulturePanel;

/**
 *
 * @author Artem
 */
public class RangeNumbersPanel extends Panel {

    private static class Range implements Serializable {

        Integer from;
        Integer to;
    }

    private static class Entry implements Serializable {

        final List<StringCulture> number;
        final Range range;

        Entry(List<StringCulture> number) {
            this.number = number;
            this.range = null;
        }

        Entry(Range range) {
            this.range = range;
            this.number = null;
        }
    }

    public static class NumbersList implements Serializable {

        private final List<Entry> entries;

        public NumbersList() {
            entries = Lists.newArrayList();
        }

        public void reset(String number) {
            entries.clear();
            addNumber(number);
        }

        private void addNumber(String number) {
            StringCultureBean stringBean = EjbBeanLocator.getBean(StringCultureBean.class);
            List<StringCulture> strings = stringBean.newStringCultures();
            stringBean.getSystemStringCulture(strings).setValue(number);
            entries.add(new Entry(strings));
        }

        private void addRange() {
            entries.add(new Entry(new Range()));
        }

        public List<List<StringCulture>> getNumbers() {
            StringCultureBean stringBean = EjbBeanLocator.getBean(StringCultureBean.class);

            final Map<String, List<StringCulture>> numbersMap = Maps.newLinkedHashMap();
            for (Entry entry : entries) {
                if (entry.number != null) {
                    final String systemNumber = stringBean.getSystemStringCulture(entry.number).getValue();
                    numbersMap.put(systemNumber, entry.number);
                } else {
                    for (int number = entry.range.from, to = entry.range.to; number <= to; number++) {
                        final String systemNumber = String.valueOf(number);
                        if (!numbersMap.containsKey(systemNumber)) {
                            final List<StringCulture> strings = stringBean.newStringCultures();
                            stringBean.getSystemStringCulture(strings).setValue(systemNumber);
                            numbersMap.put(systemNumber, strings);
                        }
                    }
                }
            }
            return ImmutableList.copyOf(numbersMap.values());
        }

        public String asString(List<StringCulture> strings) {
            StringCultureBean stringBean = EjbBeanLocator.getBean(StringCultureBean.class);
            LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);

            final StringBuilder builder = new StringBuilder(stringBean.getSystemStringCulture(strings).getValue());

            final List<String> otherLanguageValues = Lists.newLinkedList();
            for (StringCulture string : strings) {
                if (!localeBean.getSystemLocaleObject().getId().equals(string.getLocaleId())
                        && !Strings.isEmpty(string.getValue())) {
                    otherLanguageValues.add(string.getValue());
                }
            }

            if (!otherLanguageValues.isEmpty()) {
                builder.append(" (");

                String prefix = null;
                for (String value : otherLanguageValues) {
                    if (prefix != null) {
                        builder.append(prefix);
                    } else {
                        prefix = ", ";
                    }
                    builder.append(value);
                }

                builder.append(")");
            }

            return builder.toString();
        }

        public String asString() {
            final StringBuilder builder = new StringBuilder();

            for (Entry entry : entries) {
                if (entry.number != null) {
                    builder.append(asString(entry.number));
                } else {
                    builder.append(entry.range.from).append(" - ").append(entry.range.to);
                }
                builder.append(", ");
            }

            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }
            return builder.toString();
        }
    }

    private class NumberPanel extends Panel {

        NumberPanel(String id, IModel<List<StringCulture>> numberModel, IModel<String> labelModel) {
            super(id);
            add(new Label("label", labelModel));
            add(new StringCulturePanel("input", numberModel, true, labelModel, true));
        }
    }

    private class RangePanel extends FormComponentPanel<Range> {

        Integer from;
        Integer to;
        final TextField<Integer> fromField;
        final TextField<Integer> toField;

        RangePanel(String id, IModel<Range> range) {
            super(id, range);

            fromField = new TextField<Integer>("from", new PropertyModel<Integer>(this, "from"), Integer.class);
            fromField.add(new MinimumValidator<Integer>(0));
            add(fromField);

            toField = new TextField<Integer>("to", new PropertyModel<Integer>(this, "to"), Integer.class);
            toField.add(new MinimumValidator<Integer>(0));
            add(toField);

            add(new IValidator<Range>() {

                @Override
                public void validate(IValidatable<Range> validatable) {
                    final Range range = validatable.getValue();
                    if (range.from == null || range.to == null) {
                        validatable.error(new ValidationError().addMessageKey("RangeRequiredError"));
                    } else if (range.from > range.to) {
                        validatable.error(new ValidationError().addMessageKey("RangeValidationError").
                                setVariable("from", range.from).
                                setVariable("to", range.to));
                    }
                }
            });
        }

        @Override
        protected void onBeforeRender() {
            super.onBeforeRender();
            final Range range = getModelObject();
            if (range != null) {
                from = range.from;
                to = range.to;
            }
        }

        @Override
        protected void convertInput() {
            final Integer rawFrom = fromField.getConvertedInput();
            final Integer rawTo = toField.getConvertedInput();
            final Range range = getModelObject();
            range.from = rawFrom;
            range.to = rawTo;
            setConvertedInput(range);
        }
    }
    private static final String NUMBER_CONTAINER_ID = "number";
    private static final String RANGE_CONTAINER_ID = "range";
    private FeedbackPanel messages;
    private final NumbersList numbersList;

    public RangeNumbersPanel(String id, final IModel<String> labelModel, final NumbersList numbersList) {
        super(id);
        this.numbersList = numbersList;

        final WebMarkupContainer numbersContainer = new WebMarkupContainer("numbersContainer");
        numbersContainer.setOutputMarkupId(true);
        add(numbersContainer);

        final List<Entry> entries = numbersList.entries;

        if (entries.isEmpty()) {
            numbersList.addNumber(null);
        }

        numbersContainer.add(new AjaxRemovableListView<Entry>("numbers", entries) {

            @Override
            protected void populateItem(ListItem<Entry> item) {
                final Entry entry = item.getModelObject();

                Component number;
                if (entry.number != null) {
                    number = new NumberPanel(NUMBER_CONTAINER_ID, new ListModel<StringCulture>(entry.number), labelModel);
                } else {
                    number = new EmptyPanel(NUMBER_CONTAINER_ID);
                }
                item.add(number);

                Component range;
                if (entry.range != null) {
                    range = new RangePanel(RANGE_CONTAINER_ID, new PropertyModel<Range>(entry, "range"));
                } else {
                    range = new EmptyPanel(RANGE_CONTAINER_ID);
                }
                item.add(range);

                addRemoveSubmitLink("remove", null, item, null, numbersContainer, getMessages());
            }
        });

        add(new AjaxSubmitLink("addNumber") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                numbersList.addNumber(null);
                target.add(numbersContainer);
                target.add(getMessages());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(getMessages());
            }
        });

        add(new AjaxSubmitLink("addRange") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                numbersList.addRange();
                target.add(numbersContainer);
                target.add(getMessages());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(getMessages());
            }
        });
    }

    public boolean validate() {
        if (numbersList.entries.isEmpty()) {
            error(getString("RequiredError"));
            return false;
        }
        return true;
    }

    @Override
    protected void onBeforeRender() {
        if (messages == null) {
            messages = initializeMessages();
        }
        super.onBeforeRender();
    }

    private FeedbackPanel findFeedbackPanel() {
        DomainObjectEditPanel editPanel = findParent(DomainObjectEditPanel.class);
        return editPanel.getMessages();
    }

    protected FeedbackPanel initializeMessages() {
        return findFeedbackPanel();
    }

    protected FeedbackPanel getMessages() {
        return messages;
    }
}
