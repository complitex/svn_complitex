/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Map;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.description.EntityType;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.web.CanEditUtil;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.information.strategy.district.DistrictStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Artem
 */
public class OrganizationEditComponent extends AbstractComplexAttributesPanel {

    private static final Logger log = LoggerFactory.getLogger(OrganizationEditComponent.class);

    @EJB(name = "DistrictStrategy")
    private DistrictStrategy districtStrategy;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    private Attribute districtAttribute;

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
        setOutputMarkupId(true);
        final WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        add(districtContainer);

        final DomainObject currentOrganization = getInputPanel().getObject();
        final DropDownChoice<EntityType> selectType = getInputPanel().getSelectType();
        if (selectType != null) {
            if (currentOrganization.getId() == null) {
                //new organization
                selectType.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        //update domain object

                        setVisibility(districtContainer, getInputPanel().getObject());
                        selectType.setEnabled(false);
                        target.addComponent(OrganizationEditComponent.this);
                        target.addComponent(selectType);
                    }
                });
            } else {
                selectType.setEnabled(false);
            }
        }

        //district
        districtAttribute = organizationStrategy.getDistrictAttribute(currentOrganization);
        Long districtId = districtAttribute.getValueId();

        componentState = new SearchComponentState();
        if (districtId != null) {
            DomainObject district = null;
            DomainObjectExample example = new DomainObjectExample();
            example.setId(districtId);
            district = districtStrategy.find(example).get(0);

            Strategy.RestrictedObjectInfo info = districtStrategy.findParentInSearchComponent(districtId, null);
            if (info != null) {
                componentState = districtStrategy.getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                componentState.put("district", district);
            }
        }

        districtContainer.add(new SearchComponent("district", componentState, ImmutableList.of("city", "district"), new DistrictSearchCallback(),
                !isDisabled() && CanEditUtil.canEdit(currentOrganization)));
        setVisibility(districtContainer, currentOrganization);
    }

    private void setVisibility(WebMarkupContainer districtContainer, DomainObject currentOrganization) {
        Long entityTypeId = currentOrganization.getEntityTypeId();
        log.info("EntityTypeId : {}", entityTypeId);

        if (entityTypeId != null && entityTypeId.equals(OrganizationStrategy.OSZN)) {
            districtContainer.setVisible(true);
        } else {
            districtContainer.setVisible(false);
            districtAttribute.setValueId(null);
            componentState.clear();
        }
    }

    public boolean isDistrictEntered() {
        return componentState.get("district") != null;
    }
}
