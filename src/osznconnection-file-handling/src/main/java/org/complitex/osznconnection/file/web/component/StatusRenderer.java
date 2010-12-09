/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.complitex.dictionaryfw.util.EjbBeanLocator;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.StatusRenderService;

/**
 *
 * @author Artem
 */
public class StatusRenderer implements IChoiceRenderer<RequestStatus> {

    @Override
    public Object getDisplayValue(RequestStatus object) {
        StatusRenderService statusRenderService = EjbBeanLocator.getBean(StatusRenderService.class);
        return statusRenderService.displayStatus(object, Session.get().getLocale());
    }

    @Override
    public String getIdValue(RequestStatus object, int index) {
        return object.name();
    }
}
