package org.complitex.correction.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.util.AddressRenderer;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.entity.CorrectionExample;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.service.EntityBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.odlabs.wiquery.ui.autocomplete.AutocompleteAjaxComponent;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static class CorrectionRenderer<T extends Correction> implements IChoiceRenderer<T> {

        @Override
        public Object getDisplayValue(T object) {
            return object.getCorrection();
        }

        @Override
        public String getIdValue(T object, int index) {
            return String.valueOf(object.getId());
        }
    }
    private static final int AUTO_COMPLETE_SIZE = 10;

    public AddressCorrectionInputPanel(String id, final Correction correction) {
        super(id);

        final boolean isDistrict = "district".equals(correction.getEntity());
        final boolean isStreet = "street".equals(correction.getEntity());
        final boolean isBuilding = "building".equals(correction.getEntity());

        add(new Label("cityLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("city").getEntityNames(), getLocale());
            }
        }));
        WebMarkupContainer districtLabelContainer = new WebMarkupContainer("districtLabelContainer");
        districtLabelContainer.setVisible(isDistrict);
        add(districtLabelContainer);
        districtLabelContainer.add(new Label("districtLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("district").getEntityNames(), getLocale());
            }
        }));
        WebMarkupContainer streetLabelContainer = new WebMarkupContainer("streetLabelContainer");
        streetLabelContainer.setVisible(isStreet || isBuilding);
        add(streetLabelContainer);
        streetLabelContainer.add(new Label("streetLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("street").getEntityNames(), getLocale());
            }
        }));

        WebMarkupContainer buildingLabelContainer = new WebMarkupContainer("buildingLabelContainer");
        buildingLabelContainer.setVisible(isBuilding);
        add(buildingLabelContainer);
        buildingLabelContainer.add(new Label("buildingLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("building").getEntityNames(), getLocale());
            }
        }));

        final WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        districtContainer.setVisible(isDistrict);
        add(districtContainer);
        districtContainer.add(new TextField<String>("district", new PropertyModel<String>(correction, "correction")).setOutputMarkupId(true));

        final WebMarkupContainer streetContainer = new WebMarkupContainer("streetContainer");
        streetContainer.setVisible(isStreet || isBuilding);
        add(streetContainer);

        final WebMarkupContainer buildingContainer = new WebMarkupContainer("buildingContainer");
        buildingContainer.setVisible(isBuilding);
        add(buildingContainer);

        final IModel<Correction> cityModel = new Model<Correction>();

        add(new AutocompleteAjaxComponent<Correction>("city", cityModel, new CorrectionRenderer<Correction>()) {

            {
                setAutoUpdate(true);
            }

            @Override
            public List<Correction> getValues(String term) {
                if (correction.getOrganizationId() != null) {
                    CorrectionExample example = createExample(term, correction.getOrganizationId(), null);
                    return addressCorrectionBean.findCityCorrections(example, correction.getUserOrganizationId());
                }
                return Collections.emptyList();
            }

            @Override
            public Correction getValueOnSearchFail(String input) {
                return null;
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (isStreet || isDistrict) {
                    Long cityId = cityModel.getObject() != null ? cityModel.getObject().getId() : null;
                    correction.setParentId(cityId);
                }

                if (districtContainer.isVisible()) {
                    target.focusComponent(districtContainer.get(0));
                } else if (streetContainer.isVisible()) {
                    target.focusComponent(((AutocompleteAjaxComponent) streetContainer.get(1)).getAutocompleteField());
                }
            }
        });

        final IModel<List<Correction>> allStreetTypeCorrectionsModel = new AbstractReadOnlyModel<List<Correction>>() {

            List<Correction> streetTypeCorrections;

            @Override
            public List<Correction> getObject() {
                if (correction.getOrganizationId() != null) {
                    if (streetTypeCorrections == null) {
                        streetTypeCorrections =
                                addressCorrectionBean.findStreetTypeCorrections(correction.getOrganizationId(),
                                correction.getUserOrganizationId());
                    }
                    return streetTypeCorrections;
                } else {
                    return Collections.emptyList();
                }
            }
        };
        IModel<Correction> streetTypeModel = new PropertyModel<Correction>(correction, "streetTypeCorrection") {

            @Override
            public void setObject(Correction object) {
                super.setObject(object);
            }
        };

        final DropDownChoice<Correction> streetType = new DropDownChoice<Correction>("streetType", streetTypeModel, allStreetTypeCorrectionsModel,
                new ChoiceRenderer<Correction>("correction", "id"));
        streetType.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        streetType.setOutputMarkupId(true);
        streetType.setVisible(isStreet);

        final IModel<StreetCorrection> streetModel = new Model<>();

        FormComponent<?> street;

        if (isBuilding) {
            IChoiceRenderer<StreetCorrection> streetCorrectionRenderer = new IChoiceRenderer<StreetCorrection>() {

                @Override
                public Object getDisplayValue(StreetCorrection object) {
                    String streetType = null;
                    if (object.getStreetTypeCorrection() != null) {
                        streetType = object.getStreetTypeCorrection().getCorrection();
                    }
                    if (Strings.isEmpty(streetType)) {
                        streetType = null;
                    }

                    return AddressRenderer.displayStreet(streetType, object.getCorrection(), getLocale());
                }

                @Override
                public String getIdValue(StreetCorrection object, int index) {
                    return object.getId() + "";
                }
            };

            street = new AutocompleteAjaxComponent<StreetCorrection>("street", streetModel, streetCorrectionRenderer) {

                {
                    setAutoUpdate(true);
                }

                @Override
                public List<StreetCorrection> getValues(String term) {
                    Correction cityCorrection = cityModel.getObject();
                    if (cityCorrection != null && correction.getOrganizationId() != null) {
                        CorrectionExample example = createExample(term, correction.getOrganizationId(), cityCorrection.getId());

                        return addressCorrectionBean.findStreetCorrections(example, correction.getUserOrganizationId());
                    }
                    return Collections.emptyList();
                }

                @Override
                public StreetCorrection getValueOnSearchFail(String input) {
                    return null;
                }

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    Long streetId = streetModel.getObject() != null ? streetModel.getObject().getId() : null;
                    correction.setParentId(streetId);

                    if (buildingContainer.isVisible()) {
                        target.focusComponent(buildingContainer.get(0));
                    }
                }
            };
        } else {
            street = new AutocompleteAjaxComponent<String>("street", new PropertyModel<String>(correction, "correction")) {

                @Override
                public List<String> getValues(String term) {
                    Correction cityCorrection = cityModel.getObject();
                    if (cityCorrection != null && correction.getOrganizationId() != null) {
                        CorrectionExample example = createExample(term, correction.getOrganizationId(), cityCorrection.getId());

                        List<String> list = new ArrayList<String>();

                        for (StreetCorrection c : addressCorrectionBean.findStreetCorrections(example,
                                correction.getUserOrganizationId())) {
                            list.add(c.getCorrection());
                        }

                        return list;
                    }
                    return Collections.emptyList();
                }

                @Override
                public String getValueOnSearchFail(String input) {
                    return input;
                }
            };
        }
        streetContainer.add(streetType);
        streetContainer.add(street);

        TextField<String> building = new TextField<String>("building", new PropertyModel<String>(correction, "correction"));
        building.setOutputMarkupId(true);
        buildingContainer.add(building);
        buildingContainer.add(new TextField<String>("buildingCorp", new PropertyModel<String>(correction, "correctionCorp")));
    }

    private CorrectionExample createExample(String correction, long organizationId, Long parentId) {
        CorrectionExample example = new CorrectionExample();
        example.setCorrection(correction);
        example.setOrganizationId(organizationId);
        example.setParentId(parentId);
        example.setSize(AUTO_COMPLETE_SIZE);
        return example;
    }
}
