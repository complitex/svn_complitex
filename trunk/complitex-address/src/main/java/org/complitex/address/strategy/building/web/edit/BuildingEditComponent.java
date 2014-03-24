package org.complitex.address.strategy.building.web.edit;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building.entity.BuildingCode;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.complitex.dictionary.web.component.search.CollapsibleInputSearchComponent;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class BuildingEditComponent extends AbstractComplexAttributesPanel {

    @EJB
    private StrategyFactory strategyFactory;

    private SearchComponentState districtSearchComponentState;
    private FeedbackPanel messages;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy<DomainObject> organizationStrategy;

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

    protected String getBuildingStrategyName() {
        return null;
    }

    @Override
    protected void init() {
        final FeedbackPanel feedbackPanel = findFeedbackPanel();
        final WebMarkupContainer attributesContainer = new WebMarkupContainer("attributesContainer");
        attributesContainer.setOutputMarkupId(true);
        add(attributesContainer);

        IStrategy buildingStrategy = strategyFactory.getStrategy(getBuildingStrategyName(), "building");
        IStrategy districtStrategy = strategyFactory.getStrategy("district");

        final Building building = (Building) getDomainObject();

        final boolean enabled = !isDisabled() && DomainObjectAccessUtil.canEdit(getBuildingStrategyName(),
                "building", building);

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

        //
        //Building Code
        //
        if (building.getId() == null) { // new building
            building.getBuildingCodes().add(new BuildingCode());
        }

        final List<DomainObject> allServicingOrganizations = organizationStrategy.getAllOuterOrganizations(getLocale());

        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };

        final WebMarkupContainer buildingOrganizationAssociationsContainer =
                new WebMarkupContainer("buildingOrganizationAssociationsContainer");
        buildingOrganizationAssociationsContainer.setVisible(!isDisabled() || !building.getBuildingCodes().isEmpty());
        add(buildingOrganizationAssociationsContainer);

        final WebMarkupContainer associationsUpdateContainer = new WebMarkupContainer("associationsUpdateContainer");
        associationsUpdateContainer.setOutputMarkupId(true);
        buildingOrganizationAssociationsContainer.add(associationsUpdateContainer);

        ListView<BuildingCode> associations =
                new AjaxRemovableListView<BuildingCode>("associations", building.getBuildingCodes()) {

                    @Override
                    protected void populateItem(ListItem<BuildingCode> item) {
                        final WebMarkupContainer fakeContainer = new WebMarkupContainer("fakeContainer");
                        item.add(fakeContainer);

                        final BuildingCode association = item.getModelObject();

                        //organization
                        IModel<DomainObject> organizationModel = new Model<DomainObject>() {

                            @Override
                            public DomainObject getObject() {
                                Long organizationId = association.getOrganizationId();
                                if (organizationId != null) {
                                    for (DomainObject o : allServicingOrganizations) {
                                        if (organizationId.equals(o.getId())) {
                                            return o;
                                        }
                                    }
                                }
                                return null;
                            }

                            @Override
                            public void setObject(DomainObject organization) {
                                association.setOrganizationId(organization != null
                                        ? organization.getId() : null);
                            }
                        };
                        //initialize model:
                        Long organizationId = association.getOrganizationId();
                        if (organizationId != null) {
                            for (DomainObject o : allServicingOrganizations) {
                                if (organizationId.equals(o.getId())) {
                                    organizationModel.setObject(o);
                                }
                            }
                        }

                        item.add(new OrganizationPicker("organization", organizationModel,
                                OrganizationTypeStrategy.SERVICING_ORGANIZATION_TYPE));

                        //building code
                        IModel<Integer> buildingCodeModel = new PropertyModel<Integer>(association, "buildingCode");
                        TextField<Integer> buildingCode = new TextField<>("buildingCode", buildingCodeModel);
                        buildingCode.setEnabled(enabled);
                        buildingCode.add(new AjaxFormComponentUpdatingBehavior("onblur") {

                            @Override
                            protected void onUpdate(AjaxRequestTarget target) {
                                target.add(associationsUpdateContainer);
                            }
                        });
                        item.add(buildingCode);

                        //remove link
                        addRemoveLink("removeAssociation", item, null, associationsUpdateContainer).setVisible(enabled);
                    }

                    @Override
                    protected boolean approveRemoval(ListItem<BuildingCode> item) {
                        return building.getBuildingCodes().size() > 1;
                    }
                };
        associationsUpdateContainer.add(associations);
        AjaxLink<Void> addAssociation = new AjaxLink<Void>("addAssociation") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                building.getBuildingCodes().add(new BuildingCode());
                target.add(associationsUpdateContainer);
            }
        };
        addAssociation.setVisible(enabled);
        buildingOrganizationAssociationsContainer.add(addAssociation);
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

    public boolean isBuildingOrganizationAssociationListEmpty() {
        return ((Building)getDomainObject()).getBuildingCodes().isEmpty();
    }

    public boolean isBuildingOrganizationAssociationListHasNulls() {
        for (BuildingCode buildingCode : ((Building)getDomainObject()).getBuildingCodes()) {
            if (buildingCode == null || buildingCode.getOrganizationId() == null || buildingCode.getBuildingCode() == null) {
                return true;
            }
        }
        return false;
    }
}
