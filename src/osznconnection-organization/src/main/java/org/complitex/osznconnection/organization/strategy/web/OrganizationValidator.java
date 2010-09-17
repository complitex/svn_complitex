/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web;

import org.apache.wicket.Component;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

/**
 *
 * @author Artem
 */
public class OrganizationValidator implements IValidator {

    private OrganizationEditComponent organizationEditComponent;

    @Override
    public boolean validate(DomainObject object, Component component) {
        boolean valid = checkDistrict(object, getEditComponent((DomainObjectEditPanel) component));
        return valid;
    }

    private OrganizationEditComponent getEditComponent(DomainObjectEditPanel editPanel) {
        if (organizationEditComponent == null) {
            editPanel.visitChildren(OrganizationEditComponent.class, new Component.IVisitor<OrganizationEditComponent>() {

                @Override
                public Object component(OrganizationEditComponent component) {
                    organizationEditComponent = component;
                    return STOP_TRAVERSAL;
                }
            });
        }
        return organizationEditComponent;
    }

    private boolean checkDistrict(DomainObject object, OrganizationEditComponent component) {
        Long entityTypeId = object.getEntityTypeId();
        if (entityTypeId != null && entityTypeId.equals(OrganizationStrategy.OSZN)) {
            boolean validated = component.isDistrictEntered();
            if (!validated) {
                component.error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "must_have_district", component.getLocale()));
            }
            return validated;
        } else {
            return true;
        }
    }
}
