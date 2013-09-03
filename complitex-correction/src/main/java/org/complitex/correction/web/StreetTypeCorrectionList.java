package org.complitex.correction.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.StreetTypeCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.SessionBean;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class StreetTypeCorrectionList extends AbstractCorrectionList<StreetTypeCorrection> {
    @EJB
    private SessionBean sessionBean;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    public StreetTypeCorrectionList() {
        super("street_type");
    }

    @Override
    protected StreetTypeCorrection newCorrection() {
        return new StreetTypeCorrection();
    }

    @Override
    protected List<StreetTypeCorrection> getCorrections(FilterWrapper<StreetTypeCorrection> filterWrapper) {
        sessionBean.prepareFilterForPermissionCheck(filterWrapper);

        List<StreetTypeCorrection> streetTypeCorrections = addressCorrectionBean.getStreetTypeCorrections(filterWrapper);

        Locale locale = getLocale();

        if (streetTypeCorrections != null && !streetTypeCorrections.isEmpty()) {
            for (Correction streetTypeCorrection : streetTypeCorrections) {
                DomainObject streetTypeObject = streetTypeStrategy.findById(streetTypeCorrection.getObjectId(), false);
                if (streetTypeObject == null) {
                    streetTypeObject = streetTypeStrategy.findById(streetTypeCorrection.getObjectId(), true);
                    streetTypeCorrection.setEditable(false);
                }
                streetTypeCorrection.setDisplayObject(streetTypeStrategy.displayFullName(streetTypeObject, locale));
            }
        }
        return streetTypeCorrections;
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<StreetTypeCorrection> filterWrapper) {
        return addressCorrectionBean.getStreetTypeCorrectionsCount(filterWrapper);
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
