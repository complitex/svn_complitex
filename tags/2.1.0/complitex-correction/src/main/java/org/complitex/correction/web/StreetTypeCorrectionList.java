package org.complitex.correction.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.entity.CorrectionExample;
import org.complitex.correction.service.AddressCorrectionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class StreetTypeCorrectionList extends AbstractCorrectionList {

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public StreetTypeCorrectionList() {
        super("street_type");
    }

    @Override
    protected List<? extends Correction> find(CorrectionExample example) {
        return addressCorrectionBean.findStreetTypes(example);
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return StreetTypeCorrectionEdit.class;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        if (objectCorrectionId != null) {
            parameters.set(StreetTypeCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }
}
