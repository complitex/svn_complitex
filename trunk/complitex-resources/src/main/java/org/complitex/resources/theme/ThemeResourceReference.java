package org.complitex.resources.theme;


import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.05.11 16:02
 */
public class ThemeResourceReference extends PackageResourceReference {
    public ThemeResourceReference() {
        super(ThemeResourceReference.class, "jquery-ui-1.10.4.custom.css");
    }
}
