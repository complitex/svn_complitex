package org.complitex.correction.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.*;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.correction.web.component.AbstractCorrectionEditPanel;
import org.complitex.correction.web.component.AddressCorrectionInputPanel;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.IStrategy.SimpleObjectInfo;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Страница для редактирования коррекций адресов.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class AddressCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTED_ENTITY = "entity";
    public static final String CORRECTION_ID = "correction_id";

    @EJB
    private StrategyFactory strategyFactory;

    @EJB
    private CityStrategy cityStrategy;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    @EJB
    private StreetStrategy streetStrategy;

    @EJB
    private BuildingStrategy buildingStrategy;

    private class Callback implements ISearchCallback, Serializable {

        private Correction correction;
        private String entity;

        private Callback(Correction correction, String entity) {
            this.correction = correction;
            this.entity = entity;
        }

        @Override
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(entity);
            if (id != null && id > 0) {
                correction.setObjectId(id);
            } else {
                correction.setObjectId(null);
            }
        }
    }

    /**
     * Стандартная панель редактирования коррекции элемента адреса.
     */
    private abstract class AddressCorrectionEditPanel<T extends Correction> extends AbstractCorrectionEditPanel<T> {

        @EJB
        private AddressCorrectionBean addressCorrectionBean;

        private AddressCorrectionEditPanel(String id, Long correctionId) {
            super(id, correctionId);
        }

        @Override
        protected IModel<String> internalObjectLabel(Locale locale) {
            return new ResourceModel("address");
        }

        @Override
        protected Panel internalObjectPanel(String id) {
            Correction correction = getCorrection();
            String entity = correction.getEntity();
            SearchComponentState componentState = new SearchComponentState();
            if (!isNew()) {
                long objectId = correction.getObjectId();
                SimpleObjectInfo info = getStrategy(entity).findParentInSearchComponent(objectId, null);
                if (info != null) {
                    componentState = getStrategy(entity).getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    componentState.put(entity, findObject(objectId, entity));
                }
            }
            return new WiQuerySearchComponent(id, componentState, getSearchFilters(), new Callback(correction, entity), ShowMode.ACTIVE, true);
        }

        @Override
        protected String getNullObjectErrorMessage() {
            return getString("address_required");
        }

        protected IStrategy getStrategy(String entity) {
            return strategyFactory.getStrategy(entity);
        }

        protected DomainObject findObject(long objectId, String entity) {
            return getStrategy(entity).findById(objectId, true);
        }

        protected abstract List<String> getSearchFilters();

        @Override
        protected PageParameters getBackPageParameters() {
            return new PageParameters();
        }

        @Override
        protected abstract boolean validateExistence();
    }

    /**
     * Панель редактирования коррекции населенного пункта.
     */
    private class CityCorrectionEditPanel extends AddressCorrectionEditPanel<CityCorrection> {

        @EJB
        private AddressCorrectionBean addressCorrectionBean;

        private CityCorrectionEditPanel(String id, Long correctionId) {
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

    /**
     * Панель редактирования коррекции района.
     */
    private class DistrictCorrectionEditPanel extends AddressCorrectionEditPanel<DistrictCorrection> {

        @EJB
        private AddressCorrectionBean addressCorrectionBean;

        private DistrictCorrectionEditPanel(String id, Long correctionId) {
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

    /**
     * Панель редактирования коррекции улицы.
     */
    private class StreetCorrectionEditPanel extends AddressCorrectionEditPanel<StreetCorrection> {
        @EJB
        private AddressCorrectionBean addressCorrectionBean;

        @EJB
        private SessionBean sessionBean;

        private StreetCorrectionEditPanel(String id, Long correctionId) {
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

    /**
     * Панель редактирования коррекции дома.
     */
    private class BuildingCorrectionEditPanel extends AddressCorrectionEditPanel<BuildingCorrection> {

        @EJB
        private AddressCorrectionBean addressCorrectionBean;
        @EJB
        private SessionBean sessionBean;

        private BuildingCorrectionEditPanel(String id, Long correctionId) {
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

    /**
     * Панель редактирования коррекции дома.
     */
    /**
     * Панель редактирования коррекции района.
     */
    private class ApartmentCorrectionEditPanel extends AddressCorrectionEditPanel<ApartmentCorrection> {

        @EJB
        private AddressCorrectionBean addressCorrectionBean;

        private ApartmentCorrectionEditPanel(String id, Long correctionId) {
            super(id, correctionId);
        }

        @Override
        protected ApartmentCorrection getCorrection(Long correctionId) {
            return addressCorrectionBean.getApartmentCorrection(correctionId);
        }

        @Override
        protected ApartmentCorrection newCorrection() {
            return new ApartmentCorrection();
        }

        @Override
        protected List<String> getSearchFilters() {
            return ImmutableList.of("city", "street", "building", "apartment");
        }

        @Override
        protected boolean validateExistence() {
            return addressCorrectionBean.getApartmentCorrectionsCount(FilterWrapper.of(getCorrection())) > 0;
        }

        @Override
        protected boolean freezeOrganization() {
            return true;
        }

        @Override
        protected Class<? extends Page> getBackPageClass() {
            return ApartmentCorrectionList.class;
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
            ApartmentCorrection correction = getCorrection();

            Building buildingDomainObject = buildingStrategy.findById(correction.getBuildingObjectId(), true);
            String building = buildingStrategy.displayDomainObject(buildingDomainObject, getLocale());

            DomainObject streetDomainObject = streetStrategy.findById(buildingDomainObject.getPrimaryStreetId(), true);
            String street = streetStrategy.displayDomainObject(streetDomainObject, getLocale());

            DomainObject cityDomainObject = cityStrategy.findById(streetDomainObject.getParentId(), true);
            String city = cityStrategy.displayDomainObject(cityDomainObject, getLocale());

            return AddressRenderer.displayAddress(null, city, null, street, building, null, correction.getCorrection(), getLocale());
        }

        @Override
        protected Panel getCorrectionInputPanel(String id) {
            return new AddressCorrectionInputPanel(id, getCorrection());
        }

        @Override
        protected IModel<String> getTitleModel() {
            return new StringResourceModel("apartment_title", this, null);
        }
    }
    private AbstractCorrectionEditPanel addressEditPanel;

    public AddressCorrectionEdit(PageParameters params) {
        String entity = params.get(CORRECTED_ENTITY).toString();
        Long correctionId = params.get(CORRECTION_ID).toOptionalLong();
        switch (entity) {
            case "city":
                addressEditPanel = new CityCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "district":
                addressEditPanel = new DistrictCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "street":
                addressEditPanel = new StreetCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "building":
                addressEditPanel = new BuildingCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "apartment":
                addressEditPanel = new ApartmentCorrectionEditPanel("addressEditPanel", correctionId);
                break;
        }
        add(addressEditPanel);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                addressEditPanel.executeDeletion();
            }

            @Override
            public boolean isVisible() {
                return !addressEditPanel.isNew();
            }
        });
        return toolbar;
    }
}
