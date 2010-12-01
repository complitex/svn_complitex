/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.PaymentLookupBean;
import org.complitex.osznconnection.file.web.component.StatusRenderer;
import org.complitex.osznconnection.file.web.component.correction.account.AccountNumberCorrectionPanel;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.accordion.Accordion;
import org.odlabs.wiquery.ui.accordion.AccordionAnimated;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;
import org.complitex.dictionaryfw.util.CloneUtil;
import org.complitex.osznconnection.information.strategy.street.StreetStrategy;

/**
 * Панель для поиска номера л/c по различным параметрам: по адресу, по номеру лиц. счета, по номеру в мегабанке.
 * @author Artem
 */
public abstract class PaymentLookupPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(PaymentLookupPanel.class);

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "PaymentLookupBean")
    private PaymentLookupBean paymentLookupBean;

    private IModel<String> accountInfoModel;
    private IModel<String> apartmentModel;
    private IModel<String> ownNumSrModel;
    private IModel<String> megabankModel;
    private IModel<List<? extends AccountDetail>> accountsModel;
    private IModel<AccountDetail> accountModel;
    private AccountNumberCorrectionPanel accountNumberCorrectionPanel;
    private WebMarkupContainer container;
    private FeedbackPanel messages;
    private Dialog dialog;
    private Accordion accordion;
    private Label accountInfo;
    private SearchComponentState componentState;
    private SearchComponent searchComponent;
    private Payment payment;
    private Payment initialPayment;

    public PaymentLookupPanel(String id) {
        super(id);
        init();
    }

    private void init() {
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(600);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        add(dialog);

        //Контейнер для ajax
        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        dialog.add(container);

        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        container.add(messages);

        accordion = new Accordion("accordion");
        accordion.setAnimationEffect(new AccordionAnimated(false));
        accordion.setOutputMarkupPlaceholderTag(true);
        container.add(accordion);

        //lookup by address
        componentState = new SearchComponentState();

        accountInfoModel = new Model<String>();
        accountInfo = new Label("accountInfo", accountInfoModel);
        accountInfo.setOutputMarkupId(true);
        container.add(accountInfo);

        apartmentModel = new Model<String>();
        TextField<String> apartment = new TextField<String>("apartment", apartmentModel);
        apartment.setOutputMarkupId(true);
        apartment.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        accordion.add(apartment);

        AjaxLink lookupByAddress = new AjaxLink("lookupByAddress") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                initInternalAddress(componentState);
                initApartment(apartmentModel.getObject());

                if (validateInternalAddress()) {
                    paymentLookupBean.resolveOutgoingAddress(payment);

                    if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_NOT_FOUND) {
                        List<AccountDetail> accountList = paymentLookupBean.getAccounts(payment);

                        if (payment.getStatus() == RequestStatus.ACCOUNT_NUMBER_NOT_FOUND) {
                            accountInfoModel.setObject(null);
                            accountModel.setObject(null);

                            error(StatusRenderer.displayValue(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND));

                            target.addComponent(messages);
                            target.addComponent(accountInfo);
                        } else {
                            if (accountList.size() == 1) {
                                accountModel.setObject(accountList.get(0));
                                accountInfoModel.setObject(AccountNumberCorrectionPanel.displayAccountDetail(accountList.get(0)));

                                target.addComponent(accountInfo);

                                if (accountNumberCorrectionPanel.isVisible()) {
                                    accountNumberCorrectionPanel.setVisible(false);

                                    target.addComponent(accordion);
                                }
                            } else {
                                accountModel.setObject(null);
                                accountInfoModel.setObject(null);
                                accountsModel.setObject(accountList);
                                accountNumberCorrectionPanel.setVisible(true);

                                target.addComponent(accountInfo);
                                target.addComponent(accordion);
                            }
                        }
                    } else {
                        error(StatusRenderer.displayValue(payment.getStatus()));
                    }
                }
                target.addComponent(messages);
            }
        };
        accordion.add(lookupByAddress);
        searchComponent = new SearchComponent("searchComponent", componentState,
                ImmutableList.of("city", "street", "building"), null, true);
        searchComponent.setOutputMarkupPlaceholderTag(true);
        searchComponent.setVisible(false);
        accordion.add(searchComponent);

        accountModel = new Model<AccountDetail>();
        accountsModel = new WildcardListModel<AccountDetail>();
        accountNumberCorrectionPanel = new AccountNumberCorrectionPanel("accountNumberCorrectionPanel", accountsModel) {

            @Override
            protected void correctAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                accountModel.setObject(accountDetail);
                accountInfoModel.setObject(AccountNumberCorrectionPanel.displayAccountDetail(accountDetail));
                target.addComponent(accountInfo);
            }
        };
        accountNumberCorrectionPanel.setOutputMarkupPlaceholderTag(true);
        accountNumberCorrectionPanel.setVisible(false);
        accordion.add(accountNumberCorrectionPanel);

        //lookup by OWN_NUM_SR
        ownNumSrModel = new Model<String>();
        TextField<String> ownNumSr = new TextField<String>("ownNumSr", ownNumSrModel);
        ownNumSr.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        ownNumSr.setOutputMarkupId(true);

        accordion.add(ownNumSr);

        AjaxLink lookupByOwnNumSr = new AjaxLink("lookupByOwnNumSr") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validateOwnNumSr()) {
                }
                target.addComponent(messages);
            }
        };
        accordion.add(lookupByOwnNumSr);

        //lookup by megabank
        megabankModel = new Model<String>();
        TextField<String> megabank = new TextField<String>("megabank", megabankModel);
        megabank.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        megabank.setOutputMarkupId(true);
        accordion.add(megabank);
        AjaxLink lookupByMegabank = new AjaxLink("lookupByMegabank") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validateMegabank()) {
                }
                target.addComponent(messages);
            }
        };
        accordion.add(lookupByMegabank);

        // save/cancel
        container.add(new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate()) {
                    updateAccountNumber(initialPayment, accountModel.getObject().getAccountNumber(), target);
                    closeDialog(target);
                } else {
                    target.addComponent(messages);
                }
            }
        });
        container.add(new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        });

    }

    private void closeDialog(AjaxRequestTarget target) {
        searchComponent.setVisible(false);
        target.addComponent(searchComponent);
        dialog.close(target);
    }

    private boolean validateOwnNumSr() {
        boolean validated = !Strings.isEmpty(ownNumSrModel.getObject());
        if (!validated) {
            error(getString("own_num_sr_required"));
        }
        return validated;
    }

    private boolean validateMegabank() {
        boolean validated = !Strings.isEmpty(megabankModel.getObject());
        if (!validated) {
            error(getString("megabank_required"));
        }
        return validated;
    }

    private boolean validate() {
        boolean validated = accountModel.getObject() != null;
        if (!validated) {
            error(getString("account_number_not_chosen"));
        }
        return validated;
    }

    private static Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private void initApartment(String apartment) {
        payment.setField(PaymentDBF.FLAT, apartment != null ? apartment : "");
    }

    private static Long getStreetType(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    private void initInternalAddress(SearchComponentState componentState) {
        payment.setInternalCityId(getObjectId(componentState.get("city")));
        payment.setInternalStreetId(getObjectId(componentState.get("street")));
        payment.setInternalStreetTypeId(getStreetType(componentState.get("street")));
        payment.setInternalBuildingId(getObjectId(componentState.get("building")));
    }

    private void initSearchComponentState(SearchComponentState componentState, Payment payment) {
        componentState.clear();
        Map<String, Long> ids = Maps.newHashMap();

        if (payment.getInternalCityId() != null) {
            ids.put("city", payment.getInternalCityId());
            componentState.put("city", findObject(payment.getInternalCityId(), "city", ids));
        }

        if (payment.getInternalStreetId() != null) {
            ids.put("street", payment.getInternalStreetId());
            componentState.put("street", findObject(payment.getInternalStreetId(), "street", ids));
        }

        if (payment.getInternalBuildingId() != null) {
            ids.put("building", payment.getInternalBuildingId());
            componentState.put("building", findObject(payment.getInternalBuildingId(), "building", ids));
        }
    }

    private DomainObject findObject(Long objectId, String entity, Map<String, Long> ids) {
        DomainObjectExample example = new DomainObjectExample(objectId);
        strategyFactory.getStrategy(entity).configureExample(example, ids, null);
        List<? extends DomainObject> objects = strategyFactory.getStrategy(entity).find(example);
        if (objects.size() == 1) {
            return objects.get(0);
        }
        return null;
    }

    private boolean validateInternalAddress() {
        boolean validated = payment.getInternalCityId() != null && payment.getInternalCityId() > 0
                && payment.getInternalStreetId() != null && payment.getInternalStreetId() > 0
                && payment.getInternalBuildingId() != null && payment.getInternalBuildingId() > 0;
        if (!validated) {
            error(getString("address_required"));
        }
        return validated;
    }

    public void open(AjaxRequestTarget target, Payment payment) {
        this.payment = CloneUtil.cloneObject(payment);
        this.initialPayment = payment;

        accountModel.setObject(null);
        accountsModel.setObject(null);
        accountInfoModel.setObject(null);
        target.addComponent(accountInfo);

        //lookup by address
        apartmentModel.setObject((String) payment.getField(PaymentDBF.FLAT));
        initSearchComponentState(componentState, payment);
        searchComponent.reinitialize(target);
        searchComponent.setVisible(true);

        if (accountNumberCorrectionPanel.isVisible()) {
            accountNumberCorrectionPanel.setVisible(false);
        }

        //lookup by OWN_NUM_SR
        ownNumSrModel.setObject(null);

        //lookup by megabank
        megabankModel.setObject(null);

        target.addComponent(accordion);
        target.addComponent(messages);
        dialog.open(target);
    }

    protected abstract void updateAccountNumber(Payment payment, String accountNumber, AjaxRequestTarget target);
}
