package org.complitex.osznconnection.information.strategy.building.web.edit;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.StringCulture;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.service.LocaleBean;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionaryfw.strategy.web.CanEditUtil;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.StringCulturePanel;
import org.complitex.dictionaryfw.web.component.list.AjaxRemovableListView;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
import org.complitex.osznconnection.information.strategy.district.DistrictStrategy;
import org.complitex.osznconnection.information.strategy.street.StreetStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class BuildingEditComponent extends AbstractComplexAttributesPanel {

    private static final Logger log = LoggerFactory.getLogger(BuildingEditComponent.class);

    @EJB(name = "LocaleBean")
    private LocaleBean localeBean;

    @EJB(name = "BuildingStrategy")
    private BuildingStrategy buildingStrategy;

    @EJB(name = "StreetStrategy")
    private StreetStrategy streetStrategy;

    @EJB(name = "DistrictStrategy")
    private DistrictStrategy districtStrategy;

    public BuildingEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    private class SynchronizedSearchComponentState extends SearchComponentState {

        private SearchComponentState parentComponentState;

        private DomainObject object;

        private String entity;

        public SynchronizedSearchComponentState(SearchComponentState parentComponentState, String entity, DomainObject object) {
            this.parentComponentState = parentComponentState;
            this.object = object;
            this.entity = entity;
        }

        @Override
        public void updateState(Map<String, DomainObject> state) {
            Map<String, DomainObject> filterState = Maps.filterKeys(state, new Predicate<String>() {

                @Override
                public boolean apply(String entity) {
                    return buildingStrategy.getParentSearchFilters().contains(entity);
                }
            });
            parentComponentState.updateState(filterState);
            this.object = state.get(entity);
        }

        @Override
        public DomainObject get(String entity) {
            if (!entity.equals(this.entity) || (object == null)) {
                return parentComponentState.get(entity);
            } else {
                return object;
            }
        }

        @Override
        public void put(String entity, DomainObject object) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StreetSearchCallback implements ISearchCallback, Serializable {

        private BuildingAttribute buildingAttribute;

        public StreetSearchCallback(BuildingAttribute buildingAttribute) {
            this.buildingAttribute = buildingAttribute;
        }

        @Override
        public void found(SearchComponent component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            Long streetId = ids.get("street");
            if (streetId != null && streetId > 0) {
                buildingAttribute.getStreet().setValueId(streetId);
            } else {
                buildingAttribute.getStreet().setValueId(null);
            }

            component.findParent(DomainObjectInputPanel.class).visitChildren(SearchComponent.class, new IVisitor<SearchComponent>() {

                @Override
                public Object component(SearchComponent searchComponent) {
                    if (target != null) {
                        target.addComponent(searchComponent);
                    }
                    return CONTINUE_TRAVERSAL;
                }
            });
        }
    }

    private class DistrictSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            Long districtId = ids.get("district");
            if (districtId != null && districtId > 0) {
                districtAttribute.setValueId(districtId);
            } else {
                districtAttribute.setValueId(null);
            }

            component.findParent(DomainObjectInputPanel.class).visitChildren(SearchComponent.class, new IVisitor<SearchComponent>() {

                @Override
                public Object component(SearchComponent searchComponent) {
                    if (target != null) {
                        target.addComponent(searchComponent);
                    }
                    return CONTINUE_TRAVERSAL;
                }
            });
        }
    }

    private Attribute districtAttribute;

    @Override
    protected void init() {
        final WebMarkupContainer attributesContainer = new WebMarkupContainer("attributesContainer");
        attributesContainer.setOutputMarkupId(true);
        add(attributesContainer);

        final BuildingAttributeList list = new BuildingAttributeList(getInputPanel().getObject(), localeBean.getAllLocales());
        AjaxLink add = new AjaxLink("add") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                list.addNew(buildingStrategy);
                target.addComponent(attributesContainer);
            }
        };
        add.setVisible(!isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject()));
        add(add);

        final SearchComponentState parentSearchComponentState = getInputPanel().getParentSearchComponentState();
        final Long cityId = parentSearchComponentState.get("city") != null ? parentSearchComponentState.get("city").getId() : null;

        //district
        Long districtId = null;
        districtAttribute = Iterables.find(getInputPanel().getObject().getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(BuildingStrategy.DISTRICT);
            }
        });
        districtId = districtAttribute.getValueId();
        DomainObject district = null;
        if (districtId != null) {
            DomainObjectExample example = new DomainObjectExample();
            example.setId(districtId);
            districtStrategy.configureExample(example, ImmutableMap.of("city", cityId), null);
            district = districtStrategy.find(example).get(0);
        }
        attributesContainer.add(new SearchComponent("district", new SynchronizedSearchComponentState(parentSearchComponentState, "district", district),
                ImmutableList.of("country", "region", "city", "district"), new DistrictSearchCallback(),
                !isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject())));

        ListView<BuildingAttribute> buildingAttributes = new AjaxRemovableListView<BuildingAttribute>("buildingAttributes", list) {

            @Override
            protected void populateItem(ListItem<BuildingAttribute> item) {
                BuildingAttribute buildingAttribute = item.getModelObject();

                item.add(newStringPanel("number", buildingAttribute.getNumber(), new ResourceModel("number"), true,
                        !isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject())));
                item.add(newStringPanel("corp", buildingAttribute.getCorp(), new ResourceModel("corp"), false,
                        !isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject())));
                item.add(newStringPanel("structure", buildingAttribute.getStructure(), new ResourceModel("structure"), false,
                        !isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject())));

                DomainObject street = null;
                Long streetId = buildingAttribute.getStreet().getValueId();
                if (streetId != null) {
                    DomainObjectExample example = new DomainObjectExample();
                    example.setId(streetId);
                    example.setStart(0);
                    example.setSize(1);
                    streetStrategy.configureExample(example, ImmutableMap.of("city", cityId), null);
                    street = streetStrategy.find(example).get(0);
                }

                item.add(new SearchComponent("street", new SynchronizedSearchComponentState(parentSearchComponentState, "street", street),
                        buildingStrategy.getSearchFilters(), new StreetSearchCallback(buildingAttribute),
                        !isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject())));

                addRemoveLink("remove", item, null, attributesContainer).setVisible(!isDisabled() && CanEditUtil.canEdit(getInputPanel().getObject()));
            }
        };
        attributesContainer.add(buildingAttributes);
    }

    private static StringCulturePanel newStringPanel(String id, Attribute attr, IModel<String> labelModel, boolean required, boolean enabled) {
        IModel<List<StringCulture>> model = new PropertyModel<List<StringCulture>>(attr, "localizedValues");
        return new StringCulturePanel(id, model, required, labelModel, enabled);
    }
}
