/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.building.web.edit;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.visit.IVisit;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import org.apache.wicket.util.visit.IVisitor;
import org.complitex.dictionary.web.component.search.CollapsibleInputSearchComponent;

/**
 *
 * @author Artem
 */
public final class BuildingEditComponent extends AbstractComplexAttributesPanel {

    @EJB
    private StrategyFactory strategyFactory;
    private SearchComponentState districtSearchComponentState;
    private FeedbackPanel messages;

    public BuildingEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    private FeedbackPanel findFeedbackPanel() {
        if (messages == null) {
            messages = getPage().visitChildren(FeedbackPanel.class, new IVisitor<FeedbackPanel, FeedbackPanel>() {

                @Override
                public void component(FeedbackPanel object, IVisit<FeedbackPanel> visit) {
                    visit.stop(object);
                }
            });
        }
        return messages;
    }

    @Override
    protected void init() {
        final FeedbackPanel feedbackPanel = findFeedbackPanel();
        final WebMarkupContainer attributesContainer = new WebMarkupContainer("attributesContainer");
        attributesContainer.setOutputMarkupId(true);
        add(attributesContainer);

        IStrategy buildingStrategy = strategyFactory.getStrategy("building");
        IStrategy districtStrategy = strategyFactory.getStrategy("district");

        final Building building = (Building) getDomainObject();

        final boolean enabled = !isDisabled() && DomainObjectAccessUtil.canEdit(null, "building", building);

        final SearchComponentState parentSearchComponentState = getInputPanel().getParentSearchComponentState();

        //district
        final WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        attributesContainer.add(districtContainer);

        Label districtLabel = new Label("districtLabel",
                DomainObjectInputPanel.labelModel(buildingStrategy.getEntity().getAttributeType(BuildingStrategy.DISTRICT).
                getAttributeNames(), getLocale()));
        districtContainer.add(districtLabel);
        districtSearchComponentState = new SearchComponentState() {

            @Override
            public DomainObject put(String entity, DomainObject object) {
                super.put(entity, object);

                if ("district".equals(entity)) {
                    building.setDistrict(object);
                }
                return object;
            }
        };
        districtSearchComponentState.updateState(parentSearchComponentState);

        final Attribute districtAttribute = building.getAttribute(BuildingStrategy.DISTRICT);
        if (districtAttribute != null) {
            final Long districtId = districtAttribute.getValueId();
            if (districtId != null) {
                DomainObject district = districtStrategy.findById(districtId, true);
                districtSearchComponentState.put("district", district);
            }
        }
        districtContainer.add(new CollapsibleInputSearchComponent("district", districtSearchComponentState,
                ImmutableList.of("country", "region", "city", "district"), null, ShowMode.ACTIVE, enabled));
        districtContainer.setVisible(districtAttribute != null);

        //primary building address
        final DomainObject primaryBuildingAddress = building.getPrimaryAddress();
        DomainObjectInputPanel primaryAddressPanel = new DomainObjectInputPanel("primaryAddress", primaryBuildingAddress,
                "building_address", null, getInputPanel().getParentId(), getInputPanel().getParentEntity(), getInputPanel().getDate()) {

            @Override
            public SearchComponentState initParentSearchComponentState() {
                SearchComponentState primaryAddressComponentState = super.initParentSearchComponentState();

                if (primaryBuildingAddress.getId() == null) {
                    primaryAddressComponentState.updateState(parentSearchComponentState);
                }
                return primaryAddressComponentState;
            }
        };
        attributesContainer.add(primaryAddressPanel);

        //alternative addresses
        ListView<DomainObject> alternativeAdresses = new AjaxRemovableListView<DomainObject>("alternativeAdresses",
                building.getAlternativeAddresses()) {

            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                final DomainObject address = item.getModelObject();

                DomainObjectInputPanel alternativeAddress = new DomainObjectInputPanel("alternativeAddress", address,
                        "building_address", null, getInputPanel().getParentId(), getInputPanel().getParentEntity(),
                        getInputPanel().getDate()) {

                    @Override
                    public SearchComponentState initParentSearchComponentState() {
                        SearchComponentState alternativeAddressComponentState = null;
                        if (address.getId() == null) {
                            alternativeAddressComponentState = new SearchComponentState();
                            alternativeAddressComponentState.updateState(parentSearchComponentState);
                            alternativeAddressComponentState.put("street", null);
                        } else {
                            alternativeAddressComponentState = super.initParentSearchComponentState();
                        }
                        return alternativeAddressComponentState;
                    }
                };
                item.add(alternativeAddress);
                addRemoveSubmitLink("remove", findParent(Form.class), item, null, attributesContainer, feedbackPanel).
                        setVisible(enabled);
            }
        };
        attributesContainer.add(alternativeAdresses);

        AjaxSubmitLink add = new AjaxSubmitLink("add") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                IStrategy buildingAddressStrategy = strategyFactory.getStrategy("building_address");
                DomainObject newBuildingAddress = buildingAddressStrategy.newInstance();
                building.addAlternativeAddress(newBuildingAddress);

                target.add(attributesContainer);
                target.add(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
            }
        };
        add.setVisible(enabled);
        add(add);
    }

    @Override
    public void onInsert() {
        beforePersist();
    }

    @Override
    public void onUpdate() {
        beforePersist();
    }

    private void beforePersist() {
        final Attribute districtAttribute = getDomainObject().getAttribute(BuildingStrategy.DISTRICT);
        final DomainObject district = districtSearchComponentState.get("district");
        if (district != null && district.getId() > 0 && district.getId() > 0) {
            districtAttribute.setValueId(district.getId());
        } else {
            districtAttribute.setValueId(null);
        }
    }
}
