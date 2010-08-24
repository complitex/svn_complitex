///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.complitex.dictionaryfw.strategy.web;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;
//import java.util.Arrays;
//import java.util.List;
//import javax.ejb.EJB;
//import org.apache.wicket.PageParameters;
//import org.apache.wicket.markup.html.WebPage;
//import org.apache.wicket.markup.html.basic.Label;
//import org.apache.wicket.markup.html.form.Button;
//import org.apache.wicket.markup.html.form.CheckBox;
//import org.apache.wicket.markup.html.form.DropDownChoice;
//import org.apache.wicket.markup.html.form.Form;
//import org.apache.wicket.markup.html.link.Link;
//import org.apache.wicket.markup.html.panel.FeedbackPanel;
//import org.apache.wicket.markup.html.panel.Panel;
//import org.apache.wicket.model.AbstractReadOnlyModel;
//import org.apache.wicket.model.IModel;
//import org.apache.wicket.model.PropertyModel;
//import org.apache.wicket.model.ResourceModel;
//import org.apache.wicket.model.StringResourceModel;
//import org.complitex.dictionaryfw.dao.EntityBean;
//import org.complitex.dictionaryfw.dao.StringCultureBean;
//import org.complitex.dictionaryfw.entity.SimpleTypes;
//import org.complitex.dictionaryfw.entity.StringCulture;
//import org.complitex.dictionaryfw.entity.description.Entity;
//import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
//import org.complitex.dictionaryfw.entity.description.EntityAttributeValueType;
//import org.complitex.dictionaryfw.strategy.Strategy;
//import org.complitex.dictionaryfw.strategy.StrategyFactory;
//import org.complitex.dictionaryfw.web.component.StringCulturePanel;
//
///**
// *
// * @author Artem
// */
//public final class EntityAttributeEditPanel extends Panel {
//
//    @EJB(name = "StrategyFactory")
//    private StrategyFactory strategyFactory;
//
//    @EJB(name = "StringCultureBean")
//    private StringCultureBean stringBean;
//
//    @EJB(name = "EntityBean")
//    private EntityBean entityBean;
//
//    public EntityAttributeEditPanel(String id, String entity, Class<? extends WebPage> entityDescriptionPage,
//            PageParameters entityDescriptionPageParams) {
//        super(id);
//        init(entity, entityDescriptionPage, entityDescriptionPageParams);
//    }
//
//    private Strategy getStrategy(String entity) {
//        return strategyFactory.getStrategy(entity);
//    }
//
//    private void init(String entity, final Class<? extends WebPage> entityDescriptionPage, final PageParameters entityDescriptionPageParams) {
//        final Entity description = getStrategy(entity).getEntity();
//        final EntityAttributeType attributeType = entityBean.newAttributeType();
//        attributeType.getEntityAttributeValueTypes().add(new EntityAttributeValueType());
//
//        IModel<String> entityLabelModel = new AbstractReadOnlyModel<String>() {
//
//            @Override
//            public String getObject() {
//                return stringBean.displayValue(description.getEntityNames(), getLocale());
//            }
//        };
//        IModel<String> labelModel = new StringResourceModel("label", null, new Object[]{entityLabelModel});
//        add(new Label("title", labelModel));
//        add(new Label("label", labelModel));
//
//        add(new FeedbackPanel("messages"));
//
//        Form form = new Form("form");
//        add(form);
//
//        StringCulturePanel name = new StringCulturePanel("name", new PropertyModel<List<StringCulture>>(attributeType, "attributeNames"),
//                true, new ResourceModel("name"), true);
//        form.add(name);
//
//        List<String> valueTypes = Lists.newArrayList(Iterables.transform(Arrays.asList(SimpleTypes.values()), new Function<SimpleTypes, String>() {
//
//            @Override
//            public String apply(SimpleTypes valueType) {
//                return valueType.name();
//            }
//        }));
//        DropDownChoice<String> valueType = new DropDownChoice<String>("valueType",
//                new PropertyModel<String>(attributeType.getEntityAttributeValueTypes().get(0), "valueType"), valueTypes);
//        valueType.setRequired(true);
//        valueType.setLabel(new ResourceModel("value_type"));
//        form.add(valueType);
//
//        CheckBox mandatory = new CheckBox("mandatory", new PropertyModel<Boolean>(attributeType, "mandatory"));
//        form.add(mandatory);
//
//        Button submit = new Button("submit") {
//
//            @Override
//            public void onSubmit() {
//                entityBean.insertAttributeType(attributeType, description.getId());
//                back(entityDescriptionPage, entityDescriptionPageParams);
//            }
//        };
//        form.add(submit);
//        Link cancel = new Link("cancel") {
//
//            @Override
//            public void onClick() {
//                back(entityDescriptionPage, entityDescriptionPageParams);
//            }
//        };
//        form.add(cancel);
//    }
//
//    private void back(Class<? extends WebPage> entityDescriptionPage, PageParameters entityDescriptionPageParams) {
//        setResponsePage(entityDescriptionPage, entityDescriptionPageParams);
//    }
//}
