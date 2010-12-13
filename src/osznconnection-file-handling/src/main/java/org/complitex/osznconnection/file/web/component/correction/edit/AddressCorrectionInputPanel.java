/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionaryfw.service.EntityBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.web.component.AbstractAutoCompleteTextField;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;

/**
 *
 * @author Artem
 */
public final class AddressCorrectionInputPanel extends Panel {

    @EJB(name = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;
    
    @EJB(name = "EntityBean")
    private EntityBean entityBean;

    private static class CorrectionRenderer extends AbstractAutoCompleteTextRenderer<Correction> {

        @Override
        protected String getTextValue(Correction correction) {
            return asText(correction);
        }

        public static String asText(Correction correction) {
            return correction.getCorrection();
        }
    }
    private static final CorrectionRenderer CORRECTION_RENDERER = new CorrectionRenderer();

    private static abstract class AutoCompleteCorrectionTextField extends AbstractAutoCompleteTextField<Correction> {

        public AutoCompleteCorrectionTextField(String id, CorrectionModel model, AutoCompleteSettings settings) {
            super(id, null, String.class, CORRECTION_RENDERER, settings);
            model.setAutoComplete(this);
            this.setModel(model);
        }

        @Override
        protected String getChoiceValue(Correction choice) throws Throwable {
            return CorrectionRenderer.asText(choice);
        }
    }

    private static class CorrectionModel extends Model<String> {

        private AutoCompleteCorrectionTextField autoComplete;
        private IModel<Correction> model;

        public CorrectionModel(IModel<Correction> model) {
            this.model = model;
        }

        @Override
        public String getObject() {
            Correction object = model.getObject();
            if (object != null) {
                return CorrectionRenderer.asText(object);
            }
            return null;
        }

        @Override
        public void setObject(String object) {
            model.setObject(autoComplete.findChoice());
        }

        private void setAutoComplete(AutoCompleteCorrectionTextField autoComplete) {
            this.autoComplete = autoComplete;
        }
    }
    private static final int AUTO_COMPLETE_SIZE = 10;
    private static final AutoCompleteSettings settings = new AutoCompleteSettings();

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

        final IModel<Correction> cityModel = new Model<Correction>();
        AutoCompleteCorrectionTextField city = new AutoCompleteCorrectionTextField("city", new CorrectionModel(cityModel), settings) {

            @Override
            protected List<Correction> getChoiceList(String cityInput) {
                if (correction.getOrganizationId() != null) {
                    CorrectionExample example = createExample(cityInput, correction.getOrganizationId(), null);
                    List<Correction> cityCorrections = addressCorrectionBean.getCityCorrections(example);
                    return cityCorrections;
                }
                return Collections.emptyList();
            }
        };
        city.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        add(city);

        WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        districtContainer.setVisible(isDistrict);
        add(districtContainer);
        districtContainer.add(new TextField("district", new PropertyModel<String>(correction, "correction") {

            @Override
            public void setObject(String object) {
                super.setObject(object);
                correction.setParentId(cityModel.getObject().getId());
            }
        }));

        WebMarkupContainer streetContainer = new WebMarkupContainer("streetContainer");
        streetContainer.setVisible(isStreet || isBuilding);
        add(streetContainer);
        final IModel<Correction> streetModel = new Model<Correction>();
        TextField street = null;
        if (isBuilding) {
            street = new AutoCompleteCorrectionTextField("street", new CorrectionModel(streetModel), settings) {

                @Override
                protected List<Correction> getChoiceList(String streetInput) {
                    Correction cityCorrection = cityModel.getObject();
                    if (cityCorrection != null && correction.getOrganizationId() != null) {
                        CorrectionExample example = createExample(streetInput, correction.getOrganizationId(), cityCorrection.getId());
                        List<Correction> streetCorrections = addressCorrectionBean.getStreetCorrections(example);
                        return streetCorrections;
                    }
                    return Collections.emptyList();
                }
            };
            street.add(new AjaxFormComponentUpdatingBehavior("onblur") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });
        } else {
            street = new TextField<String>("street", new PropertyModel<String>(correction, "correction") {

                @Override
                public void setObject(String object) {
                    super.setObject(object);
                    correction.setParentId(cityModel.getObject().getId());
                }
            });
        }
        streetContainer.add(street);

        WebMarkupContainer buildingContainer = new WebMarkupContainer("buildingContainer");
        buildingContainer.setVisible(isBuilding);
        add(buildingContainer);
        TextField<String> building = new TextField<String>("building", new PropertyModel<String>(correction, "correction") {

            @Override
            public void setObject(String object) {
                super.setObject(object);
                correction.setParentId(streetModel.getObject().getId());
            }
        });
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
