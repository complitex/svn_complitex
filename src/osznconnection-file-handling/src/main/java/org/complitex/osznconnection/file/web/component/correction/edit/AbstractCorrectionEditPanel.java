/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.service.CorrectionBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.web.model.OrganizationModel;

/**
 * Абстрактная панель для редактирования коррекций.
 * @author Artem
 */
public abstract class AbstractCorrectionEditPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(AbstractCorrectionEditPanel.class);

    @EJB(name = "CorrectionBean")
    private CorrectionBean correctionBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;
    
    private Long correctionId;
    private Correction correction;
    private WebMarkupContainer form;

    public AbstractCorrectionEditPanel(String id, String entity, Long correctionId) {
        super(id);
        this.correctionId = correctionId;
        if (isNew()) {
            correction = newObjectCorrection(entity);
        } else {
            correction = initObjectCorrection(entity, this.correctionId);
            correction.setEntity(entity);
        }
        init();
    }

    public boolean isNew() {
        return correctionId == null;
    }

    protected Correction initObjectCorrection(String entity, Long correctionId) {
        return correctionBean.findById(entity, correctionId);
    }

    protected Correction newObjectCorrection(String entity) {
        return new Correction(entity);
    }

    protected Correction getModel() {
        return correction;
    }

    protected String displayCorrection() {
        return correction.getCorrection();
    }

    protected abstract IModel<String> internalObjectLabel(Locale locale);

    protected abstract Panel internalObjectPanel(String id);

    protected abstract String getNullObjectErrorMessage();

    protected String getNullCorrectionErroMessage(){
        return new StringResourceModel("Required", Model.ofMap(ImmutableMap.of("label", getString("correction")))).getObject();
    };

    protected boolean freezeOrganization(){
        return false;
    }

    protected boolean validate() {
        boolean valid = true;
        if (Strings.isEmpty(getModel().getCorrection())) {
            error(getNullCorrectionErroMessage());
            valid = false;
        }

        if (getModel().getObjectId() == null) {
            error(getNullObjectErrorMessage());
            valid = false;
        }

        if (valid && validateExistence()) {
            error(getString("exist"));
            valid = false;
        }
        return valid;
    }

    protected boolean validateExistence(){
        return correctionBean.checkExistence(getModel());
    }

    protected abstract void back();

    protected void saveOrUpdate() {
        try {
            if (isNew()) {
                save();
            } else {
                update();
            }
            back();
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
        }
    }

    protected void save() {
        correctionBean.insert(correction);
    }

    protected void update() {
        correctionBean.update(correction);
    }

    protected void delete() {
        correctionBean.delete(correction);
    }

    public void executeDeletion() {
        try {
            delete();
            back();
        } catch (Exception e) {
            error(getString("db_error"));
            log.error("", e);
        }
    }

    protected WebMarkupContainer getFormContainer() {
        return form;
    }

    protected boolean isOrganizationCodeRequired() {
        return false;
    }

    protected Panel getCorrectionInputPanel(String id) {
        return new DefaultCorrectionInputPanel(id, new PropertyModel<String>(getModel(), "correction"));
    }

    protected void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);

        form = new Form("form");
        add(form);

        WebMarkupContainer codeRequiredContainer = new WebMarkupContainer("codeRequiredContainer");
        form.add(codeRequiredContainer);

        boolean isOrganizationCodeRequired = isOrganizationCodeRequired();

        codeRequiredContainer.setVisible(isOrganizationCodeRequired);

        TextField<String> code = new TextField<String>("code", new PropertyModel<String>(correction, "code"));
        code.setRequired(isOrganizationCodeRequired);

        form.add(code);

        final IModel<List<DomainObject>> allOuterOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOuterOrganizations(getLocale());
            }
        };

        IModel<DomainObject> outerOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getOrganizationId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                correction.setOrganizationId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOuterOrganizationsModel.getObject();
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        final DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization",
                outerOrganizationModel, allOuterOrganizationsModel, renderer);
        if (freezeOrganization()) {
            organization.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    organization.setEnabled(false);
                    target.addComponent(organization);
                }
            });
        }
        organization.setRequired(true);
        organization.setEnabled(isNew());
        form.add(organization);

        if (isNew()) {
            correction.setInternalOrganizationId(OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
        }

        final List<DomainObject> internalOrganizations = Lists.newArrayList(organizationStrategy.getItselfOrganization());
        IModel<DomainObject> internalOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getInternalOrganizationId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return internalOrganizations;
            }
        };
        DisableAwareDropDownChoice<DomainObject> internalOrganization = new DisableAwareDropDownChoice<DomainObject>("internalOrganization",
                internalOrganizationModel, internalOrganizations, renderer);
        internalOrganization.setEnabled(false);
        form.add(internalOrganization);

        form.add(new Label("internalObjectLabel", internalObjectLabel(getLocale())));
        form.add(internalObjectPanel("internalObject"));

        // correction input panel
        form.add(getCorrectionInputPanel("correctionInput").setVisible(isNew()));
        //correction label
        form.add(new Label("correctionLabel", displayCorrection()).setVisible(!isNew()));

        //save-cancel functional
        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                if (AbstractCorrectionEditPanel.this.validate()) {
                    saveOrUpdate();
                }
            }
        };
        form.add(submit);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back();
            }
        };
        form.add(cancel);
    }
}

