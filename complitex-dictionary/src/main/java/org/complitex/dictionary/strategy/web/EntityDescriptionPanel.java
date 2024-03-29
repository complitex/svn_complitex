/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.*;
import org.complitex.dictionary.entity.SimpleTypes;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityAttributeValueType;
import org.complitex.dictionary.service.EntityBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.type.StringCulturePanel;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;

import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public class EntityDescriptionPanel extends Panel {

    private static final String DATE_FORMAT = "HH:mm dd.MM.yyyy";
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private EntityBean entityBean;

    public EntityDescriptionPanel(String id, String entity, PageParameters pageParameters) {
        super(id);
        init(entity, pageParameters);
    }

    private void init(final String entity, final PageParameters params) {
        final Entity oldEntity = entityBean.getEntity(entity);
        final Entity description = CloneUtil.cloneObject(oldEntity);

        IModel<String> entityLabelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(description.getEntityNames(), getLocale());
            }
        };
        IModel<String> labelModel = new StringResourceModel("label", null, new Object[]{entityLabelModel});
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        add(new FeedbackPanel("messages"));

        Form form = new Form("form");
        add(form);

        //attributes
        final WebMarkupContainer attributesContainer = new WebMarkupContainer("attributesContainer");
        attributesContainer.setOutputMarkupId(true);
        form.add(attributesContainer);

        final List<String> supportedValueTypes = Lists.newArrayList(Iterables.transform(Arrays.asList(SimpleTypes.values()), new Function<SimpleTypes, String>() {

            @Override
            public String apply(SimpleTypes valueType) {
                return valueType.name();
            }
        }));

        ListView<EntityAttributeType> attributes = new AjaxRemovableListView<EntityAttributeType>("attributes", description.getEntityAttributeTypes()) {

            @Override
            protected void populateItem(ListItem<EntityAttributeType> item) {
                final EntityAttributeType attributeType = item.getModelObject();
                final List<EntityAttributeValueType> valueTypes = attributeType.getEntityAttributeValueTypes();

                WebMarkupContainer valueTypesContainer = new WebMarkupContainer("valueTypesContainer");
                item.add(valueTypesContainer);
                RepeatingView valueTypeItem = new RepeatingView("valueTypeItem");
                valueTypesContainer.add(valueTypeItem);
                Label valueType = new Label("valueType", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return displayValueType(valueTypes.get(0).getValueType());
                    }
                });
                item.add(valueType);

                DropDownChoice<String> valueTypeSelect = new DropDownChoice<String>("valueTypeSelect",
                        new PropertyModel<String>(attributeType.getEntityAttributeValueTypes().get(0), "valueType"), supportedValueTypes);
                valueTypeSelect.setRequired(true);
                valueTypeSelect.setLabel(new ResourceModel("attribute_value_type"));
                valueTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update model
                    }
                });
                item.add(valueTypeSelect);

                Label mandatoryLabel = new Label("mandatoryLabel", new ResourceModel(attributeType.isMandatory() ? "yes" : "no"));
                item.add(mandatoryLabel);

                CheckBox mandatoryInput = new CheckBox("mandatoryInput", new PropertyModel<Boolean>(attributeType, "mandatory"));
                mandatoryInput.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update model
                    }
                });
                item.add(mandatoryInput);

                item.add(new Label("startDate", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        if (attributeType.isSystem()) {
                            return getString("built-in");
                        } else if (attributeType.getId() != null) {
                            return new SimpleDateFormat(DATE_FORMAT, getLocale()).format(attributeType.getStartDate());
                        } else {
                            return null;
                        }
                    }
                }));
                item.add(new Label("endDate", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        if (attributeType.getEndDate() == null) {
                            return null;
                        } else {
                            return new SimpleDateFormat(DATE_FORMAT, getLocale()).format(attributeType.getEndDate());
                        }
                    }
                }));

                if (attributeType.getId() != null) { // old attribute
                    item.add(new Label("name", new AbstractReadOnlyModel<String>() {

                        @Override
                        public String getObject() {
                            return stringBean.displayValue(attributeType.getAttributeNames(), getLocale());
                        }
                    }));

                    if (valueTypes.size() == 1) {
                        valueTypesContainer.setVisible(false);
                    } else {
                        valueType.setVisible(false);
                        for (final EntityAttributeValueType currentValueType : valueTypes) {
                            valueTypeItem.add(new Label(String.valueOf(currentValueType.getId()), new AbstractReadOnlyModel<String>() {

                                @Override
                                public String getObject() {
                                    return displayValueType(currentValueType.getValueType());
                                }
                            }));
                        }
                    }
                    valueTypeSelect.setVisible(false);
                    mandatoryInput.setVisible(false);
                } else {
                    //new attribute
                    item.add(new StringCulturePanel("name", new PropertyModel<List<StringCulture>>(attributeType, "attributeNames"), true,
                            new ResourceModel("attribute_name"), true, new MarkupContainer[0]));

                    valueType.setVisible(false);
                    valueTypesContainer.setVisible(false);
                    mandatoryLabel.setVisible(false);
                }

                addRemoveLink("remove", item, null, attributesContainer).setVisible(!attributeType.isSystem() && (attributeType.getEndDate() == null));
            }
        };
        attributes.setReuseItems(true);
        attributesContainer.add(attributes);

        AjaxLink addAttribute = new AjaxLink("addAttribute") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                EntityAttributeType attributeType = entityBean.newAttributeType();
                attributeType.getEntityAttributeValueTypes().add(new EntityAttributeValueType());
                description.getEntityAttributeTypes().add(attributeType);
                target.add(attributesContainer);
            }
        };
        form.add(addAttribute);

        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                save(oldEntity, description);
                setResponsePage(getPage().getClass(), params);
            }
        };
        form.add(submit);

        final String[] parents = strategyFactory.getStrategy(entity).getParents();
        WebMarkupContainer parentsContainer = new WebMarkupContainer("parentsContainer");
        Label parentsInfo = new Label("parentsInfo", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                if ((parents != null) && (parents.length > 0)) {
                    return displayParents(parents);
                }
                return null;
            }
        });
        parentsContainer.add(parentsInfo);
        add(parentsContainer);
        if (parents == null || parents.length == 0) {
            parentsContainer.setVisible(false);
        }
    }

    private void save(Entity oldEntity, Entity newEntity) {
        entityBean.save(oldEntity, newEntity);
    }

    private String displayParents(String[] parents) {
        StringBuilder parentsLabel = new StringBuilder();

        for (int i = 0; i < parents.length; i++) {
            IStrategy parentStrategy = strategyFactory.getStrategy(parents[i]);
            parentsLabel.append("'").
                    append(stringBean.displayValue(parentStrategy.getEntity().getEntityNames(), getLocale())).
                    append("'");
            if (i < parents.length - 1) {
                parentsLabel.append(", ");
            }
        }
        return parentsLabel.toString();
    }

    private String displayValueType(String valueType) {
        if (SimpleTypes.isSimpleType(valueType)) {
            return valueType;
        } else {
            IStrategy referenceEntityStrategy = strategyFactory.getStrategy(valueType, true);
            if (referenceEntityStrategy == null) {
                return new StringResourceModel("reference_table", this, null, new Object[]{valueType.toUpperCase()}).getObject();
            } else {
                return new StringResourceModel("reference", this, null, new Object[]{
                            stringBean.displayValue(referenceEntityStrategy.getEntity().getEntityNames(), getLocale())
                        }).getObject();
            }
        }
    }
}
