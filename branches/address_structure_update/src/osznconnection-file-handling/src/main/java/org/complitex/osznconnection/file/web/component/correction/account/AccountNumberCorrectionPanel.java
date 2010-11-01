/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.account;

import java.util.List;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.AccountDetail;

/**
 * Панель для показа возможных вариантов выбора л/c по детальной информации,
 * когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
 * @author Artem
 */
public abstract class AccountNumberCorrectionPanel extends Panel {

    private static final String RESOURCE_BUNDLE = AccountNumberCorrectionPanel.class.getName();

    public AccountNumberCorrectionPanel(String id, IModel<List<? extends AccountDetail>> accountDetailsModel) {
        super(id);
        init(accountDetailsModel);
    }

    private void init(IModel<List<? extends AccountDetail>> accountDetailsModel) {
        final IModel<AccountDetail> model = new Model<AccountDetail>();
        IChoiceRenderer<AccountDetail> renderer = new IChoiceRenderer<AccountDetail>() {

            @Override
            public Object getDisplayValue(AccountDetail object) {
                return displayAccountDetail(object);
            }

            @Override
            public String getIdValue(AccountDetail object, int index) {
                return object.getAccountNumber();
            }
        };
        RadioChoice<AccountDetail> accounts = new RadioChoice<AccountDetail>("accounts", model,
                accountDetailsModel, renderer);
        accounts.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                AccountNumberCorrectionPanel.this.correctAccountNumber(model.getObject(), target);
            }
        });
        add(accounts);
    }

    public static String displayAccountDetail(AccountDetail accountDetail) {
        StringBuilder displayValueBuilder = new StringBuilder().append(accountDetail.getAccountNumber());

        if (!Strings.isEmpty(accountDetail.getOwnerName())) {
            displayValueBuilder.append(", ").append(accountDetail.getOwnerName());
        }
        if (!Strings.isEmpty(accountDetail.getOwnerINN())) {
            displayValueBuilder.append(", ").append(getStringResource("INN")).append(" : ").append(accountDetail.getOwnerINN());
        }
        return displayValueBuilder.toString();
    }

    private static String getStringResource(String resourceKey) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, resourceKey, Session.get().getLocale());
    }

    protected abstract void correctAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target);
}
