package org.complitex.correction.web.component;

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
import org.complitex.correction.entity.BuildingCorrection;
import org.complitex.correction.entity.DistrictCorrection;
import org.complitex.correction.entity.StreetCorrection;
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
import java.util.*;

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
        streetContainer.setVisible(isStreet || isBuilding);
        add(streetContainer);

        streetContainer.add(new Label("streetTypeLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("street_type").getEntityNames(), getLocale());
            }
        }).setVisible(!isBuilding));

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

        List<String> filter = isBuilding ? Arrays.asList("city", "street") : Arrays.asList("city");

        //City
        add(new WiQuerySearchComponent("search_component", new SearchComponentState(), filter, new ISearchCallback() {
            @Override
            public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
                Long cityObjectId = ids.get("city");
                Long streetObjectId = ids.get("street");

                if (correction instanceof DistrictCorrection){
                    ((DistrictCorrection) correction).setCityObjectId(cityObjectId);
                } else if (correction instanceof StreetCorrection){
                    ((StreetCorrection) correction).setCityObjectId(cityObjectId);
                } else if (correction instanceof BuildingCorrection){
                    ((BuildingCorrection) correction).setStreetObjectId(streetObjectId);
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
        streetTypeSelect.setVisible(!isBuilding);
        streetContainer.add(streetTypeSelect);

        //Street
        streetContainer.add(new TextField<>("street", new PropertyModel<String>(correction, "correction")).setVisible(isStreet));

        //Building
        buildingContainer.add(new TextField<>("building", new PropertyModel<String>(correction, "correction")));
        buildingContainer.add(new TextField<>("buildingCorp", new PropertyModel<String>(correction, "correctionCorp")));
    }
}
