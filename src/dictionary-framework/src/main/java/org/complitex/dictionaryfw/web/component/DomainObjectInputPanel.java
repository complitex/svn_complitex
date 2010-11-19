/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.converter.*;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.SimpleTypes;
import org.complitex.dictionaryfw.entity.StringCulture;
import org.complitex.dictionaryfw.entity.description.Entity;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.description.EntityType;
import org.complitex.dictionaryfw.service.EntityBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.CanEditUtil;
import org.complitex.dictionaryfw.web.DictionaryFwSession;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentSessionState;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.*;
import org.apache.wicket.markup.html.form.DropDownChoice;

/**
 *
 * @author Artem
 */
public class DomainObjectInputPanel extends Panel {

    private static class SimpleTypeModel<T extends Serializable> extends Model<T> {

        private StringCulture systemLocaleStringCulture;

        private IConverter<T> converter;

        public SimpleTypeModel(StringCulture systemLocaleStringCulture, IConverter<T> converter) {
            this.systemLocaleStringCulture = systemLocaleStringCulture;
            this.converter = converter;
        }

        @Override
        public T getObject() {
            if (!Strings.isEmpty(systemLocaleStringCulture.getValue())) {
                return converter.toObject(systemLocaleStringCulture.getValue());
            }
            return null;
        }

        @Override
        public void setObject(T object) {
            if (object != null) {
                systemLocaleStringCulture.setValue(converter.toString(object));
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DomainObjectInputPanel.class);

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;

    @EJB(name = "EntityBean")
    private EntityBean entityBean;

    private SearchComponentState searchComponentState;

    private String entity;

    private DomainObject object;

    private Long parentId;

    private String parentEntity;

    private Date date;

    private DisableAwareDropDownChoice<EntityType> types;

    /**
     * For use in history components
     * @param id
     * @param object
     * @param entity
     * @param parentId
     * @param parentEntity
     * @param date
     */
    public DomainObjectInputPanel(String id, DomainObject object, String entity, Long parentId, String parentEntity, Date date) {
        super(id);
        this.object = object;
        this.entity = entity;
        this.parentId = parentId;
        this.parentEntity = parentEntity;
        this.date = date;
        init();
    }

    /**
     * For use in non-history components
     * @param id
     * @param object
     * @param entity
     * @param parentId
     * @param parentEntity
     */
    public DomainObjectInputPanel(String id, DomainObject object, String entity, Long parentId, String parentEntity) {
        super(id);
        this.object = object;
        this.entity = entity;
        this.parentId = parentId;
        this.parentEntity = parentEntity;
        init();
    }

    public Date getDate() {
        return date;
    }

    private boolean isHistory() {
        return date != null;
    }

    private boolean fromParent() {
        return parentId != null && !Strings.isEmpty(parentEntity);
    }

    private Strategy getStrategy() {
        return strategyFactory.getStrategy(entity);
    }

    public DomainObject getObject() {
        return object;
    }

    public DropDownChoice<EntityType> getSelectType() {
        return types;
    }

    private void init() {
//        final Entity description = isHistory() ? entityBean.getEntity(entity) : getStrategy().getEntity();
        final Entity description = getStrategy().getEntity();

        //entity type
        WebMarkupContainer typeContainer = new WebMarkupContainer("typeContainer");
        add(typeContainer);
        List<EntityType> allEntityTypes = description.getEntityTypes() != null ? description.getEntityTypes() : new ArrayList<EntityType>();

        final List<EntityType> entityTypes;
        List<EntityType> liveEntityTypes = Lists.newArrayList(Iterables.filter(allEntityTypes, new Predicate<EntityType>() {

            @Override
            public boolean apply(EntityType entityType) {
                return entityType.getEndDate() == null;
            }
        }));
        if (object.getEntityTypeId() != null) {
            EntityType entityType = Iterables.find(allEntityTypes, new Predicate<EntityType>() {

                @Override
                public boolean apply(EntityType type) {
                    return object.getEntityTypeId().equals(type.getId());
                }
            });
            if (entityType.getEndDate() == null) {
                entityTypes = liveEntityTypes;
            } else {
                entityTypes = allEntityTypes;
            }
        } else {
            entityTypes = liveEntityTypes;
        }

        if (entityTypes.isEmpty()) {
            typeContainer.setVisible(false);
        }
        IModel<EntityType> typeModel = new Model<EntityType>() {

            @Override
            public void setObject(EntityType entityType) {
                object.setEntityTypeId(entityType.getId());
            }

            @Override
            public EntityType getObject() {
                if (object.getEntityTypeId() != null) {
                    return Iterables.find(entityTypes, new Predicate<EntityType>() {

                        @Override
                        public boolean apply(EntityType entityType) {
                            return entityType.getId().equals(object.getEntityTypeId());
                        }
                    });
                } else {
                    return null;
                }
            }
        };
        IDisableAwareChoiceRenderer<EntityType> renderer = new IDisableAwareChoiceRenderer<EntityType>() {

            @Override
            public boolean isDisabled(EntityType object) {
                return object.getEndDate() != null;
            }

            @Override
            public Object getDisplayValue(EntityType object) {
                return stringBean.displayValue(object.getEntityTypeNames(), getLocale());
            }

            @Override
            public String getIdValue(EntityType object, int index) {
                return String.valueOf(object.getId());
            }
        };
        types = new DisableAwareDropDownChoice<EntityType>("types", typeModel, entityTypes, renderer);
        types.setLabel(new ResourceModel("entity_type"));
        types.setRequired(true);
        types.setEnabled(!isHistory() && CanEditUtil.canEdit(object));
        typeContainer.add(types);


        //simple attributes
        final Map<Attribute, EntityAttributeType> attrToTypeMap = Maps.newLinkedHashMap();
        for (Attribute attr : object.getAttributes()) {
            EntityAttributeType attrType = description.getAttributeType(attr.getAttributeTypeId());
                if(getStrategy().isSimpleAttributeType(attrType)){
                    attrToTypeMap.put(attr, attrType);
                }
//            try {
//                EntityAttributeType attrDesc = Iterables.find(description.getEntityAttributeTypes(), new Predicate<EntityAttributeType>() {
//
//                    @Override
//                    public boolean apply(EntityAttributeType attrDesc) {
//                        return attrDesc.getId().equals(attr.getAttributeTypeId()) && getStrategy().isSimpleAttributeType(attrDesc);
//                    }
//                });
//                attrToTypeMap.put(attr, attrDesc);
//            } catch (NoSuchElementException e) {
//            }
        }

        ListView<Attribute> simpleAttributes = new ListView<Attribute>("simpleAttributes", Lists.newArrayList(attrToTypeMap.keySet())) {

            @Override
            protected void populateItem(ListItem<Attribute> item) {
                Attribute attr = item.getModelObject();
                final EntityAttributeType attrType = attrToTypeMap.get(attr);

                IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return stringBean.displayValue(attrType.getAttributeNames(), getLocale());
                    }
                };
                item.add(new Label("label", labelModel));

                WebMarkupContainer required = new WebMarkupContainer("required");
                item.add(required);
                required.setVisible(attrType.isMandatory());

                String valueType = attrType.getEntityAttributeValueTypes().get(0).getValueType();
                SimpleTypes type = SimpleTypes.valueOf(valueType.toUpperCase());

                Component input = null;
                final StringCulture systemLocaleStringCulture = stringBean.getSystemStringCulture(attr.getLocalizedValues());
                switch (type) {
                    case STRING: {
                        IConverter<String> stringConverter = new IConverter<String>() {

                            @Override
                            public String toString(String object) {
                                return object;
                            }

                            @Override
                            public String toObject(String value) {
                                return value;
                            }
                        };
                        IModel<String> model = new SimpleTypeModel<String>(systemLocaleStringCulture, stringConverter);
                        input = new StringPanel("input", model, attrType.isMandatory(), labelModel, !isHistory() && CanEditUtil.canEdit(object));
                    }
                    break;
                    case STRING_CULTURE: {
                        IModel<List<StringCulture>> model = new PropertyModel<List<StringCulture>>(attr, "localizedValues");
                        input = new StringCulturePanel("input", model, attrType.isMandatory(), labelModel, !isHistory() && CanEditUtil.canEdit(object));
                    }
                    break;
                    case INTEGER: {
                        IModel<Integer> model = new SimpleTypeModel<Integer>(systemLocaleStringCulture, new IntegerConverter());
                        input = new IntegerPanel("input", model, attrType.isMandatory(), labelModel, !isHistory() && CanEditUtil.canEdit(object));
                    }
                    break;
                    case DATE: {
                        IModel<Date> model = new SimpleTypeModel<Date>(systemLocaleStringCulture, new DateConverter());
                        input = new DatePanel("input", model, attrType.isMandatory(), labelModel, !isHistory() && CanEditUtil.canEdit(object));
                    }
                    break;
                    case BOOLEAN: {
                        IModel<Boolean> model = new SimpleTypeModel<Boolean>(systemLocaleStringCulture, new BooleanConverter());
                        input = new BooleanPanel("input", model, labelModel, !isHistory() && CanEditUtil.canEdit(object));
                    }
                    break;
                    case DOUBLE: {
                        IModel<Double> model = new SimpleTypeModel<Double>(systemLocaleStringCulture, new DoubleConverter());
                        input = new DoublePanel("input", model, attrType.isMandatory(), labelModel, !isHistory() && CanEditUtil.canEdit(object));
                    }
                    break;
                }

                item.add(input);
            }
        };
        simpleAttributes.setReuseItems(true);
        add(simpleAttributes);
        searchComponentState = initParentSearchComponentState();

        WebMarkupContainer parentContainer = new WebMarkupContainer("parentContainer");
        add(parentContainer);
        List<String> parentFilters = getStrategy().getParentSearchFilters();
        ISearchCallback parentSearchCallback = getStrategy().getParentSearchCallback();
        if (parentFilters == null || parentFilters.isEmpty() || parentSearchCallback == null) {
            parentContainer.setVisible(false);
            parentContainer.add(new EmptyPanel("parentSearch"));
        } else {
            SearchComponent parentSearchComponent = new SearchComponent("parentSearch", getParentSearchComponentState(), parentFilters,
                    parentSearchCallback, !isHistory() && CanEditUtil.canEdit(object));
            parentContainer.add(parentSearchComponent);
            parentSearchComponent.invokeCallback();
        }

        //complex attributes
        AbstractComplexAttributesPanel complexAttributes = null;
        Class<? extends AbstractComplexAttributesPanel> clazz = getStrategy().getComplexAttributesPanelClass();
        if (clazz != null) {
            try {
                complexAttributes = clazz.getConstructor(String.class, boolean.class).newInstance("complexAttributes", isHistory());
            } catch (Exception e) {
                log.warn("Couldn't instantiate complex attributes panel object.", e);
            }
        }
        if (complexAttributes == null) {
            add(new EmptyPanel("complexAttributes"));
        } else {
            add(complexAttributes);
        }
    }

    protected SearchComponentState initParentSearchComponentState() {
        //parent search
        SearchComponentState componentState = null;
        if (object.getId() == null) {
            if (!fromParent()) {
                componentState = getSearchComponentStateFromSession();
            } else {
                componentState = getStrategy().getSearchComponentStateForParent(parentId, parentEntity, null);
            }
        } else {
            Strategy.RestrictedObjectInfo info = getStrategy().findParentInSearchComponent(object.getId(), isHistory() ? date : null);
            if (info != null) {
                componentState = getStrategy().getSearchComponentStateForParent(info.getId(), info.getEntityTable(), date);
            }
        }
        return componentState;
    }

    public boolean validateParent() {
        if (!(getStrategy().getParentSearchFilters() == null
                || getStrategy().getParentSearchFilters().isEmpty()
                || getStrategy().getParentSearchCallback() == null)) {
            if ((object.getParentId() == null) || (object.getParentEntityId() == null)) {
                error(getString("parent_required"));
                return false;
            }
        }
        return true;
    }

    public SearchComponentState getParentSearchComponentState() {
        return searchComponentState;
    }

    protected DictionaryFwSession getDictionaryFwSession() {
        return (DictionaryFwSession) getSession();
    }

    protected SearchComponentState getSearchComponentStateFromSession() {
        SearchComponentSessionState searchComponentSessionState = getDictionaryFwSession().getSearchComponentSessionState();
        SearchComponentState componentState = searchComponentSessionState.get(entity);
        if (componentState == null) {
            componentState = new SearchComponentState();
            searchComponentSessionState.put(entity, componentState);
        }
        return componentState;
    }
}
