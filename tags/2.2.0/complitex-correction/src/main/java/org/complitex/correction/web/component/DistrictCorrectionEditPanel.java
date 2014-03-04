package org.complitex.correction.web.component;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.DistrictCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.correction.web.DistrictCorrectionList;
import org.complitex.dictionary.entity.FilterWrapper;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель редактирования коррекции района.
 */
public class DistrictCorrectionEditPanel extends AddressCorrectionEditPanel<DistrictCorrection> {
    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public DistrictCorrectionEditPanel(String id, Long correctionId) {
        super(id, correctionId);
    }

    @Override
    protected DistrictCorrection getCorrection(Long correctionId) {
        return addressCorrectionBean.getDistrictCorrection(correctionId);
    }

    @Override
    protected DistrictCorrection newCorrection() {
        return new DistrictCorrection();
    }

    @Override
    protected List<String> getSearchFilters() {
        return ImmutableList.of("city", "district");
    }

    @Override
    protected boolean validateExistence() {
        return addressCorrectionBean.getDistrictCorrectionsCount(FilterWrapper.of(getCorrection())) > 0;
    }

    @Override
    protected boolean freezeOrganization() {
        return true;
    }

    @Override
    protected Class<? extends Page> getBackPageClass() {
        return DistrictCorrectionList.class;
    }

    @Override
    protected void save() {
        addressCorrectionBean.save(getCorrection());
    }

    @Override
    protected void delete() {
        addressCorrectionBean.delete(getCorrection());
    }

    @Override
    protected boolean checkCorrectionEmptiness() {
        return false;
    }

    @Override
    protected boolean preValidate() {
        if (Strings.isEmpty(getCorrection().getCorrection())) {
            error(getString("correction_required"));
            return false;
        }
        return true;
    }

    @Override
    protected String displayCorrection() {
        DistrictCorrection correction = getCorrection();

        String city = cityStrategy.displayDomainObject(cityStrategy.findById(correction.getCityObjectId(), true), getLocale());

        return AddressRenderer.displayAddress(null, city, correction.getCorrection(), getLocale());
    }

    @Override
    protected Panel getCorrectionInputPanel(String id) {
        return new AddressCorrectionInputPanel(id, getCorrection());
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("district_title", this, null);
    }
}
