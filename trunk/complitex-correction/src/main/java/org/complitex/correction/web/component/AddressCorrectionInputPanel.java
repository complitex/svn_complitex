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
import org.complitex.correction.entity.CityCorrection;
import org.complitex.correction.entity.StreetCorrection;
import org.complitex.correction.entity.StreetTypeCorrection;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;
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
        districtContainer.add(new TextField<>("district", new PropertyModel<String>(correction, "correction")).setOutputMarkupId(true));

        final WebMarkupContainer streetContainer = new WebMarkupContainer("streetContainer");
        streetContainer.setVisible(isStreet || isBuilding);
        add(streetContainer);

        final WebMarkupContainer buildingContainer = new WebMarkupContainer("buildingContainer");
        buildingContainer.setVisible(isBuilding);
        add(buildingContainer);

        final IModel<CityCorrection> cityModel = new Model<>();

        add(new AutocompleteAjaxComponent<CityCorrection>("city", cityModel, new CorrectionRenderer<CityCorrection>()) {

            {
                setAutoUpdate(true);
            }

            @Override
            public List<CityCorrection> getValues(String term) {
                if (correction.getOrganizationId() != null) {
                    return addressCorrectionBean.getCityCorrections(FilterWrapper.of(new CityCorrection(null, null, term,
                            correction.getOrganizationId(), correction.getUserOrganizationId(), correction.getModuleId())));
                }
                return Collections.emptyList();
            }

            @Override
            public CityCorrection getValueOnSearchFail(String input) {
                return null;
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (isStreet || isDistrict) {
                    Long cityId = cityModel.getObject() != null ? cityModel.getObject().getId() : null;
                }

                if (districtContainer.isVisible()) {
                    target.focusComponent(districtContainer.get(0));
                } else if (streetContainer.isVisible()) {
                    target.focusComponent(((AutocompleteAjaxComponent) streetContainer.get(1)).getAutocompleteField());
                }
            }
        });

        final IModel<List<StreetTypeCorrection>> allStreetTypeCorrectionsModel = new AbstractReadOnlyModel<List<StreetTypeCorrection>>() {

            List<StreetTypeCorrection> streetTypeCorrections;

            @Override
            public List<StreetTypeCorrection> getObject() {
                if (correction.getOrganizationId() != null) {
                    if (streetTypeCorrections == null) {
                        streetTypeCorrections =
                                addressCorrectionBean.getStreetTypeCorrections(FilterWrapper.of(new StreetTypeCorrection(
                                        null, null, null, correction.getOrganizationId(), correction.getUserOrganizationId(), null)));
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

        final DropDownChoice<Correction> streetType = new DropDownChoice<>("streetType", streetTypeModel, allStreetTypeCorrectionsModel,
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
                    Correction cityCorrection = cityModel.getObject(); //todo city correction from address dictionary

                    if (cityCorrection != null && correction.getOrganizationId() != null) {
                        return addressCorrectionBean.getStreetCorrections(FilterWrapper.of(
                                new StreetCorrection(null, null, null, null, term,
                                        correction.getOrganizationId(), correction.getUserOrganizationId(),
                                        correction.getModuleId())));
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
                        List<String> list = new ArrayList<>();

                        for (StreetCorrection c : addressCorrectionBean.getStreetCorrections(FilterWrapper.of(
                                new StreetCorrection(null, null, null, null, term,
                                        correction.getOrganizationId(), correction.getUserOrganizationId(),
                                        correction.getModuleId())))) {
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

        TextField<String> building = new TextField<>("building", new PropertyModel<String>(correction, "correction"));
        building.setOutputMarkupId(true);
        buildingContainer.add(building);
        buildingContainer.add(new TextField<>("buildingCorp", new PropertyModel<String>(correction, "correctionCorp")));
    }
}
