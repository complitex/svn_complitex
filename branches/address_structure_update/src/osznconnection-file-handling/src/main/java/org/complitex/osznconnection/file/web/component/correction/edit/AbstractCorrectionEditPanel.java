/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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
import org.apache.wicket.model.Model;
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

    private String entity;

    private Long correctionId;

    private Correction correction;

    private WebMarkupContainer form;

    public AbstractCorrectionEditPanel(String id, String entity, Long correctionId) {
        super(id);
        this.entity = entity;
        this.correctionId = correctionId;
        if (isNew()) {
            correction = newObjectCorrection();
        } else {
            correction = initObjectCorrection(this.entity, this.correctionId);
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

    protected Correction newObjectCorrection() {
        return new Correction(entity);
    }

    protected Correction getModel() {
        return correction;
    }

    protected String getEntity() {
        return entity;
    }

    protected String getDisplayCorrection(){
        return correction.getCorrection();
    }

    protected abstract IModel<String> internalObjectLabel(Locale locale);

    protected abstract Panel internalObjectPanel(String id);

    protected boolean validate() {
        return true;
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

    protected void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);

        form = new Form("form");
        add(form);

        form.add(new Label("correction", getDisplayCorrection()));

        WebMarkupContainer codeRequiredContainer = new WebMarkupContainer("codeRequiredContainer");
        form.add(codeRequiredContainer);

        boolean isOrganizationCodeRequired = isOrganizationCodeRequired();

        codeRequiredContainer.setVisible(isOrganizationCodeRequired);

        TextField<String> code = new TextField<String>("code", new PropertyModel<String>(this.correction, "code"));
        code.setRequired(isOrganizationCodeRequired);

        form.add(code);

        abstract class OrganizationModel extends Model<DomainObject> {

            @Override
            public DomainObject getObject() {
                final Long organizationId = getOrganizationId(AbstractCorrectionEditPanel.this.correction);
                if (organizationId != null) {
                    return Iterables.find(getOrganizations(), new Predicate<DomainObject>() {

                        @Override
                        public boolean apply(DomainObject object) {
                            return object.getId().equals(organizationId);
                        }
                    });
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                setOrganizationId(AbstractCorrectionEditPanel.this.correction, object.getId());
            }

            public abstract Long getOrganizationId(Correction objectCorrection);

            public abstract void setOrganizationId(Correction objectCorrection, Long organizationId);

            public abstract List<DomainObject> getOrganizations();
        }

        final List<DomainObject> allOuterOrganizations = organizationStrategy.getAllOuterOrganizations();
        IModel<DomainObject> outerOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId(Correction objectCorrection) {
                return objectCorrection.getOrganizationId();
            }

            @Override
            public void setOrganizationId(Correction objectCorrection, Long organizationId) {
                objectCorrection.setOrganizationId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOuterOrganizations;
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization",
                outerOrganizationModel, allOuterOrganizations, renderer);
        organization.setRequired(true);
        form.add(organization);

        if (isNew()) {
            this.correction.setInternalOrganizationId(OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
        }

        final List<DomainObject> internalOrganizations = Lists.newArrayList(organizationStrategy.getItselfOrganization());
        IModel<DomainObject> internalOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId(Correction objectCorrection) {
                return objectCorrection.getInternalOrganizationId();
            }

            @Override
            public void setOrganizationId(Correction objectCorrection, Long organizationId) {
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
