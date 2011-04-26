package org.complitex.organization.strategy.web.edit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.description.EntityType;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.wicket.Component;
import org.complitex.dictionary.strategy.IStrategy.SimpleObjectInfo;

/**
 * 
 * @author Artem
 */
public class OrganizationEditComponent extends AbstractComplexAttributesPanel {

    private static final Logger log = LoggerFactory.getLogger(OrganizationEditComponent.class);
    @EJB
    private StrategyFactory strategyFactory;
    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;
    private Attribute districtAttribute;
    private Attribute parentAttribute;

    private class DistrictSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long districtId = ids.get("district");
            if (districtId != null && districtId > 0) {
                districtAttribute.setValueId(districtId);
            } else {
                districtAttribute.setValueId(null);
            }
        }
    }
    private SearchComponentState componentState;

    public OrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        //district container
        final WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        districtContainer.setOutputMarkupPlaceholderTag(true);
        add(districtContainer);

        //district required container
        final WebMarkupContainer districtRequired = new WebMarkupContainer("districtRequired");
        districtContainer.add(districtRequired);

        //parent container
        final WebMarkupContainer parentContainer = new WebMarkupContainer("parentContainer");
        parentContainer.setOutputMarkupPlaceholderTag(true);
        add(parentContainer);

        final DomainObject currentOrganization = getInputPanel().getObject();
        final DropDownChoice<EntityType> selectType = getInputPanel().getSelectType();
        if (selectType != null) {
            if (currentOrganization.getId() == null) {
                //new organization
                selectType.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update domain object

                        Collection<Component> componentsToUpdate = onTypeChanged();
                        setDistrictVisibility(districtContainer, districtRequired, getInputPanel().getObject().getEntityTypeId());
                        setParentVisibility(parentContainer, getInputPanel().getObject().getEntityTypeId());
                        selectType.setEnabled(false);
                        target.addComponent(districtContainer);
                        target.addComponent(parentContainer);
                        target.addComponent(selectType);
                        if (componentsToUpdate != null && !componentsToUpdate.isEmpty()) {
                            for (Component component : componentsToUpdate) {
                                target.addComponent(component);
                            }
                        }
                    }
                });
            } else {
                selectType.setEnabled(false);
            }
        }

        //district
        componentState = new SearchComponentState();
        districtAttribute = organizationStrategy.getDistrictAttribute(currentOrganization);
        if (districtAttribute != null) {
            Long districtId = districtAttribute.getValueId();

            if (districtId != null) {
                IStrategy districtStrategy = strategyFactory.getStrategy("district");
                DomainObject district = districtStrategy.findById(districtId, true);
                SimpleObjectInfo info = districtStrategy.findParentInSearchComponent(districtId, null);
                if (info != null) {
                    componentState = districtStrategy.getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    componentState.put("district", district);
                }
            }
        }

        districtContainer.add(new SearchComponent("district", componentState, ImmutableList.of("city", "district"), new DistrictSearchCallback(),
                ShowMode.ACTIVE, !isDisabled() && DomainObjectAccessUtil.canEdit(null, "organization", currentOrganization)));
        setDistrictVisibility(districtContainer, districtRequired, currentOrganization.getEntityTypeId());

        //parent
        parentAttribute = organizationStrategy.getParentAttribute(currentOrganization);
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

        if (currentOrganization.getId() == null) {
            //new organization
            parentContainer.add(new UserOrganizationPicker("parent", parentModel));
        } else {
            Set<Long> excludeOrganizationIds = Sets.newHashSet(currentOrganization.getId());
            excludeOrganizationIds.addAll(organizationStrategy.getTreeChildrenOrganizationIds(currentOrganization.getId()));
            Long[] excludeAsArray = new Long[excludeOrganizationIds.size()];
            UserOrganizationPicker parent = new UserOrganizationPicker("parent", parentModel, excludeOrganizationIds.toArray(excludeAsArray));
            parent.setEnabled(!isDisabled() && DomainObjectAccessUtil.canEdit(null, "organization", currentOrganization));
            parentContainer.add(parent);
        }

        setParentVisibility(parentContainer, currentOrganization.getEntityTypeId());
    }

    protected Collection<Component> onTypeChanged() {
        return null;
    }

    private void setDistrictVisibility(WebMarkupContainer districtContainer, WebMarkupContainer districtRequiredContainer,
            Long entityTypeId) {
        if (districtAttribute == null) {
            districtContainer.setVisible(false);
            return;
        }

        if (entityTypeId != null && isDistrictVisible(entityTypeId)) {
            districtContainer.setVisible(true);

            if (isDistrictNotRequired(entityTypeId)) {
                districtRequiredContainer.setVisible(false);
            }
        } else {
            districtContainer.setVisible(false);
            districtAttribute.setValueId(null);
            componentState.clear();
        }
    }

    private void setParentVisibility(WebMarkupContainer parentContainer, Long entityTypeId) {
        if (parentAttribute == null) {
            parentContainer.setVisible(false);
            return;
        }

        if (entityTypeId != null && isParentVisible(entityTypeId)) {
            parentContainer.setVisible(true);
        } else {
            parentContainer.setVisible(false);
            parentAttribute.setValueId(null);
        }
    }

    public boolean isDistrictEntered() {
        DomainObject district = componentState.get("district");
        Long districtId = district != null ? district.getId() : null;
        return districtId != null && districtId > 0;
    }

    protected boolean isParentVisible(Long entityTypeId) {
        return entityTypeId.equals(IOrganizationStrategy.USER_ORGANIZATION);
    }

    protected boolean isDistrictVisible(Long entityTypeId) {
        return entityTypeId.equals(IOrganizationStrategy.USER_ORGANIZATION);
    }

    protected boolean isDistrictNotRequired(Long entityTypeId) {
        return entityTypeId.equals(IOrganizationStrategy.USER_ORGANIZATION);
    }
}
