package org.complitex.organization;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Artem
 */
public interface IOrganizationModule {

    Class<? extends WebPage> getEditPage();

    String getEditPageParams();
}
