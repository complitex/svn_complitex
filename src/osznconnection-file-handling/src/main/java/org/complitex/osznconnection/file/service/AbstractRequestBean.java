/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import javax.ejb.EJB;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.warning.RequestWarningBean;

/**
 *
 * @author Artem
 */
public abstract class AbstractRequestBean extends AbstractBean {

    @EJB
    private RequestWarningBean requestWarningBean;

    protected void clearWarnings(long requestFileId, RequestFile.TYPE requestFileType) {
        requestWarningBean.delete(requestFileId, requestFileType);
    }
}
