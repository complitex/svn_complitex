package org.complitex.correction.web.component;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.correction.entity.CityCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.correction.web.CityCorrectionList;
import org.complitex.dictionary.entity.FilterWrapper;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель редактирования коррекции населенного пункта.
 */
public class CityCorrectionEditPanel extends AddressCorrectionEditPanel<CityCorrection> {

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public CityCorrectionEditPanel(String id, Long correctionId) {
        super(id, correctionId);
    }

    @Override
    protected CityCorrection getCorrection(Long correctionId) {
        return addressCorrectionBean.getCityCorrection(correctionId);
    }

    @Override
    protected CityCorrection newCorrection() {
        return new CityCorrection();
    }

    @Override
    protected List<String> getSearchFilters() {
        return ImmutableList.of("city");
    }

    @Override
    protected boolean validateExistence() {
        return addressCorrectionBean.getCityCorrectionsCount(FilterWrapper.of(getCorrection())) > 0;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("city_title", this, null);
    }

    @Override
    protected Class<? extends Page> getBackPageClass() {
        return CityCorrectionList.class;
    }

    @Override
    protected void save() {
        addressCorrectionBean.save(getCorrection());
    }

    @Override
    protected void delete() {
        addressCorrectionBean.delete(getCorrection());
    }
}
