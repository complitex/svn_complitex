/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

/**
 * Список коррекций форм власти.
 * @author Artem
 */
public class OwnershipCorrectionList extends AbstractCorrectionList {

    public OwnershipCorrectionList(PageParameters params) {
        super(params);
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return OwnershipCorrectionEdit.class;
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        if (objectCorrectionId != null) {
            parameters.put(OwnershipCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }
}
