package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.RoomCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;

/**
 * @author Pavel Sknar
 */
public class RoomCorrectionList extends AddressCorrectionList<RoomCorrection> {
    @EJB
    private SessionBean sessionBean;

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public RoomCorrectionList() {
        super("room");
    }

    @Override
    protected RoomCorrection newCorrection() {
        return new RoomCorrection();
    }

    @Override
    protected List<RoomCorrection> getCorrections(FilterWrapper<RoomCorrection> filterWrapper) {
        sessionBean.prepareFilterForPermissionCheck(filterWrapper);

        List<RoomCorrection> rooms = addressCorrectionBean.getRoomCorrections(filterWrapper);

        IStrategy roomStrategy = strategyFactory.getStrategy("room");
        IStrategy apartmentStrategy = strategyFactory.getStrategy("apartment");
        IStrategy buildingStrategy = strategyFactory.getStrategy("building");
        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy cityStrategy = strategyFactory.getStrategy("city");

        Locale locale = getLocale();

        for (RoomCorrection c : rooms) {
            DomainObject room = roomStrategy.findById(c.getObjectId(), false);
            if (room == null) {
                room = roomStrategy.findById(c.getObjectId(), true);
                c.setEditable(false);
            }

            SearchComponentState state = roomStrategy.getSearchComponentStateForParent(room.getParentId(), "room", null);
            DomainObject apartment = state.get("apartment");
            DomainObject building = state.get("building");
            DomainObject street = state.get("street");
            DomainObject city = state.get("city");
            String displayRoom = roomStrategy.displayDomainObject(room, locale);
            String displayApartment = apartment == null || apartment.getId() < 1? "" : apartmentStrategy.displayDomainObject(apartment, locale);
            String displayBuilding = buildingStrategy.displayDomainObject(building, locale);
            String displayStreet = streetStrategy.displayDomainObject(street, locale);
            String displayCity = cityStrategy.displayDomainObject(city, locale);
            c.setDisplayObject(displayCity + ", " + displayStreet + ", " + displayBuilding + ", " + displayApartment + ", " + displayRoom);
        }
        return rooms;
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<RoomCorrection> filterWrapper) {
        return addressCorrectionBean.getRoomCorrectionsCount(filterWrapper);
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected String displayCorrection(RoomCorrection correction) {
        IStrategy apartmentStrategy = strategyFactory.getStrategy("apartment");
        IStrategy buildingStrategy = strategyFactory.getStrategy("building");
        IStrategy streetStrategy = strategyFactory.getStrategy("street");
        IStrategy cityStrategy = strategyFactory.getStrategy("city");

        String apartment = null;
        Building buildingDomainObject;
        if (correction.getApartmentObjectId() == null) {
            buildingDomainObject = (Building)buildingStrategy.findById(correction.getBuildingObjectId(), true);
        } else {
            DomainObject apartmentDomainObject = apartmentStrategy.findById(correction.getApartmentObjectId(), true);
            apartment = apartmentStrategy.displayDomainObject(apartmentDomainObject, getLocale());

            buildingDomainObject = (Building)buildingStrategy.findById(apartmentDomainObject.getParentId(), true);
        }
        String building = buildingStrategy.displayDomainObject(buildingDomainObject, getLocale());

        DomainObject streetDomainObject = streetStrategy.findById(buildingDomainObject.getPrimaryStreetId(), true);
        String street = streetStrategy.displayDomainObject(streetDomainObject, getLocale());

        DomainObject cityDomainObject = cityStrategy.findById(streetDomainObject.getParentId(), true);
        String city = cityStrategy.displayDomainObject(cityDomainObject, getLocale());

        return AddressRenderer.displayAddress(null, city, null, street, building, null, apartment, correction.getCorrection(), getLocale());
    }
}

