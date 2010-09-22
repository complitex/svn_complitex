/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.account;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountCorrectionDetail;

/**
 *
 * @author Artem
 */
public abstract class AccountNumberCorrectionPanel extends Panel {

    private IModel<AccountCorrectionDetail> model;

    public AccountNumberCorrectionPanel(String id, List<AccountCorrectionDetail> accountCorrectionDetails) {
        super(id);
        init(accountCorrectionDetails);
    }

    private void init(List<AccountCorrectionDetail> accountCorrectionDetails) {
        boolean needChoose = accountCorrectionDetails != null && !accountCorrectionDetails.isEmpty();

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        IModel<String> labelModel = new ResourceModel(needChoose ? "label" : "no_info");
        Label label = new Label("label", labelModel);
        add(label);

        model = new Model<AccountCorrectionDetail>();

        Form form = new Form("form") {

            @Override
            protected void onSubmit() {
                correctAccountNumber(model.getObject().getAccountNumber());
                back();
            }
        };
        add(form);

        IChoiceRenderer<AccountCorrectionDetail> renderer = new IChoiceRenderer<AccountCorrectionDetail>() {

            @Override
            public Object getDisplayValue(AccountCorrectionDetail object) {
                StringBuilder displayValueBuilder = new StringBuilder().append(object.getAccountNumber());

                if (!Strings.isEmpty(object.getOwnerName())) {
                    displayValueBuilder.append(", ").append(object.getOwnerName());
                }
                if (!Strings.isEmpty(object.getOwnerINN())) {
                    displayValueBuilder.append(", ").append(getString("INN")).append(" : ").append(object.getOwnerINN());
                }
                return displayValueBuilder.toString();
            }

            @Override
            public String getIdValue(AccountCorrectionDetail object, int index) {
                return object.getAccountNumber();
            }
        };
        RadioChoice<AccountCorrectionDetail> accountDetails = new RadioChoice<AccountCorrectionDetail>("accountDetails", model,
                accountCorrectionDetails, renderer);
        accountDetails.setRequired(true);
        form.add(accountDetails);

        AjaxSubmitLink save = new AjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(messages);
            }
        };
        form.add(save);

        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back();
            }
        };
        form.add(cancel);

        Link back = new Link("back") {

            @Override
            public void onClick() {
                back();
            }
        };
        add(back);

        back.setVisible(!needChoose);
        form.setVisible(needChoose);
    }

    protected abstract void back();

    protected abstract void correctAccountNumber(String accountNumber);
}
