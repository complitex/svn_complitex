/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.commons.web.component.toolbar.DeleteItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.osznconnection.file.web.component.correction.edit.AddressCorrectionInputPanel;
import org.complitex.osznconnection.file.web.pages.util.BuildingFormatter;

/**
 * Страница для редактирования коррекций адресов.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class AddressCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTED_ENTITY = "entity";
    public static final String CORRECTION_ID = "correction_id";
    
    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private class Callback implements ISearchCallback, Serializable {

        private Correction correction;
        private String entity;

        public Callback(Correction correction, String entity) {
            this.correction = correction;
            this.entity = entity;
        }

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(entity);
            if (id != null && id > 0) {
                correction.setObjectId(id);
            }
        }
    }

    /**
     * Стандартная панель редактирования коррекции элемента адреса.
     * Подходит для города, улицы.
     */
    private class AddressCorrectionEditPanel extends AbstractCorrectionEditPanel {

        @EJB(name = "AddressCorrectionBean")
        private AddressCorrectionBean addressCorrectionBean;

        public AddressCorrectionEditPanel(String id, String entity, Long correctionId) {
            super(id, entity, correctionId);
        }

        @Override
        protected IModel<String> internalObjectLabel(Locale locale) {
            return new ResourceModel("address");
        }

        @Override
        protected Panel internalObjectPanel(String id) {
            Correction correction = getModel();
            String entity = correction.getEntity();
            SearchComponentState componentState = new SearchComponentState();
            if (!isNew()) {
                long objectId = correction.getObjectId();
                Strategy.RestrictedObjectInfo info = getStrategy(entity).findParentInSearchComponent(objectId, null);
                if (info != null) {
                    componentState = getStrategy(entity).getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    componentState.put(entity, findObject(objectId, entity));
                }
            }
            return new SearchComponent(id, componentState, getSearchFilters(entity), new Callback(correction, entity), true);
        }

        @Override
        protected String getNullObjectErrorMessage() {
            return getString("address_required");
        }

        @Override
        protected boolean validate() {
            boolean valid = true;

            if (!"city".equals(getModel().getEntity()) && getModel().getParentId() == null) {
                error(getString("correction_required"));
                valid = false;
            }
            return valid && super.validate();
        }

        protected Strategy getStrategy(String entity) {
            return strategyFactory.getStrategy(entity);
        }

        @Override
        protected Correction initObjectCorrection(String entity, Long correctionId) {
            if ("city".equals(entity)) {
                return addressCorrectionBean.getCityCorrection(correctionId);
            } else if ("street".equals(entity)) {
                return addressCorrectionBean.getStreetCorrection(correctionId);
            } else if ("district".equals(entity)) {
                return addressCorrectionBean.getDistrictCorrection(correctionId);
            }

            return super.initObjectCorrection(entity, correctionId);
        }

        @Override
        protected String displayCorrection() {
            Correction correction = getModel();

            boolean districtOrStreet = "street".equals(correction.getEntity()) || "district".equals(correction.getEntity());
            if (districtOrStreet && correction.getParent() != null) {
                return correction.getParent().getCorrection()
                        + ", " + correction.getCorrection();
            }

            return super.displayCorrection();
        }

        protected DomainObject findObject(long objectId, String entity) {
            DomainObjectExample example = new DomainObjectExample();
            example.setId(objectId);
            return getStrategy(entity).find(example).get(0);
        }

        protected List<String> getSearchFilters(String entity) {
            if (entity.equals("city")) {
                return ImmutableList.of("city");
            } else if (entity.equals("district")) {
                return ImmutableList.of("city", "district");
            } else if (entity.equals("street")) {
                return ImmutableList.of("city", "street");
            } else if (entity.equals("building")) {
                return ImmutableList.of("city", "street", "building");
            } else {
                return ImmutableList.of("city", "street", "building", "apartment");
            }
        }

        @Override
        protected Panel getCorrectionInputPanel(String id) {
            String entity = getModel().getEntity();
            if ("street".equals(entity) || "district".equals(entity)) {
                return new AddressCorrectionInputPanel(id, getModel());
            } else {
                return super.getCorrectionInputPanel(id);
            }
        }

        @Override
        protected boolean freezeOrganization() {
            return "city".equals(getModel().getEntity()) ? false : true;
        }

        @Override
        protected void back() {
            PageParameters parameters = new PageParameters();
            parameters.put(AddressCorrectionList.CORRECTED_ENTITY, getModel().getEntity());
            setResponsePage(AddressCorrectionList.class, parameters);
        }
    }

    /**
     * Панель редактирования коррекции дома.
     */
    private class BuildingCorrectionEditPanel extends AddressCorrectionEditPanel {

        @EJB(name = "AddressCorrectionBean")
        private AddressCorrectionBean addressCorrectionBean;

        public BuildingCorrectionEditPanel(String id, Long correctionId) {
            super(id, "building", correctionId);
        }

        @Override
        protected BuildingCorrection getModel() {
            return (BuildingCorrection) super.getModel();
        }

        @Override
        protected BuildingCorrection initObjectCorrection(String entity, Long correctionId) {
            return addressCorrectionBean.getBuildingCorrection(correctionId);
        }

        @Override
        protected BuildingCorrection newObjectCorrection(String entity) {
            return new BuildingCorrection();
        }

        @Override
        protected String displayCorrection() {
            BuildingCorrection correction = getModel();
            String parentAddress = "";
            if (correction.getParent() != null && correction.getParent().getParent() != null) {
                parentAddress = correction.getParent().getParent().getCorrection()
                        + ", " + correction.getParent().getCorrection() + ", ";
            }

            return parentAddress + BuildingFormatter.formatBuilding(correction.getCorrection(), correction.getCorrectionCorp(), getLocale());
        }

        @Override
        protected void back() {
            PageParameters parameters = new PageParameters();
            parameters.put(BuildingCorrectionList.CORRECTED_ENTITY, getModel().getEntity());
            setResponsePage(BuildingCorrectionList.class, parameters);
        }

        @Override
        protected void save() {
            addressCorrectionBean.insertBuilding(getModel());
        }

        @Override
        protected void update() {
            addressCorrectionBean.updateBuilding(getModel());
        }

        @Override
        protected void delete() {
            addressCorrectionBean.deleteBuilding(getModel());
        }

        @Override
        protected boolean validateExistence() {
            return addressCorrectionBean.checkExistence(getModel());
        }

        @Override
        protected Panel getCorrectionInputPanel(String id) {
            return new AddressCorrectionInputPanel(id, getModel());
        }
    }
    private AbstractCorrectionEditPanel addressEditPanel;

    public AddressCorrectionEdit(PageParameters params) {
        String entity = params.getString(CORRECTED_ENTITY);
        Long correctionId = params.getAsLong(CORRECTION_ID);
        addressEditPanel = entity.equals("building") ? new BuildingCorrectionEditPanel("addressEditPanel", correctionId)
                : new AddressCorrectionEditPanel("addressEditPanel", entity, correctionId);
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

