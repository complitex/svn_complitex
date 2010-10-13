/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.web.component.correction.account.AccountNumberCorrectionPanel;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

/**
 *
 * @author Artem
 */
public class PaymentAccountNumberCorrectionPanel extends Panel {

    @EJB(name = "PersonAccountService")
    private PersonAccountService personAccountService;

    private Payment payment;

    private Dialog dialog;

    private IModel<String> accountNumberModel;

    private MarkupContainer[] toUpdate;

    private boolean isPostBack;

    public PaymentAccountNumberCorrectionPanel(String id, Payment payment, MarkupContainer[] toUpdate) {
        super(id);
        this.payment = payment;
        accountNumberModel = new Model<String>();
        this.toUpdate = toUpdate;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        if (!isPostBack) {
            List<AccountDetail> accountCorrectionDetails = personAccountService.acquireAccountCorrectionDetails(payment);
            
            dialog = new Dialog("dialog");
            dialog.setModal(true);
            dialog.setWidth(450);
            dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                    chain("find", "'.ui-dialog-titlebar-close'").
                    chain("hide").render()));
            dialog.setCloseOnEscape(false);
            add(dialog);

            boolean infoExists = accountCorrectionDetails != null && !accountCorrectionDetails.isEmpty();

            WebMarkupContainer noInfo = new WebMarkupContainer("noInfo");
            noInfo.setVisible(!infoExists);
            dialog.add(noInfo);

            WebMarkupContainer infoContainer = new WebMarkupContainer("infoContainer");
            infoContainer.setVisible(infoExists);
            dialog.add(infoContainer);

            final FeedbackPanel messages = new FeedbackPanel("messages");
            messages.setOutputMarkupId(true);
            infoContainer.add(messages);

            infoContainer.add(new AccountNumberCorrectionPanel("accountNumberCorrectionPanel", Model.ofList(accountCorrectionDetails)) {

                @Override
                protected void correctAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                    accountNumberModel.setObject(accountDetail.getAccountNumber());
                }
            });

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
            };
            infoContainer.add(save);
            AjaxLink cancel = new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.close(target);
                }
            };
            infoContainer.add(cancel);
            isPostBack = true;
        }
    }

    private boolean validate() {
        boolean validated = !Strings.isEmpty(accountNumberModel.getObject());
        if (!validated) {
            error(getString("account_number_required"));
        }
        return validated;
    }

    public void open(AjaxRequestTarget target) {
        dialog.open(target);
    }
}

