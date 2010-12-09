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
import org.apache.wicket.model.Model;
import org.complitex.osznconnection.file.calculation.entity.BenefitData;
import org.complitex.osznconnection.file.entity.Benefit;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.osznconnection.file.calculation.adapter.AccountNotFoundException;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.RequestFile.TYPE;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.RequestWarning;
import org.complitex.osznconnection.file.entity.RequestWarningParameter;
import org.complitex.osznconnection.file.entity.RequestWarningStatus;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
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

    @EJB(name = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;
    
    @EJB(name = "WebWarningRenderer")
    private WebWarningRenderer webWarningRenderer;

    private class BenefitDataModel extends AbstractReadOnlyModel<List<BenefitData>> {

        private List<BenefitData> benefitData;

        protected List<BenefitData> load() {
            try {
                return Lists.newArrayList(benefitBean.getBenefitData(benefit));
            } catch (AccountNotFoundException e) {
                error(statusRenderService.displayStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND, getLocale()));
            } catch (Exception e) {
                log.error("", e);
                error(getString("common_db_error"));
            }
            return null;
        }

        @Override
        public List<BenefitData> getObject() {
            if (benefitData == null) {
                benefitData = load();
            }
            return benefitData;
        }

        public void clear() {
            benefitData = null;
        }
    }
    private Dialog dialog;
    private Benefit benefit;
    private WebMarkupContainer container;
    private BenefitDataModel dataModel;

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

        dataModel = new BenefitDataModel();

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
                    benefitBean.connectBenefit(benefit, selectedBenefitData, dataModel.getObject().size() > 1);

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
        long osznId = benefit.getOrganizationId();
        long calculationCenterId = benefitData.getCalcCenterId();
        String osznBenefitCode = benefitData.getOsznPrivilegeCode();
        Long privilegeObjectId = benefitData.getPrivilegeObjectId();
        if (privilegeObjectId == null) {
            valid = false;
            RequestWarning warning = new RequestWarning(TYPE.BENEFIT, RequestWarningStatus.PRIVILEGE_OBJECT_NOT_FOUND);
            warning.addParameter(new RequestWarningParameter(0, benefitData.getCode()));
            warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
            error(webWarningRenderer.display(warning, getLocale()));
        } else if (osznBenefitCode == null) {
            RequestWarning warning = new RequestWarning(TYPE.BENEFIT, RequestWarningStatus.PRIVILEGE_CODE_NOT_FOUND);
            warning.addParameter(new RequestWarningParameter(0, "privilege", privilegeObjectId));
            warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
            error(webWarningRenderer.display(warning, getLocale()));
            valid = false;
        } else {
            try {
                Integer.valueOf(osznBenefitCode);
            } catch (NumberFormatException e) {
                RequestWarning warning = new RequestWarning(TYPE.BENEFIT, RequestWarningStatus.PRIVILEGE_CODE_INVALID);
                warning.addParameter(new RequestWarningParameter(0, osznBenefitCode));
                warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                warning.addParameter(new RequestWarningParameter(2, "privilege", privilegeObjectId));
                error(webWarningRenderer.display(warning, getLocale()));
                valid = false;
            }

            try {
                Integer.valueOf(benefitData.getOrderFamily());
            } catch (NumberFormatException e) {
                RequestWarning warning = new RequestWarning(TYPE.BENEFIT, RequestWarningStatus.ORD_FAM_INVALID);
                warning.addParameter(new RequestWarningParameter(0, benefitData.getOrderFamily()));
                warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                error(webWarningRenderer.display(warning, getLocale()));
                valid = false;
            }
        }

        return valid;
    }

    private void closeDialog(AjaxRequestTarget target) {
        container.setVisible(false);
        benefit = null;
        target.addComponent(container);
        dialog.close(target);
    }

    public void open(AjaxRequestTarget target, Benefit benefit) {
        this.benefit = benefit;

        container.setVisible(true);
        dataModel.clear();
        target.addComponent(container);
        dialog.open(target);
    }
}
