/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
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
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class AddressCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTED_ENTITY = "entity";

    public static final String CORRECTION_ID = "correction_id";

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private class Callback implements ISearchCallback, Serializable {

        private ObjectCorrection correction;

        private String entity;

        public Callback(ObjectCorrection correction, String entity) {
            this.correction = correction;
            this.entity = entity;
        }

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(entity);
            if (id != null && id > 0) {
                correction.setInternalObjectId(id);
            }
        }
    }

    private class AddressCorrectionEditPanel extends AbstractCorrectionEditPanel {

        public AddressCorrectionEditPanel(String id, String entity, Long correctionId) {
            super(id, entity, correctionId);
        }

        @Override
        protected IModel<String> internalObjectLabel(Locale locale) {
            return new ResourceModel("address");
        }

        @Override
        protected Panel internalObjectPanel(String id) {
            ObjectCorrection correction = getModel();
            String entity = correction.getEntity();
            SearchComponentState componentState = new SearchComponentState();
            if (!isNew()) {
                long objectId = correction.getInternalObjectId();
                Strategy.RestrictedObjectInfo info = getStrategy(entity).findParentInSearchComponent(objectId, null);
                if (info != null) {
                    componentState = getStrategy(entity).getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                    componentState.put(entity, findObject(objectId, entity));
                }
            }
            return new SearchComponent(id, componentState, getSearchFilters(entity), new Callback(correction, entity), true);
        }

        @Override
        protected boolean validate() {
            boolean valid = getModel().getInternalObjectId() != null;
            if (!valid) {
                error(getString("address_required"));
            }
            return valid;
        }

        protected Strategy getStrategy(String entity) {
            return strategyFactory.getStrategy(entity);
        }

        protected DomainObject findObject(long objectId, String entity) {
            DomainObjectExample example = new DomainObjectExample();
            example.setId(objectId);
            DomainObject object = getStrategy(entity).find(example).get(0);
            return object;
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
        protected void back() {
            PageParameters parameters = new PageParameters();
            parameters.put(AddressCorrectionList.CORRECTED_ENTITY, getEntity());
            setResponsePage(AddressCorrectionList.class, parameters);
        }
    }

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
        protected BuildingCorrection initObjectCorrection(String entity, long correctionId) {
            BuildingCorrection correction = addressCorrectionBean.findBuildingById(correctionId);
            correction.setEntity("building");
            return correction;
        }

        @Override
        protected BuildingCorrection newObjectCorrection() {
            BuildingCorrection correction = new BuildingCorrection();
            correction.setEntity("building");
            return correction;
        }

        @Override
        protected void init() {
            super.init();

            TextField<String> correctionCorp = new TextField<String>("correctionCorp", new PropertyModel<String>(getModel(), "correctionCorp"));
            getFormContainer().add(correctionCorp);
        }

        @Override
        protected void back() {
            PageParameters parameters = new PageParameters();
            parameters.put(BuildingCorrectionList.CORRECTED_ENTITY, getEntity());
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
        public void delete() {
            addressCorrectionBean.deleteBuilding(getModel());
            back();
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
                addressEditPanel.delete();
            }

            @Override
            public boolean isVisible() {
                return !addressEditPanel.isNew();
            }
        });
        return toolbar;
    }
}

