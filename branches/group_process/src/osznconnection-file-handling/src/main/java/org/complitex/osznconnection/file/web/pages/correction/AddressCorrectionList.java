/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

/**
 *
 * @author Artem
 */
public class AddressCorrectionList extends AbstractCorrectionList {

    public AddressCorrectionList(PageParameters params) {
        super(params);
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return AddressCorrectionEdit.class;
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        parameters.put(AddressCorrectionEdit.CORRECTED_ENTITY, getEntity());
        if (objectCorrectionId != null) {
            parameters.put(AddressCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }
}
