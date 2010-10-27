/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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

    private ObjectCorrection newCorrection;

    private WebMarkupContainer form;

    public AbstractCorrectionEditPanel(String id, String entity, Long correctionId) {
        super(id);
        this.entity = entity;
        this.correctionId = correctionId;
        if (isNew()) {
            newCorrection = newModel();
        } else {
            newCorrection = initModel(this.entity, this.correctionId);
        }
        init();
    }

    protected boolean isNew() {
        return correctionId == null;
    }

    protected ObjectCorrection initModel(String entity, long correctionId) {
        ObjectCorrection correction = correctionBean.findById(entity, correctionId);
        correction.setEntity(entity);
        return correction;
    }

    protected ObjectCorrection newModel() {
        ObjectCorrection correction = new ObjectCorrection();
        correction.setEntity(entity);
        return correction;
    }

    protected ObjectCorrection getModel() {
        return newCorrection;
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
        correctionBean.insert(newCorrection);
    }

    protected void update() {
        correctionBean.update(newCorrection);
    }

    protected WebMarkupContainer getFormContainer() {
        return form;
    }

    protected void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);

        form = new Form("form");
        add(form);

        TextField<String> correction = new TextField<String>("correction", new PropertyModel<String>(newCorrection, "correction"));
        correction.setRequired(true);
        form.add(correction);

        TextField<String> code = new TextField<String>("code", new PropertyModel<String>(newCorrection, "code"));
        code.setRequired(true);
        form.add(code);

        final List<DomainObject> allOrganizations = organizationStrategy.getAll();
        IModel<DomainObject> organizationModel = new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                final Long organizationId = getModel().getOrganizationId();
                if (organizationId != null) {
                    return Iterables.find(allOrganizations, new Predicate<DomainObject>() {

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
                getModel().setOrganizationId(object.getId());
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization",
                organizationModel, allOrganizations, renderer);
        organization.setRequired(true);
        form.add(organization);

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
