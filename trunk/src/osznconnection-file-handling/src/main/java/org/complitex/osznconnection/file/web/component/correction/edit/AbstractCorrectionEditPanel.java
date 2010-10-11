/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
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
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.service.CorrectionBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

/**
 *
 * @author Artem
 */
public abstract class AbstractCorrectionEditPanel extends Panel {

    @EJB(name = "CorrectionBean")
    private CorrectionBean correctionBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    private String entity;

    private Long correctionId;

    private ObjectCorrection objectCorrection;

    private WebMarkupContainer form;

    public AbstractCorrectionEditPanel(String id, String entity, Long correctionId) {
        super(id);
        this.entity = entity;
        this.correctionId = correctionId;
        if (isNew()) {
            objectCorrection = newObjectCorrection();
        } else {
            objectCorrection = initObjectCorrection(this.entity, this.correctionId);
        }
        init();
    }

    protected boolean isNew() {
        return correctionId == null;
    }

    protected ObjectCorrection initObjectCorrection(String entity, long correctionId) {
        ObjectCorrection correction = correctionBean.findById(entity, correctionId);
        correction.setEntity(entity);
        return correction;
    }

    protected ObjectCorrection newObjectCorrection() {
        ObjectCorrection correction = new ObjectCorrection();
        correction.setEntity(entity);
        return correction;
    }

    protected ObjectCorrection getModel() {
        return objectCorrection;
    }

    protected String getEntity() {
        return entity;
    }

    protected abstract IModel<String> internalObjectLabel(Locale locale);

    protected abstract Panel internalObjectPanel(String id);

    protected boolean validate() {
        return true;
    }

    protected abstract void back();

    protected void saveOrUpdate() {
        if (isNew()) {
            save();
        } else {
            update();
        }
    }

    protected void save() {
        correctionBean.insert(objectCorrection);
    }

    protected void update() {
        correctionBean.update(objectCorrection);
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

        TextField<String> correction = new TextField<String>("correction", new PropertyModel<String>(objectCorrection, "correction"));
        correction.setRequired(true);
        form.add(correction);

        WebMarkupContainer codeRequiredContainer = new WebMarkupContainer("codeRequiredContainer");
        form.add(codeRequiredContainer);
        boolean isOrganizationCodeRequired = isOrganizationCodeRequired();
        codeRequiredContainer.setVisible(isOrganizationCodeRequired);
        TextField<String> code = new TextField<String>("code", new PropertyModel<String>(objectCorrection, "code"));
        code.setRequired(isOrganizationCodeRequired);
        form.add(code);

        abstract class OrganizationModel extends Model<DomainObject> {

            @Override
            public DomainObject getObject() {
                final Long organizationId = getOrganizationId(objectCorrection);
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
                setOrganizationId(objectCorrection, object.getId());
            }

            public abstract Long getOrganizationId(ObjectCorrection objectCorrection);

            public abstract void setOrganizationId(ObjectCorrection objectCorrection, Long organizationId);

            public abstract List<DomainObject> getOrganizations();
        }

        final List<DomainObject> allOuterOrganizations = organizationStrategy.getAllOuterOrganizations();
        IModel<DomainObject> outerOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId(ObjectCorrection objectCorrection) {
                return objectCorrection.getOrganizationId();
            }

            @Override
            public void setOrganizationId(ObjectCorrection objectCorrection, Long organizationId) {
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
            objectCorrection.setInternalOrganizationId(OrganizationStrategy.ITSELF_ORGANIZATION_OBJECT_ID);
        }
        final List<DomainObject> internalOrganizations = Lists.newArrayList(organizationStrategy.getItselfOrganization());
        IModel<DomainObject> internalOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId(ObjectCorrection objectCorrection) {
                return objectCorrection.getInternalOrganizationId();
            }

            @Override
            public void setOrganizationId(ObjectCorrection objectCorrection, Long organizationId) {
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

        Label internalObjectLabel = new Label("internalObjectLabel", internalObjectLabel(getLocale()));
        form.add(internalObjectLabel);
        Panel internalObject = internalObjectPanel("internalObject");
        form.add(internalObject);

        //save-cancel functional
        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                if (AbstractCorrectionEditPanel.this.validate()) {
                    saveOrUpdate();
                    back();
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

