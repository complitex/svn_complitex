package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.IStrategy;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author Artem
 */
public class StreetCorrectionList extends AddressCorrectionList<StreetCorrection> {

    @EJB
    private SessionBean sessionBean;

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
    protected StreetCorrection newCorrection() {
        return new StreetCorrection();
    }

    @Override
    protected List<StreetCorrection> getCorrections(FilterWrapper<StreetCorrection> filterWrapper) {
        sessionBean.prepareFilterForPermissionCheck(filterWrapper);

        List<StreetCorrection> streets = addressCorrectionBean.getStreetCorrections(filterWrapper);

        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy cityStrategy = strategyFactory.getStrategy("city");
        Locale locale = getLocale();

        for (Correction c : streets) {
            DomainObject street = streetStrategy.findById(c.getObjectId(), false);
            if (street == null) {
                street = streetStrategy.findById(c.getObjectId(), true);
                c.setEditable(false);
            }
            DomainObject city = null;
            if (c.isEditable()) {
                city = cityStrategy.findById(street.getParentId(), false);
            }
            if (city == null) {
                city = cityStrategy.findById(street.getParentId(), true);
                c.setEditable(false);
            }
            String displayCity = cityStrategy.displayDomainObject(city, locale);
            String displayStreet = streetStrategy.displayDomainObject(street, locale);
            c.setDisplayObject(displayCity + ", " + displayStreet);
        }

        return streets;
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<StreetCorrection> filterWrapper) {
        return addressCorrectionBean.getStreetCorrectionsCount(filterWrapper);
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
