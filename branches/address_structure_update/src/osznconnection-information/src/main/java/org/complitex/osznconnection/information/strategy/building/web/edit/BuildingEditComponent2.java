/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building.web.edit;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.CanEditUtil;
import org.complitex.dictionaryfw.util.CloneUtil;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.list.AjaxRemovableListView;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
import org.complitex.osznconnection.information.strategy.building.entity.Building;
import org.complitex.osznconnection.information.strategy.building_address.BuildingAddressStrategy;
import org.complitex.osznconnection.information.strategy.district.DistrictStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public final class BuildingEditComponent2 extends AbstractComplexAttributesPanel {

    private static final Logger log = LoggerFactory.getLogger(BuildingEditComponent2.class);

    @EJB(name = "BuildingStrategy")
    private BuildingStrategy buildingStrategy;

    @EJB(name = "DistrictStrategy")
    private DistrictStrategy districtStrategy;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;

    @EJB(name = "BuildingAddressStrategy")
    private BuildingAddressStrategy buildingAddressStrategy;

    private SearchComponentState districtComponentState;

    private class DistrictSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            DomainObject district = districtComponentState.get("district");
            Building building = (Building) getInputPanel().getObject();
            if (district != null && district.getId() > 0) {
                districtAttribute.setValueId(district.getId());
                building.setDistrict(district);
            } else {
                districtAttribute.setValueId(null);
                building.setDistrict(null);
            }
        }
    }

    public BuildingEditComponent2(String id, boolean disabled) {
        super(id, disabled);
    }

    private Attribute districtAttribute;

    @Override
    protected void init() {
        final WebMarkupContainer attributesContainer = new WebMarkupContainer("attributesContainer");
        attributesContainer.setOutputMarkupId(true);
        add(attributesContainer);

        final Building building = (Building) getInputPanel().getObject();

        final SearchComponentState parentSearchComponentState = getInputPanel().getParentSearchComponentState();

        //district
        Label districtLabel = new Label("districtLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(buildingStrategy.getEntity().getAttributeType(BuildingStrategy.DISTRICT).getAttributeNames(), getLocale());
            }
        });
        attributesContainer.add(districtLabel);
        districtComponentState = CloneUtil.cloneObject(parentSearchComponentState);
        Long districtId = null;
        districtAttribute = building.getAttribute(BuildingStrategy.DISTRICT);
        districtId = districtAttribute.getValueId();
        DomainObject district = null;
        if (districtId != null) {
            district = districtStrategy.findById(districtId);
            districtComponentState.put("district", district);
        }
        attributesContainer.add(new SearchComponent("district", districtComponentState,
                ImmutableList.of("country", "region", "city", "district"), new DistrictSearchCallback(),
                !isDisabled() && CanEditUtil.canEdit(building)));

        //primary building address
        DomainObject primaryBuildingAddress = building.getPrimaryAddress();
        DomainObjectInputPanel primaryAddressPanel = new DomainObjectInputPanel("primaryAddress", primaryBuildingAddress, "building_address",
                null, null) {

            @Override
            protected IModel<String> getParentLabelModel() {
                return new ResourceModel("street");
            }

            @Override
            public SearchComponentState getParentSearchComponentState() {
                final SearchComponentState superSearchComponentState = super.getParentSearchComponentState();

                return new SearchComponentState() {

                    @Override
                    public void clear() {
                        superSearchComponentState.clear();
                    }

                    @Override
                    public DomainObject get(String entity) {
                        return superSearchComponentState.get(entity);
                    }

                    @Override
                    public void put(String entity, DomainObject object) {
                        superSearchComponentState.put(entity, object);
                        if ("street".equals(entity) && object != null) {
                            building.setPrimaryStreet(object);
                        }
                    }

                    @Override
                    public void updateState(Map<String, DomainObject> state) {
                        superSearchComponentState.updateState(state);
                    }
                };
            }
        };
        attributesContainer.add(primaryAddressPanel);

        //alternative addresses
        ListView<DomainObject> alternativeAdresses = new AjaxRemovableListView<DomainObject>("alternativeAdresses",
                building.getAlternativeAddresses()) {

            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                DomainObject address = item.getModelObject();

                DomainObjectInputPanel alternativeAddess = new DomainObjectInputPanel("alternativeAddess", address, "building_address", null, null) {

                    @Override
                    protected IModel<String> getParentLabelModel() {
                        return new ResourceModel("street");
                    }
                };
                item.add(alternativeAddess);
                addRemoveLink("remove", item, null, attributesContainer).setVisible(!isDisabled() && CanEditUtil.canEdit(building));
            }
        };
        attributesContainer.add(alternativeAdresses);

        AjaxLink add = new AjaxLink("add") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                DomainObject newBuildingAddress = buildingAddressStrategy.newInstance();
                building.addAlternativeAddress(newBuildingAddress);
                target.addComponent(attributesContainer);
            }
        };
        add.setVisible(!isDisabled() && CanEditUtil.canEdit(building));
        add(add);
    }
}
