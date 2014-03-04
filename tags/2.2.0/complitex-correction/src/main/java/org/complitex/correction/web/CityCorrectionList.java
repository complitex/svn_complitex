package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.correction.entity.CityCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class CityCorrectionList extends AddressCorrectionList<CityCorrection> {
    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private CityStrategy cityStrategy;

    public CityCorrectionList() {
        super("city");
    }

    @Override
    protected CityCorrection newCorrection() {
        return new CityCorrection();
    }

    @Override
    protected List<CityCorrection> getCorrections(FilterWrapper<CityCorrection> filterWrapper) {
        return addressCorrectionBean.getCityCorrections(filterWrapper);
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<CityCorrection> filterWrapper) {
        return addressCorrectionBean.getCityCorrectionsCount(filterWrapper);
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected String displayInternalObject(Correction correction) {
        // todo tune getName
        return cityStrategy.displayDomainObject(cityStrategy.findById(correction.getObjectId(), true), getLocale());
    }

    @Override
    protected boolean isSyncVisible() {
        return false;
    }
}
