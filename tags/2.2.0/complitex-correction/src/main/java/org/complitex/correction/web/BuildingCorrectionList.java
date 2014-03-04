package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.BuildingCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;

/**
 * Список коррекций домов.
 * @author Artem
 */
public class BuildingCorrectionList extends AddressCorrectionList<BuildingCorrection> {
    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private SessionBean sessionBean;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private LocaleBean localeBean;

    public BuildingCorrectionList() {
        super("building");
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected BuildingCorrection newCorrection() {
        return new BuildingCorrection();
    }

    @Override
    protected List<BuildingCorrection> getCorrections(FilterWrapper<BuildingCorrection> filterWrapper) {
        sessionBean.prepareFilterForPermissionCheck(filterWrapper);

        List<BuildingCorrection> list = addressCorrectionBean.getBuildingCorrections(filterWrapper);

        IStrategy cityStrategy = strategyFactory.getStrategy("city");
        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy buildingStrategy = strategyFactory.getStrategy("building");

        Locale locale = getLocale();

        for (Correction c : list) {
            try {
                DomainObject building = buildingStrategy.findById(c.getObjectId(), false);

                if (building == null) {
                    building = buildingStrategy.findById(c.getObjectId(), true);
                    c.setEditable(false);
                }
                SearchComponentState state = buildingStrategy.getSearchComponentStateForParent(building.getParentId(), "building_address", null);
                DomainObject street = state.get("street");
                DomainObject city = state.get("city");
                String displayBuilding = buildingStrategy.displayDomainObject(building, locale);
                String displayStreet = streetStrategy.displayDomainObject(street, locale);
                String displayCity = cityStrategy.displayDomainObject(city, locale);
                c.setDisplayObject(displayCity + ", " + displayStreet + ", " + displayBuilding);
            } catch (Exception e) {
                log().warn("[Полный адрес не найден]", e);
                c.setDisplayObject("[Полный адрес не найден]");
                c.setEditable(false);
            }
        }

        return list;
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<BuildingCorrection> filterWrapper) {
        return addressCorrectionBean.getBuildingCorrectionsCount(filterWrapper);
    }

    @Override
    protected String displayCorrection(BuildingCorrection correction) {
        DomainObject streetDomainObject = streetStrategy.findById(correction.getStreetObjectId(), true);

        String city = ""; //todo display city

        String street = streetStrategy.displayDomainObject(streetDomainObject, getLocale());

        BuildingCorrection bc = (BuildingCorrection) correction;
        return AddressRenderer.displayAddress(null, city, null, street, bc.getCorrection(), bc.getCorrectionCorp(), null, getLocale());
    }
}
