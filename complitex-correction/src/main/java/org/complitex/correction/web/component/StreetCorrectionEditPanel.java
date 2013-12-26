package org.complitex.correction.web.component;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.correction.web.StreetCorrectionList;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.SessionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель редактирования коррекции улицы.
 */
public class StreetCorrectionEditPanel extends AddressCorrectionEditPanel<StreetCorrection> {
    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private SessionBean sessionBean;

    public StreetCorrectionEditPanel(String id, Long correctionId) {
        super(id, correctionId);
    }

    @Override
    protected StreetCorrection getCorrection(Long correctionId) {
        return addressCorrectionBean.getStreetCorrection(correctionId);
    }

    @Override
    protected StreetCorrection newCorrection() {
        return new StreetCorrection();
    }

    @Override
    protected String displayCorrection() {
        StreetCorrection correction = getCorrection();

        String city = cityStrategy.displayDomainObject(cityStrategy.findById(correction.getCityObjectId(), true), getLocale());

        String streetType = null;
        if (correction.getStreetTypeCorrection() != null) {
            streetType = correction.getStreetTypeCorrection().getCorrection();
        }
        if (Strings.isEmpty(streetType)) {
            streetType = null;
        }
        return AddressRenderer.displayAddress(null, city, streetType, correction.getCorrection(), null, null, null, getLocale());
    }

    @Override
    protected Class<? extends Page> getBackPageClass() {
        return StreetCorrectionList.class;
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
    protected boolean validateExistence() {
        return addressCorrectionBean.getStreetCorrectionsCount(FilterWrapper.of(getCorrection())) > 0;
    }

    @Override
    protected Panel getCorrectionInputPanel(String id) {
        return new AddressCorrectionInputPanel(id, getCorrection());
    }

    @Override
    protected boolean freezeOrganization() {
        return true;
    }

    @Override
    protected List<String> getSearchFilters() {
        return ImmutableList.of("city", "street");
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
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("street_title", this, null);
    }
}
