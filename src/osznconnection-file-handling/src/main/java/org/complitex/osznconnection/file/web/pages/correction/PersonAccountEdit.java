/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.PersonAccount;
import org.complitex.osznconnection.file.service.PersonAccountLocalBean;

/**
 *
 * @author Artem
 */
public final class PersonAccountEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    @EJB(name = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;

    private Long correctionId;

    private PersonAccount newPersonAccount;

    public PersonAccountEdit(PageParameters params) {
        this.correctionId = params.getAsLong(CORRECTION_ID);
        if (isNew()) {
            newPersonAccount = new PersonAccount();
        } else {
            newPersonAccount = personAccountLocalBean.findById(this.correctionId);
        }
        init();
    }

    private boolean isNew() {
        return correctionId == null;
    }

    private void saveOrUpdate() {
        if (isNew()) {
            personAccountLocalBean.insert(newPersonAccount);
        } else {
            personAccountLocalBean.update(newPersonAccount);
        }
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);

        IModel<PersonAccount> model = new CompoundPropertyModel<PersonAccount>(newPersonAccount);
        Form<PersonAccount> form = new Form<PersonAccount>("form", model);
        add(form);

        form.add(new TextField<String>("lastName").setRequired(true));
        form.add(new TextField<String>("firstName").setRequired(true));
        form.add(new TextField<String>("middleName").setRequired(true));
        form.add(new TextField<String>("city").setRequired(true));
        form.add(new TextField<String>("street").setRequired(true));
        form.add(new TextField<String>("buildingNumber").setRequired(true));
        form.add(new TextField<String>("buildingCorp"));
        form.add(new TextField<String>("apartment").setRequired(true));
        form.add(new TextField<String>("accountNumber").setRequired(true));
        form.add(new TextField<String>("ownNumSr").setRequired(true));

        //save-cancel functional
        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                saveOrUpdate();
                setResponsePage(PersonAccountList.class);
            }
        };
        form.add(submit);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                setResponsePage(PersonAccountList.class);
            }
        };
        form.add(cancel);
    }
}

