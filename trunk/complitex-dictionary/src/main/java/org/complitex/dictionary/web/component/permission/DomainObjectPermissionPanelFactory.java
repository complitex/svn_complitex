/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.permission;

import org.complitex.dictionary.web.component.factory.WebComponentFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class DomainObjectPermissionPanelFactory {

    private static final Logger log = LoggerFactory.getLogger(DomainObjectPermissionPanelFactory.class);
    public static final String WEB_COMPONENT_NAME = "DomainObjectPermissionPanel";

    public static AbstractDomainObjectPermissionPanel create(String id, DomainObjectPermissionParameters parameters) {
        Class<? extends AbstractDomainObjectPermissionPanel> domainObjectPermissionPanelClass =
                (Class) WebComponentFactoryUtil.getComponentClass(WEB_COMPONENT_NAME);
        try {
            return domainObjectPermissionPanelClass.getConstructor(
                    String.class, DomainObjectPermissionParameters.class).newInstance(id, parameters);
        } catch (Exception e) {
            log.warn("Couldn't to instantiate domain object permission panel. Default one will be used.", e);
            return new DomainObjectPermissionsPanel(id, parameters);
        }
    }

    public DomainObjectPermissionPanelFactory() {
    }
}
