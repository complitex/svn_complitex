package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.DistrictCorrection;
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
public class DistrictCorrectionList extends AddressCorrectionList<DistrictCorrection> {
    @EJB
    private SessionBean sessionBean;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private CityStrategy cityStrategy;

    public DistrictCorrectionList() {
        super("district");
    }

    @Override
    protected DistrictCorrection newCorrection() {
        return new DistrictCorrection();
    }

    @Override
    protected List<DistrictCorrection> getCorrections(FilterWrapper<DistrictCorrection> filterWrapper) {
        sessionBean.prepareFilterForPermissionCheck(filterWrapper);

        List<DistrictCorrection> districts = addressCorrectionBean.getDistrictCorrections(filterWrapper);

        IStrategy districtStrategy = strategyFactory.getStrategy("district");
        IStrategy cityStrategy = strategyFactory.getStrategy("city");

        Locale locale = getLocale();

        for (Correction c : districts) {
            DomainObject district = districtStrategy.findById(c.getObjectId(), false);
            if (district == null) {
                district = districtStrategy.findById(c.getObjectId(), true);
                c.setEditable(false);
            }
            DomainObject city = null;
            if (c.isEditable()) {
                city = cityStrategy.findById(district.getParentId(), false);
            }
            if (city == null) {
                city = cityStrategy.findById(district.getParentId(), true);
                c.setEditable(false);
            }
            String displayCity = cityStrategy.displayDomainObject(city, locale);
            String displayDistrict = districtStrategy.displayDomainObject(district, locale);
            c.setDisplayObject(displayCity + ", " + displayDistrict);
        }
        return districts;
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<DistrictCorrection> filterWrapper) {
        return addressCorrectionBean.getDistrictCorrectionsCount(filterWrapper);
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected String displayCorrection(DistrictCorrection correction) {
        String city = cityStrategy.displayDomainObject(cityStrategy.findById(correction.getCityObjectId(), true), getLocale());

        return AddressRenderer.displayAddress(null, city, correction.getCorrection(), getLocale());
    }
}
