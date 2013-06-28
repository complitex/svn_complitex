package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.entity.CorrectionExample;
import org.complitex.correction.service.AddressCorrectionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class DistrictCorrectionList extends AddressCorrectionList {

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public DistrictCorrectionList() {
        super("district");
    }

    @Override
    protected List<? extends Correction> find(CorrectionExample example) {
        return addressCorrectionBean.findDistricts(example);
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected String displayCorrection(Correction correction) {
        String city = null;
        if (correction.getParent() != null) {
            city = correction.getParent().getCorrection();
        }

        return AddressRenderer.displayAddress(null, city, correction.getCorrection(), getLocale());
    }
}
