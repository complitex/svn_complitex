package org.complitex.osznconnection.file.web;

import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.service.RequestFileBean;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
public class RequestFileList extends TemplatePage{
    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    public RequestFileList() {
        super();

        

    }
}
