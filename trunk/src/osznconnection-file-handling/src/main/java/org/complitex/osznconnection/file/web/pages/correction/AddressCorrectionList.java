/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * Страница для списка коррекций элементов адреса(город, улица).
 * @author Artem
 */
public class AddressCorrectionList extends AbstractCorrectionList {

    @EJB(name = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

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

    @Override
    protected List<? extends Correction> find(CorrectionExample example) {
        return addressCorrectionBean.find(example);
    }

    @Override
    protected String displayInternalObject(Correction correction) {
        return correction.getDisplayObject();
    }

    @Override
    protected String displayCorrection(Correction correction) {
        boolean districtOrStreet = "street".equals(this.getEntity()) || "district".equals(this.getEntity());
        if (districtOrStreet && correction.getParent() != null) {
            return correction.getParent().getCorrection() + ", " + correction.getCorrection();
        }

        return super.displayCorrection(correction);
    }
}
