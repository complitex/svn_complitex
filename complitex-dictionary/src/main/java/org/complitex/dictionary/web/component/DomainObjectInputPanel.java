package org.complitex.dictionary.web.component;

import com.google.common.base.Predicate;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.converter.*;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.description.EntityType;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.IStrategy.SimpleObjectInfo;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.complitex.dictionary.strategy.web.DomainObjectAccessUtil.canEdit;

/**
 *
 * @author Artem
 */
public class DomainObjectInputPanel extends Panel {

    public static class SimpleTypeModel<T extends Serializable> extends Model<T> {

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
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StringCultureBean stringBean;
    private SearchComponentState searchComponentState;
    private String entity;
    private String strategyName;
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
    public DomainObjectInputPanel(String id, DomainObject object, String entity, String strategyName, Long parentId,
            String parentEntity, Date date) {
        super(id);
        this.object = object;
        this.entity = entity;
        this.strategyName = strategyName;
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
    public DomainObjectInputPanel(String id, DomainObject object, String entity, String strategyName, Long parentId, String parentEntity) {
        super(id);
        this.object = object;
        this.entity = entity;
        this.strategyName = strategyName;
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

    private IStrategy getStrategy() {
        return strategyFactory.getStrategy(strategyName, entity);
    }

    public DomainObject getObject() {
        return object;
    }

    public DropDownChoice<EntityType> getSelectType() {
        return types;
    }

    private void init() {
        final Entity description = getStrategy().getEntity();

        //entity type
        WebMarkupContainer typeContainer = new WebMarkupContainer("typeContainer");
        add(typeContainer);
        List<EntityType> allEntityTypes = description.getEntityTypes() != null ? description.getEntityTypes() : new ArrayList<EntityType>();

        final List<EntityType> entityTypes;
        List<EntityType> liveEntityTypes = newArrayList(filter(allEntityTypes, new Predicate<EntityType>() {

            @Override
            public boolean apply(EntityType entityType) {
                return entityType.getEndDate() == null;
            }
        }));
        if (object.getEntityTypeId() != null) {
            EntityType entityType = find(allEntityTypes, new Predicate<EntityType>() {

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
                    return find(entityTypes, new Predicate<EntityType>() {

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
        types.setEnabled(!isHistory() && canEdit(strategyName, entity, object));
        typeContainer.add(types);


        //simple attributes
        final Map<Attribute, EntityAttributeType> attrToTypeMap = newLinkedHashMap();
        for (Attribute attr : object.getAttributes()) {
            EntityAttributeType attrType = description.getAttributeType(attr.getAttributeTypeId());
            if (getStrategy().isSimpleAttributeType(attrType)) {
                attrToTypeMap.put(attr, attrType);
            }
        }

        ListView<Attribute> simpleAttributes = new ListView<Attribute>("simpleAttributes", newArrayList(attrToTypeMap.keySet())) {

            @Override
            protected void populateItem(ListItem<Attribute> item) {
                Attribute attr = item.getModelObject();
                final EntityAttributeType attributeType = attrToTypeMap.get(attr);

                IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return stringBean.displayValue(attributeType.getAttributeNames(), getLocale());
                    }
                };
                item.add(new Label("label", labelModel));

                WebMarkupContainer required = new WebMarkupContainer("required");
                item.add(required);
                required.setVisible(attributeType.isMandatory());

                String valueType = attributeType.getEntityAttributeValueTypes().get(0).getValueType();
                SimpleTypes type = SimpleTypes.valueOf(valueType.toUpperCase());

                Component input = null;
                final StringCulture systemLocaleStringCulture = stringBean.getSystemStringCulture(attr.getLocalizedValues());
                switch (type) {
                    case STRING: {
                        IModel<String> model = new SimpleTypeModel<String>(systemLocaleStringCulture, new StringConverter());
                        input = new StringPanel("input", model, attributeType.isMandatory(), labelModel, !isHistory()
                                && canEdit(strategyName, entity, object));
                    }
                    break;
                    case BIG_STRING: {
                        IModel<String> model = new SimpleTypeModel<String>(systemLocaleStringCulture, new StringConverter());
                        input = new BigStringPanel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case STRING_CULTURE: {
                        IModel<List<StringCulture>> model = new PropertyModel<List<StringCulture>>(attr, "localizedValues");
                        input = new StringCulturePanel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case INTEGER: {
                        IModel<Integer> model = new SimpleTypeModel<Integer>(systemLocaleStringCulture, new IntegerConverter());
                        input = new IntegerPanel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case DATE: {
                        IModel<Date> model = new SimpleTypeModel<Date>(systemLocaleStringCulture, new DateConverter());
                        input = new DatePanel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case DATE2: {
                        IModel<Date> model = new SimpleTypeModel<Date>(systemLocaleStringCulture, new DateConverter());
                        input = new Date2Panel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case BOOLEAN: {
                        IModel<Boolean> model = new SimpleTypeModel<Boolean>(systemLocaleStringCulture, new BooleanConverter());
                        input = new BooleanPanel("input", model, labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case DOUBLE: {
                        IModel<Double> model = new SimpleTypeModel<Double>(systemLocaleStringCulture, new DoubleConverter());
                        input = new DoublePanel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
                    }
                    break;
                    case GENDER: {
                        IModel<Gender> model = new SimpleTypeModel<Gender>(systemLocaleStringCulture, new GenderConverter());
                        input = new GenderPanel("input", model, attributeType.isMandatory(), labelModel,
                                !isHistory() && canEdit(strategyName, entity, object));
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
                    parentSearchCallback, ShowMode.ACTIVE,
                    !isHistory() && canEdit(strategyName, entity, object));
            parentContainer.add(parentSearchComponent);
            parentSearchComponent.invokeCallback();
        }

        //complex attributes
        //before simple attributes:
        addComplexAttributesPanel("complexAttributesBefore", getStrategy().getComplexAttributesPanelBeforeClass());

        //after simple attributes:
        addComplexAttributesPanel("complexAttributesAfter", getStrategy().getComplexAttributesPanelAfterClass());
    }

    protected void addComplexAttributesPanel(String id, Class<? extends AbstractComplexAttributesPanel> complexAttributesPanelClass) {
        AbstractComplexAttributesPanel complexAttributes = null;
        if (complexAttributesPanelClass != null) {
            try {
                complexAttributes = complexAttributesPanelClass.getConstructor(String.class, boolean.class).newInstance(id, isHistory());
            } catch (Exception e) {
                log.error("Couldn't instantiate complex attributes panel object.", e);
            }
        }
        if (complexAttributes == null) {
            add(new EmptyPanel(id));
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
                boolean checkEnable = getStrategy().checkEnable(componentState);
                if (!checkEnable) {
                    componentState = new SearchComponentState();
                    updateSearchComponentSessionState(componentState);
                }

            } else {
                componentState = getStrategy().getSearchComponentStateForParent(parentId, parentEntity, null);
            }
        } else {
            SimpleObjectInfo info = getStrategy().findParentInSearchComponent(object.getId(), isHistory() ? date : null);
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
        Map<String, SearchComponentState> searchComponentSessionState = getDictionaryFwSession().getSearchComponentSessionState();

        SearchComponentState componentState = searchComponentSessionState.get(entity);
        if (componentState == null) {
            componentState = new SearchComponentState();
            searchComponentSessionState.put(entity, componentState);
        }
        return componentState;
    }

    protected void updateSearchComponentSessionState(SearchComponentState componentState) {
        Map<String, SearchComponentState> searchComponentSessionState = getDictionaryFwSession().getSearchComponentSessionState();

        searchComponentSessionState.put(entity, componentState);
    }
}
