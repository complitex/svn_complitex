/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.web.component.correction.account.AccountNumberCorrectionPanel;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;

/**
 * Панель для корректировки номера л/c вручную, когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
 * @author Artem
 */
public class PaymentAccountNumberCorrectionPanel extends Panel {

    @EJB(name = "PersonAccountService")
    private PersonAccountService personAccountService;

    private Payment payment;

    private Dialog dialog;

    private IModel<String> accountNumberModel;

    private AccountNumberCorrectionPanel accountNumberCorrectionPanel;

    private List<AccountDetail> accountCorrectionDetails;

    private WebMarkupContainer infoContainer;

    public PaymentAccountNumberCorrectionPanel(String id, final MarkupContainer... toUpdate) {
        super(id);
        accountNumberModel = new Model<String>();

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(450);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        add(dialog);

        infoContainer = new WebMarkupContainer("infoContainer");
        infoContainer.setOutputMarkupId(true);

        dialog.add(infoContainer);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        infoContainer.add(messages);

        accountNumberCorrectionPanel = new AccountNumberCorrectionPanel("accountNumberCorrectionPanel",
                Model.ofList(accountCorrectionDetails)){
            @Override
            protected void correctAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                accountNumberModel.setObject(accountDetail.getAccountNumber());
            }

            @Override
            public boolean isVisible() {
                return getAccountCorrectionDetails() != null && !getAccountCorrectionDetails().isEmpty();
            }
        };

        infoContainer.add(accountNumberCorrectionPanel);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate()) {
                    personAccountService.correctAccountNumber(payment, accountNumberModel.getObject());

                    dialog.close(target);
                    if (toUpdate != null) {
                        for (MarkupContainer container : toUpdate) {
                            target.addComponent(container);
                        }
                    }
                } else {
                    target.addComponent(messages);
                }
            }

            @Override
            public boolean isVisible() {
                return getAccountCorrectionDetails() != null && !getAccountCorrectionDetails().isEmpty();
            }
        };
        infoContainer.add(save);

        AjaxLink cancel = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        };
        infoContainer.add(cancel);
    }

    private List<AccountDetail> getAccountCorrectionDetails() {
        return accountCorrectionDetails;
    }

    private boolean validate() {
        boolean validated = !Strings.isEmpty(accountNumberModel.getObject());
        if (!validated) {
            error(getString("account_number_required"));
        }
        return validated;
    }

    public void open(AjaxRequestTarget target, Payment payment) {
        this.payment = payment;

        accountCorrectionDetails = personAccountService.acquireAccountCorrectionDetails(payment);
        accountNumberCorrectionPanel.getAccountDetailsModel().setObject(accountCorrectionDetails);

        if (accountCorrectionDetails == null || accountCorrectionDetails.isEmpty()){
            error(getString("no_info"));
        }

        target.addComponent(infoContainer);

        dialog.open(target);
    }
}

