package org.complitex.correction.web.component;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.BuildingCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.correction.web.BuildingCorrectionList;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.SessionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель редактирования коррекции дома.
 */
public class BuildingCorrectionEditPanel extends AddressCorrectionEditPanel<BuildingCorrection> {
    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private SessionBean sessionBean;

    public BuildingCorrectionEditPanel(String id, Long correctionId) {
        super(id, correctionId);
    }

    @Override
    protected BuildingCorrection getCorrection(Long correctionId) {
        return addressCorrectionBean.getBuildingCorrection(correctionId);
    }

    @Override
    protected BuildingCorrection newCorrection() {
        return new BuildingCorrection();
    }

    @Override
    protected String displayCorrection() {
        BuildingCorrection correction = getCorrection();

        String city = ""; //todo display city

        DomainObject streetDomainObject = streetStrategy.findById(correction.getStreetObjectId(), true);

        String street = streetStrategy.displayDomainObject(streetDomainObject, getLocale());

        return AddressRenderer.displayAddress(null, city, null, street, correction.getCorrection(),
                correction.getCorrectionCorp(), null, getLocale());
    }

    @Override
    protected Class<? extends Page> getBackPageClass() {
        return BuildingCorrectionList.class;
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
        return addressCorrectionBean.getBuildingCorrectionsCount(FilterWrapper.of(getCorrection())) > 0;
    }

    @Override
    protected Panel getCorrectionInputPanel(String id) {
        return new AddressCorrectionInputPanel(id, getCorrection());
    }

    @Override
    protected List<String> getSearchFilters() {
        return ImmutableList.of("city", "street", "building");
    }

    @Override
    protected boolean freezeOrganization() {
        return true;
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
        return new StringResourceModel("building_title", this, null);
    }
}
