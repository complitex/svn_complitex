package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.entity.CorrectionExample;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.correction.service.AddressCorrectionBean;

import javax.ejb.EJB;
import java.util.List;


/**
 *
 * @author Artem
 */
public class StreetCorrectionList extends AddressCorrectionList {

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public StreetCorrectionList() {
        super("street");
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected List<StreetCorrection> find(CorrectionExample example) {
        return addressCorrectionBean.findStreets(example);
    }

    @Override
    protected String displayCorrection(Correction correction) {
        StreetCorrection streetCorrection = (StreetCorrection) correction;
        String city = null;
        if (streetCorrection.getParent() != null) {
            city = streetCorrection.getParent().getCorrection();
        }
        String streetType = null;
        if (streetCorrection.getStreetTypeCorrection() != null) {
            streetType = streetCorrection.getStreetTypeCorrection().getCorrection();
        }
        if (Strings.isEmpty(streetType)) {
            streetType = null;
        }


        return AddressRenderer.displayAddress(null, city, streetType, streetCorrection.getCorrection(), null, null, null, getLocale());
    }
}
