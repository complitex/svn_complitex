package org.complitex.correction.web.component;

import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.correction.entity.*;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.EntityBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;

import javax.ejb.EJB;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class AddressCorrectionInputPanel extends Panel {
    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    @EJB
    private StringCultureBean stringBean;

    @EJB
    private EntityBean entityBean;

    @EJB
    private StreetTypeStrategy streetTypeStrategy;

    public AddressCorrectionInputPanel(String id, final Correction correction) {
        super(id);

        final boolean isDistrict = "district".equals(correction.getEntity());
        final boolean isStreet = "street".equals(correction.getEntity());
        final boolean isBuilding = "building".equals(correction.getEntity());
        final boolean isApartment = "apartment".equals(correction.getEntity());
        final boolean isRoom = "room".equals(correction.getEntity());

        //District
        final WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        add(districtContainer);
        districtContainer.setVisible(isDistrict);

        districtContainer.add(new Label("districtLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("district").getEntityNames(), getLocale());
            }
        }));
        districtContainer.add(new TextField<>("district", new PropertyModel<String>(correction, "correction"))
                .setOutputMarkupId(true));

        //Street
        final WebMarkupContainer streetContainer = new WebMarkupContainer("streetContainer");
        streetContainer.setOutputMarkupId(true);
        streetContainer.setVisible(isStreet || isBuilding || isApartment || isRoom);
        add(streetContainer);

        streetContainer.add(new Label("streetTypeLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("street_type").getEntityNames(), getLocale());
            }
        }).setVisible(isStreet));

        streetContainer.add(new Label("streetLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("street").getEntityNames(), getLocale());
            }
        }).setVisible(isStreet));

        //Building
        final WebMarkupContainer buildingContainer = new WebMarkupContainer("buildingContainer");
        buildingContainer.setVisible(isBuilding);
        add(buildingContainer);

        buildingContainer.add(new Label("buildingLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("building").getEntityNames(), getLocale());
            }
        }));

        //Building corp
        final WebMarkupContainer buildingCorpContainer = new WebMarkupContainer("buildingCorpContainer");
        buildingCorpContainer.setVisible(isBuilding);
        add(buildingCorpContainer);

        //Apartment
        final WebMarkupContainer apartmentContainer = new WebMarkupContainer("apartmentContainer");
        apartmentContainer.setVisible(isApartment);
        add(apartmentContainer);

        apartmentContainer.add(new Label("apartmentLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("apartment").getEntityNames(), getLocale());
            }
        }));

        //Room
        final WebMarkupContainer roomContainer = new WebMarkupContainer("roomContainer");
        roomContainer.setVisible(isRoom);
        add(roomContainer);

        roomContainer.add(new Label("roomLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("room").getEntityNames(), getLocale());
            }
        }));

        List<String> filter = isBuilding || isApartment || isRoom ? Lists.newArrayList("city", "street") : Lists.newArrayList("city");
        if (isApartment || isRoom) {
            filter.add("building");
        }
        if (isRoom) {
            filter.add("apartment");
        }

        //City
        add(new WiQuerySearchComponent("search_component", new SearchComponentState(), filter, new ISearchCallback() {
            @Override
            public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
                Long cityObjectId = ids.get("city");
                Long streetObjectId = ids.get("street");
                Long buildingObjectId = ids.get("building");
                Long apartmentObjectId = ids.get("apartment");

                if (correction instanceof DistrictCorrection){
                    ((DistrictCorrection) correction).setCityObjectId(cityObjectId);
                } else if (correction instanceof StreetCorrection){
                    ((StreetCorrection) correction).setCityObjectId(cityObjectId);
                } else if (correction instanceof BuildingCorrection){
                    ((BuildingCorrection) correction).setStreetObjectId(streetObjectId);
                } else if (correction instanceof ApartmentCorrection){
                    ((ApartmentCorrection) correction).setBuildingObjectId(buildingObjectId);
                } else if (correction instanceof RoomCorrection && apartmentObjectId != null && apartmentObjectId > 0){
                    ((RoomCorrection) correction).setApartmentObjectId(apartmentObjectId);
                } else if (correction instanceof RoomCorrection && buildingObjectId != null && buildingObjectId > 0){
                    ((RoomCorrection) correction).setBuildingObjectId(buildingObjectId);
                }
            }
        }, ShowMode.ACTIVE, true));

        //StreetType
        DomainObjectExample example = new DomainObjectExample();
        List<? extends DomainObject> streetTypes = streetTypeStrategy.find(example);
        Collections.sort(streetTypes, new Comparator<DomainObject>() {
            @Override
            public int compare(DomainObject o1, DomainObject o2) {
                return streetTypeStrategy.displayFullName(o1, getLocale())
                        .compareTo(streetTypeStrategy.displayFullName(o2, getLocale()));
            }
        });

        final IModel<DomainObject> streetTypeModel = new Model<>();
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return streetTypeStrategy.displayFullName(object, getLocale());
            }
        };

        DisableAwareDropDownChoice streetTypeSelect = new DisableAwareDropDownChoice<>("streetType", streetTypeModel,
                streetTypes, renderer);
        streetTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (correction instanceof StreetCorrection){
                    ((StreetCorrection) correction).setStreetTypeObjectId(streetTypeModel.getObject().getId());
                }
            }
        });
        streetTypeSelect.setVisible(isStreet);
        streetContainer.add(streetTypeSelect);

        //Street
        streetContainer.add(new TextField<>("street", new PropertyModel<String>(correction, "correction")).setVisible(isStreet));

        //Building
        buildingContainer.add(new TextField<>("building", new PropertyModel<String>(correction, "correction")));

        //Building corp
        buildingCorpContainer.add(new TextField<>("buildingCorp", new PropertyModel<String>(correction, "correctionCorp")));

        //Apartment
        apartmentContainer.add(new TextField<>("apartment", new PropertyModel<String>(correction, "correction")));
    
        //Room
        roomContainer.add(new TextField<>("room", new PropertyModel<String>(correction, "correction")));
    }
}
