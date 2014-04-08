package org.complitex.dictionary.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.IStrategy.SimpleObjectInfo;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.web.component.search.CollapsibleInputSearchComponent;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.complitex.dictionary.strategy.web.DomainObjectAccessUtil.canEdit;

/**
 *
 * @author Artem
 */
public class DomainObjectInputPanel extends Panel {

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
                                    new IVisitor<DomainObjectEditPanel, DomainObjectEditPanel>() {

                                        @Override
                                        public void component(DomainObjectEditPanel object, IVisit<DomainObjectEditPanel> visit) {
                                            visit.stop(object);
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
                item.add(new Label("label", DomainObjectComponentUtil.labelModel(attributeType.getAttributeNames(), getLocale())));
                WebMarkupContainer required = new WebMarkupContainer("required");
                item.add(required);
                required.setVisible(attributeType.isMandatory());

                item.add(DomainObjectComponentUtil.newInputComponent(entity, strategyName, object, attr,
                        getLocale(), isHistory()));
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
