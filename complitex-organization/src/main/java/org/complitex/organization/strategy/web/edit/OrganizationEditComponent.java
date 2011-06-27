package org.complitex.organization.strategy.web.edit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.IStrategy.SimpleObjectInfo;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import java.util.List;
import java.util.Set;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.DisableAwareListMultipleChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

/**
 * 
 * @author Artem
 */
public class OrganizationEditComponent extends AbstractComplexAttributesPanel {

    @EJB
    private StrategyFactory strategyFactory;
    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;
    @EJB
    private OrganizationTypeStrategy organizationTypeStrategy;
    private SearchComponentState districtSearchComponentState;
    private IModel<List<DomainObject>> organizationTypesModel;
    private WebMarkupContainer districtContainer;
    private WebMarkupContainer districtRequiredContainer;
    private WebMarkupContainer parentContainer;

    public OrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        final DomainObject organization = getDomainObject();

        // organization type container
        final List<DomainObject> allOrganizationTypes = organizationTypeStrategy.getAll();
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationTypeStrategy.displayDomainObject(object, getLocale());
            }
        };

        organizationTypesModel = new IModel<List<DomainObject>>() {

            private List<DomainObject> organizationTypes = Lists.newArrayList();

            {
                for (Attribute attribute : organization.getAttributes(IOrganizationStrategy.ORGANIZATION_TYPE)) {
                    if (attribute.getValueId() != null) {
                        for (DomainObject organizationType : allOrganizationTypes) {
                            if (organizationType.getId().equals(attribute.getValueId())) {
                                organizationTypes.add(organizationType);
                            }
                        }
                    }
                }
            }

            @Override
            public List<DomainObject> getObject() {
                return organizationTypes;
            }

            @Override
            public void setObject(List<DomainObject> organizationTypes) {
                this.organizationTypes = organizationTypes;
            }

            @Override
            public void detach() {
            }
        };
        DisableAwareListMultipleChoice<DomainObject> organizationType = new DisableAwareListMultipleChoice<DomainObject>("organizationType",
                organizationTypesModel, allOrganizationTypes, renderer);
        if (isOrganizationTypeEnabled()) {
            organizationType.add(new AjaxFormComponentUpdatingBehavior("onclick") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    onOrganizationTypeChanged(target);
                }
            });
        } else {
            organizationType.setEnabled(false);
        }
        add(organizationType);

        //district container
        districtContainer = new WebMarkupContainer("districtContainer");
        districtContainer.setOutputMarkupPlaceholderTag(true);
        add(districtContainer);

        //district required container
        districtRequiredContainer = new WebMarkupContainer("districtRequiredContainer");
        districtContainer.add(districtRequiredContainer);

        //parent container
        parentContainer = new WebMarkupContainer("parentContainer");
        parentContainer.setOutputMarkupPlaceholderTag(true);
        add(parentContainer);


        //district
        districtSearchComponentState = new SearchComponentState();
        Attribute districtAttribute = organization.getAttribute(IOrganizationStrategy.DISTRICT);
        if (districtAttribute != null) {
            Long districtId = districtAttribute.getValueId();
            if (districtId != null) {
                IStrategy districtStrategy = strategyFactory.getStrategy("district");
                DomainObject district = districtStrategy.findById(districtId, true);
                SimpleObjectInfo info = districtStrategy.findParentInSearchComponent(districtId, null);
                if (info != null) {
                    districtSearchComponentState = districtStrategy.getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    districtSearchComponentState.put("district", district);
                }
            }
        }

        districtContainer.add(new SearchComponent("district", districtSearchComponentState, ImmutableList.of("city", "district"),
                null, ShowMode.ACTIVE, enabled()));
        districtContainer.setVisible(isDistrictVisible());
        districtRequiredContainer.setVisible(isDistrictRequired());

        //parent
        final Attribute parentAttribute = organization.getAttribute(IOrganizationStrategy.USER_ORGANIZATION_PARENT);
        IModel<Long> parentModel = new Model<Long>();
        if (parentAttribute != null) {
            parentModel = new Model<Long>() {

                @Override
                public Long getObject() {
                    return parentAttribute.getValueId();
                }

                @Override
                public void setObject(Long object) {
                    parentAttribute.setValueId(object);
                }
            };
        }

        if (organization.getId() == null) {
            //new organization
            parentContainer.add(new UserOrganizationPicker("parent", parentModel));
        } else {
            Set<Long> excludeOrganizationIds = Sets.newHashSet(organization.getId());
            excludeOrganizationIds.addAll(organizationStrategy.getTreeChildrenOrganizationIds(organization.getId()));
            Long[] excludeAsArray = new Long[excludeOrganizationIds.size()];
            UserOrganizationPicker parent = new UserOrganizationPicker("parent", parentModel, excludeOrganizationIds.toArray(excludeAsArray));
            parent.setEnabled(enabled());
            parentContainer.add(parent);
        }
        parentContainer.setVisible(isParentVisible());
    }

    protected boolean isOrganizationTypeEnabled() {
        return enabled();
    }

    protected boolean enabled() {
        return !isDisabled() && DomainObjectAccessUtil.canEdit(getStrategyName(), "organization", getDomainObject());
    }

    protected String getStrategyName() {
        return null;
    }

    protected void onOrganizationTypeChanged(AjaxRequestTarget target) {
        //parent container
        boolean parentContainerWasVisible = parentContainer.isVisible();
        parentContainer.setVisible(isParentVisible());
        boolean parentContainerVisibleNow = parentContainer.isVisible();
        if (parentContainerWasVisible ^ parentContainerVisibleNow) {
            target.addComponent(parentContainer);
        }

        // district container
        boolean districtContainerWasVisible = districtContainer.isVisible();
        boolean districtRequiredContainerWasVisible = districtRequiredContainer.isVisible();
        districtContainer.setVisible(isDistrictVisible());
        districtRequiredContainer.setVisible(isDistrictRequired());
        boolean districtContainerVisibleNow = districtContainer.isVisible();
        boolean districtRequiredContainerVisibleNow = districtRequiredContainer.isVisible();
        if ((districtContainerWasVisible ^ districtContainerVisibleNow)
                || (districtRequiredContainerWasVisible ^ districtRequiredContainerVisibleNow)) {
            target.addComponent(districtContainer);
        }
    }

    public boolean isDistrictEntered() {
        DomainObject district = districtSearchComponentState.get("district");
        Long districtId = district != null ? district.getId() : null;
        return districtId != null && districtId > 0;
    }

    protected boolean isParentVisible() {
        for (DomainObject organizationType : getOrganizationTypesModel().getObject()) {
            if (organizationType.getId().equals(OrganizationTypeStrategy.USER_ORGANIZATION_TYPE)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDistrictVisible() {
        for (DomainObject organizationType : getOrganizationTypesModel().getObject()) {
            if (organizationType.getId().equals(OrganizationTypeStrategy.USER_ORGANIZATION_TYPE)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDistrictRequired() {
        return false;
    }

    protected IModel<List<DomainObject>> getOrganizationTypesModel() {
        return organizationTypesModel;
    }

    @Override
    public void onUpdate() {
        onPersist();
    }

    @Override
    public void onInsert() {
        onPersist();
    }

    protected void onPersist() {
        //district
        Attribute districtAttribute = getDomainObject().getAttribute(IOrganizationStrategy.DISTRICT);
        if (isDistrictVisible()) {
            districtAttribute.setValueId(isDistrictEntered() ? districtSearchComponentState.get("district").getId() : null);
        } else {
            districtAttribute.setValueId(null);
        }

        //parent
        Attribute parentAttribute = getDomainObject().getAttribute(IOrganizationStrategy.USER_ORGANIZATION_PARENT);
        if (!isParentVisible()) {
            parentAttribute.setValueId(null);
        }

        //organization types
        getDomainObject().removeAttribute(IOrganizationStrategy.ORGANIZATION_TYPE);
        List<DomainObject> organizationTypes = getOrganizationTypesModel().getObject();
        if (organizationTypes != null && !organizationTypes.isEmpty()) {
            Collections.sort(organizationTypes, new Comparator<DomainObject>() {

                @Override
                public int compare(DomainObject o1, DomainObject o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            long attributeId = 1;
            for (DomainObject organizationType : getOrganizationTypesModel().getObject()) {
                Attribute attribute = new Attribute();
                attribute.setAttributeId(attributeId++);
                attribute.setAttributeTypeId(IOrganizationStrategy.ORGANIZATION_TYPE);
                attribute.setValueTypeId(IOrganizationStrategy.ORGANIZATION_TYPE);
                attribute.setValueId(organizationType.getId());
                getDomainObject().addAttribute(attribute);
            }
        }
    }
}
