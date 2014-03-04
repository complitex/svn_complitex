package org.complitex.organization;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author Artem
 */
public interface IOrganizationModule {

    Class<? extends WebPage> getEditPage();

    PageParameters getEditPageParams();
}
