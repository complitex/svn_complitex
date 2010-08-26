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
        return checkParent(object, getParentEditComponent((DomainObjectEditPanel) component));
    }

    private OrganizationEditComponent getParentEditComponent(DomainObjectEditPanel editPanel) {
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

    private boolean checkParent(DomainObject object, OrganizationEditComponent component) {
        long entityTypeId = object.getEntityTypeId();
        if ((entityTypeId == OrganizationStrategy.OSZN) && component.getParentObject() != null) {
            component.getPage().error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "oszn_cannot_have_parent", component.getLocale()));
            return false;
        } else if ((entityTypeId == OrganizationStrategy.PU) && (component.getParentObject() == null)) {
            component.getPage().error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "pu_must_have_parent", component.getLocale()));
            return false;
        }
        return true;
    }
}
