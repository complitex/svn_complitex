/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.Map;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.dictionaryfw.entity.Attribute;
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
        final DomainObject currentOrganization = getInputPanel().getObject();

        //district
        districtAttribute = Iterables.find(currentOrganization.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(OrganizationStrategy.DISTRICT);
            }
        });
        Long districtId = districtAttribute.getValueId();

        if (currentOrganization.getId() == null) {
            componentState = new SearchComponentState();
        } else {
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

        add(new SearchComponent("district", componentState, ImmutableList.of("city", "district"), new DistrictSearchCallback(),
                !isDisabled() && CanEditUtil.canEdit(currentOrganization)));
    }

    public boolean isDistrictEntered() {
        return componentState.get("district") != null;
    }
}
