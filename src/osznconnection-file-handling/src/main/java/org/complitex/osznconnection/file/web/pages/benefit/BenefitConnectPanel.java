package org.complitex.osznconnection.file.web.pages.benefit;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.osznconnection.file.calculation.entity.BenefitData;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.service.BenefitFillService;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.osznconnection.file.calculation.adapter.AccountNotFoundException;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.10.2010 14:46:58
 */
public class BenefitConnectPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(BenefitConnectPanel.class);

    @EJB(name = "BenefitFillService")
    private BenefitFillService benefitFillService;
    
    private Dialog dialog;
    private Benefit benefit;
    private WebMarkupContainer container;
    private IModel<List<BenefitData>> dataModel;

    public BenefitConnectPanel(String id, final MarkupContainer... toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(735);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        add(dialog);

        container = new WebMarkupContainer("container");
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(false);
        dialog.add(container);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        container.add(messages);

        Form form = new Form("form");
        container.add(form);

        container.add(new Label("name", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return benefit.getDisplayName();
            }
        }));

        container.add(new Label("address", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return benefit.getDisplayAddress();
            }
        }));

        container.add(new Label("accountNumber", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return benefit.getAccountNumber();
            }
        }));

        container.add(new Label("inn", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return StringUtil.valueOf((String) benefit.getField(BenefitDBF.IND_COD));
            }
        }));

        container.add(new Label("passport", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return StringUtil.valueOf((String) benefit.getField(BenefitDBF.PSP_SER))
                        + StringUtil.valueOf((String) benefit.getField(BenefitDBF.PSP_NUM));
            }
        }));

        dataModel = new LoadableDetachableModel<List<BenefitData>>() {

            @Override
            protected List<BenefitData> load() {
                try {
                    return Lists.newArrayList(benefitFillService.getBenefitData(benefit));
                } catch (AccountNotFoundException e) {
                    error(getString("account_not_found"));
                } catch (Exception e) {
                    log.error("", e);
                    error(getString("common_db_error"));
                }
                return null;
            }
        };

        WebMarkupContainer table = new WebMarkupContainer("table") {

            @Override
            public boolean isVisible() {
                return dataModel.getObject() != null && !dataModel.getObject().isEmpty();
            }
        };
        table.setOutputMarkupPlaceholderTag(true);
        form.add(table);

        final IModel<BenefitData> benefitDataModel = new Model<BenefitData>();
        final RadioGroup<BenefitData> radioGroup = new RadioGroup<BenefitData>("radioGroup", benefitDataModel);
        radioGroup.setRequired(true);
        table.add(radioGroup);

        ListView<BenefitData> data = new ListView<BenefitData>("data", dataModel) {

            @Override
            protected void populateItem(ListItem<BenefitData> item) {
                BenefitData benefitData = item.getModelObject();

                item.add(new Radio<BenefitData>("radio", item.getModel(), radioGroup));
                item.add(new Label("firstName", StringUtil.valueOf(benefitData.getFirstName())));
                item.add(new Label("lastName", StringUtil.valueOf(benefitData.getLastName())));
                item.add(new Label("middleName", StringUtil.valueOf(benefitData.getMiddleName())));
                item.add(new Label("inn", benefitData.getInn()));
                item.add(new Label("passport", StringUtil.valueOf(benefitData.getPassportSerial()) + " "
                        + StringUtil.valueOf(benefitData.getPassportNumber())));
                item.add(new Label("orderFamily", StringUtil.valueOf(benefitData.getOrderFamily())));
                item.add(new Label("code", StringUtil.valueOf(benefitData.getCode())));
                item.add(new Label("userCount", StringUtil.valueOf(benefitData.getUserCount())));
            }
        };
        radioGroup.add(data);

        AjaxButton connect = new AjaxButton("connect") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                BenefitData selectedBenefitData = benefitDataModel.getObject();
                if (validateBenefitData(selectedBenefitData)) {
                    benefitFillService.connectBenefit(benefit, selectedBenefitData);

                    if (toUpdate != null) {
                        for (MarkupContainer container : toUpdate) {
                            target.addComponent(container);
                        }
                    }
                    closeDialog(target);
                } else {
                    target.addComponent(messages);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(messages);
            }

            @Override
            public boolean isVisible() {
                return dataModel.getObject() != null && !dataModel.getObject().isEmpty();
            }
        };
        connect.setOutputMarkupPlaceholderTag(true);
        form.add(connect);

        AjaxLink cancel = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        };
        form.add(cancel);
    }

    private boolean validateBenefitData(BenefitData benefitData) {
        boolean valid = true;
        String osznBenefitCode = benefitData.getOsznBenefitCode();
        if (Strings.isEmpty(osznBenefitCode)) {
            error(getString("benefit_code_not_found"));
            valid = false;
        } else {
            try {
                Integer.valueOf(osznBenefitCode);
            } catch (NumberFormatException e) {
                error(StatusRenderer.displayBenefitCodeError(osznBenefitCode));
                valid = false;
            }

            try {
                Integer.valueOf(benefitData.getOrderFamily());
            } catch (NumberFormatException e) {
                error(StatusRenderer.displayBenefitOrdFamError(benefitData.getOrderFamily()));
                valid = false;
            }
        }
        return valid;
    }

    private void closeDialog(AjaxRequestTarget target) {
        container.setVisible(false);
        benefit = null;
        dataModel.detach();
        target.addComponent(container);
        dialog.close(target);
    }

    public void open(AjaxRequestTarget target, Benefit benefit) {
        this.benefit = benefit;

        container.setVisible(true);
        target.addComponent(container);
        dialog.open(target);
    }
}
