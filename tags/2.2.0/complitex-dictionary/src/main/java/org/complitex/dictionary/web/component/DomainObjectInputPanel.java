package org.complitex.dictionary.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.complitex.dictionary.converter.*;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.IStrategy.SimpleObjectInfo;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.web.component.search.CollapsibleInputSearchComponent;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.type.*;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.apache.wicket.util.string.Strings.isEmpty;
import static org.complitex.dictionary.strategy.web.DomainObjectAccessUtil.canEdit;
import static org.complitex.dictionary.util.EjbBeanLocator.getBean;

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
            if (!isEmpty(systemLocaleStringCulture.getValue())) {
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

    public static final String INPUT_COMPONENT_ID = "input";
    @EJB
    private StrategyFactory strategyFactory;
    private SearchComponentState searchComponentState;
    private String entity;
    private String strategyName;
    private DomainObject object;
    private Long parentId;
    private String parentEntity;
    private Date date;
    private final Entity description;

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
        this.description = getStrategy().getEntity();
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
    public DomainObjectInputPanel(String id, DomainObject object, String entity, String strategyName, Long parentId,
            String parentEntity) {
        super(id);
        this.object = object;
        this.entity = entity;
        this.strategyName = strategyName;
        this.parentId = parentId;
        this.parentEntity = parentEntity;
        this.description = getStrategy().getEntity();
        init();
    }

    public Date getDate() {
        return date;
    }

    private boolean isHistory() {
        return date != null;
    }

    public String getParentEntity() {
        return parentEntity;
    }

    public Long getParentId() {
        return parentId;
    }

    private IStrategy getStrategy() {
        return strategyFactory.getStrategy(strategyName, entity);
    }

    public DomainObject getObject() {
        return object;
    }

    private void init() {
        //simple attributes
        ListView<Attribute> simpleAttributes = newSimpleAttributeListView("simpleAttributes");
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
            CollapsibleInputSearchComponent parentSearchComponent = new CollapsibleInputSearchComponent("parentSearch", getParentSearchComponentState(),
                    parentFilters, parentSearchCallback, ShowMode.ACTIVE, !isHistory() && canEdit(strategyName, entity, object)) {

                @Override
                protected void onSelect(AjaxRequestTarget target, String entity) {
                    super.onSelect(target, entity);

                    if (object.getId() == null) {
                        DomainObject parent = getModelObject(entity);
                        if (parent != null && parent.getId() != null && parent.getId() > 0) {
                            DomainObjectEditPanel editPanel = visitParents(DomainObjectEditPanel.class,
                                    new IVisitor<Component, DomainObjectEditPanel>() {

                                        @Override
                                        public void component(Component object, IVisit<DomainObjectEditPanel> visit) {
                                            visit.stop((DomainObjectEditPanel) object);
                                        }
                                    });
                            editPanel.updateParentPermissions(target, parent.getSubjectIds());
                        }
                    }
                }
            };
            parentContainer.add(parentSearchComponent);
            parentSearchComponent.invokeCallback();
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        //before simple attributes:
        addComplexAttributesPanelBefore("complexAttributesBefore");

        //after simple attributes:
        addComplexAttributesPanelAfter("complexAttributesAfter");
    }

    protected void addComplexAttributesPanelBefore(String id) {
        addComplexAttributesPanel(id, getStrategy().getComplexAttributesPanelBeforeClass());
    }

    protected void addComplexAttributesPanelAfter(String id) {
        addComplexAttributesPanel(id, getStrategy().getComplexAttributesPanelAfterClass());
    }

    protected ListView<Attribute> newSimpleAttributeListView(String id) {
        final List<Attribute> simpleAttributes = getSimpleAttributes(object.getAttributes());

        final Map<Attribute, EntityAttributeType> attrToTypeMap = newLinkedHashMap();
        for (Attribute attr : simpleAttributes) {
            EntityAttributeType attrType = description.getAttributeType(attr.getAttributeTypeId());
            attrToTypeMap.put(attr, attrType);
        }

        return new ListView<Attribute>(id, simpleAttributes) {

            @Override
            protected void populateItem(ListItem<Attribute> item) {
                Attribute attr = item.getModelObject();
                final EntityAttributeType attributeType = attrToTypeMap.get(attr);
                item.add(new Label("label", labelModel(attributeType.getAttributeNames(), getLocale())));
                WebMarkupContainer required = new WebMarkupContainer("required");
                item.add(required);
                required.setVisible(attributeType.isMandatory());

                Component input = newInputComponent(entity, strategyName, object, attr, getLocale(), isHistory());
                item.add(input);
            }
        };
    }

    protected List<Attribute> getSimpleAttributes(List<Attribute> allAttributes) {
        final List<Attribute> attributes = newArrayList();
        for (Attribute attribute : allAttributes) {
            EntityAttributeType attrType = description.getAttributeType(attribute.getAttributeTypeId());
            if (getStrategy().isSimpleAttributeType(attrType)) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    private static StringCultureBean stringBean() {
        return getBean(StringCultureBean.class);
    }

    public static IModel<String> labelModel(final List<StringCulture> attributeNames, final Locale locale) {
        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return Strings.capitalize(stringBean().displayValue(attributeNames, locale).toLowerCase(locale));
            }
        };
    }

    public static Component newInputComponent(String entityTable, String strategyName, DomainObject object,
            Attribute attribute, final Locale locale, boolean isHistory) {
        StrategyFactory strategyFactory = getBean(StrategyFactory.class);
        IStrategy strategy = strategyFactory.getStrategy(strategyName, entityTable);
        Entity entity = strategy.getEntity();
        final EntityAttributeType attributeType = entity.getAttributeType(attribute.getAttributeTypeId());
        IModel<String> labelModel = labelModel(attributeType.getAttributeNames(), locale);
        String valueType = attributeType.getEntityAttributeValueTypes().get(0).getValueType();
        SimpleTypes type = SimpleTypes.valueOf(valueType.toUpperCase());
        Component input = null;
        final StringCulture systemLocaleStringCulture = stringBean().getSystemStringCulture(attribute.getLocalizedValues());
        switch (type) {
            case STRING: {
                IModel<String> model = new SimpleTypeModel<String>(systemLocaleStringCulture, new StringConverter());
                input = new StringPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case BIG_STRING: {
                IModel<String> model = new SimpleTypeModel<String>(systemLocaleStringCulture, new StringConverter());
                input = new BigStringPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case STRING_CULTURE: {
                IModel<List<StringCulture>> model = new PropertyModel<List<StringCulture>>(attribute, "localizedValues");
                input = new StringCulturePanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case INTEGER: {
                IModel<Integer> model = new SimpleTypeModel<Integer>(systemLocaleStringCulture, new IntegerConverter());
                input = new IntegerPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case DATE: {
                IModel<Date> model = new SimpleTypeModel<Date>(systemLocaleStringCulture, new DateConverter());
                input = new DatePanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case DATE2: {
                IModel<Date> model = new SimpleTypeModel<Date>(systemLocaleStringCulture, new DateConverter());
                input = new Date2Panel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case MASKED_DATE: {
                IModel<Date> model = new SimpleTypeModel<Date>(systemLocaleStringCulture, new DateConverter());
                input = new MaskedDateInputPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case BOOLEAN: {
                IModel<Boolean> model = new SimpleTypeModel<Boolean>(systemLocaleStringCulture, new BooleanConverter());
                input = new BooleanPanel(INPUT_COMPONENT_ID, model, labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case DOUBLE: {
                IModel<Double> model = new SimpleTypeModel<Double>(systemLocaleStringCulture, new DoubleConverter());
                input = new DoublePanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case GENDER: {
                IModel<Gender> model = new SimpleTypeModel<Gender>(systemLocaleStringCulture, new GenderConverter());
                input = new GenderPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
        }
        if (input == null) {
            throw new IllegalStateException("Input component for attribute type " + attributeType.getId() + " is not recognized.");
        }
        return input;
    }

    protected void addComplexAttributesPanel(String id, Class<? extends AbstractComplexAttributesPanel> complexAttributesPanelClass) {
        AbstractComplexAttributesPanel complexAttributes = null;
        if (complexAttributesPanelClass != null) {
            try {
                complexAttributes = complexAttributesPanelClass.getConstructor(String.class, boolean.class).
                        newInstance(id, isHistory());
            } catch (Exception e) {
                LoggerFactory.getLogger(getClass()).error("Couldn't instantiate complex attributes panel object.", e);
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
            if (parentId != null && !Strings.isEmpty(parentEntity)) {
                if (parentId > 0) {
                    componentState = getStrategy().getSearchComponentStateForParent(parentId, parentEntity, null);
                } else {
                    componentState = new SearchComponentState();
                }
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
}