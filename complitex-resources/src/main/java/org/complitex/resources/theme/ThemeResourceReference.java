package org.complitex.resources.theme;

import org.apache.wicket.ResourceReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.05.11 16:02
 */
public class ThemeResourceReference extends ResourceReference{
    public ThemeResourceReference() {
        super(ThemeResourceReference.class, "jquery-ui-1.8.13.custom.css");
    }
}
