/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.RequestStatus;

/**
 *
 * @author Artem
 */
public class StatusRenderer implements IChoiceRenderer<RequestStatus> {

    private static final String RESOURCE_BUNDLE = StatusRenderer.class.getName();

    @Override
    public Object getDisplayValue(RequestStatus object) {
        return displayValue(object);
    }

    @Override
    public String getIdValue(RequestStatus object, int index) {
        return object.name();
    }

    public static String displayValue(RequestStatus object) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, object.name(), Session.get().getLocale());
    }

    public static String displayTarifNotFoundDetails(Double calculationCenterCode2_1) {
        return ResourceUtil.getFormatString(RESOURCE_BUNDLE, "TARIF_CODE2_1_NOT_FOUND_DETAILS", Session.get().getLocale(), calculationCenterCode2_1);
    }
}
