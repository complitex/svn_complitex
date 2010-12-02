/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.web.pages.util.BuildingFormatter;
import org.complitex.osznconnection.information.strategy.street.StreetStrategy;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Панель для корректировки адреса вручную, когда нет соответствующей коррекции и поиск по локальной адресной базе не дал результатов.
 * @author Artem
 */
public class AddressCorrectionPanel extends Panel {

    private enum CORRECTED_ENTITY {

        CITY, STREET, BUILDING;
    }
    
    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "AddressService")
    private AddressService addressService;
    
    private CORRECTED_ENTITY correctedEntity;
    private Dialog dialog;
    private SearchComponent searchComponent;
    private SearchComponentState componentState;
    private FeedbackPanel messages;
    private WebMarkupContainer container;
    private Payment payment;

    public AddressCorrectionPanel(String id, final MarkupContainer... toUpdate) {
        super(id);

        //Диалог
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(600);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        dialog.setOutputMarkupId(true);
        add(dialog);

        //Контейнер для ajax
        container = new WebMarkupContainer("container");
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(false);
        dialog.add(container);

        //Панель обратной связи
        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        container.add(messages);

        container.add(new Label("name", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return payment.getField(PaymentDBF.SUR_NAM) + " "
                        + payment.getField(PaymentDBF.F_NAM) + " "
                        + payment.getField(PaymentDBF.M_NAM);
            }
        }));

        container.add(new Label("address", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return payment.getField(PaymentDBF.N_NAME) + ", "
                        + payment.getField(PaymentDBF.VUL_NAME) + ", "
                        + BuildingFormatter.formatBuilding((String) payment.getField(PaymentDBF.BLD_NUM),
                        (String) payment.getField(PaymentDBF.CORP_NUM), getLocale()) + ", "
                        + payment.getField(PaymentDBF.FLAT);
            }
        }));

        componentState = new SearchComponentState();
        // at start create fake search component
        searchComponent = new SearchComponent("searchComponent", componentState, ImmutableList.of(""), null, true);
        container.add(searchComponent);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate(componentState)) {
                    correctAddress(getObjectId(componentState.get("city")),
                            getObjectId(componentState.get("street")),
                            getStreetTypeId(componentState.get("street")),
                            getObjectId(componentState.get("building")),
                            getObjectId(componentState.get("apartment")));

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
        };
        container.add(save);

        AjaxLink cancel = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        };
        container.add(cancel);
    }

    private static Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private static Long getStreetTypeId(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    protected void correctAddress(Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId) {
        addressService.correctLocalAddress(payment, cityId, streetId, streetTypeId, buildingId);
    }

    protected boolean validate(SearchComponentState componentState) {
        boolean validated = true;
        switch (correctedEntity) {
            case BUILDING:
                DomainObject building = componentState.get("building");
                validated &= building != null && building.getId() != null && building.getId() > 0;
            case STREET:
                DomainObject street = componentState.get("street");
                validated &= street != null && street.getId() != null && street.getId() > 0;
            case CITY:
                DomainObject city = componentState.get("city");
                validated = city != null && city.getId() != null && city.getId() > 0;
                break;
        }
        if (!validated) {
            error(getString("address_mistake"));
        }
        return validated;
    }

    private void initSearchComponentState(SearchComponentState componentState) {
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
        if (objects != null && !objects.isEmpty()) {
            return objects.get(0);
        }
        return null;
    }

    protected List<String> initFilters() {
        switch (correctedEntity) {
            case CITY:
                return ImmutableList.of("city");
            case STREET:
                return ImmutableList.of("city", "street");
            default:
                return ImmutableList.of("city", "street", "building");
        }
    }

    protected void initCorrectedEntity() {
        if (payment.getInternalCityId() == null) {
            correctedEntity = CORRECTED_ENTITY.CITY;
            return;
        }
        if (payment.getInternalStreetId() == null) {
            correctedEntity = CORRECTED_ENTITY.STREET;
            return;
        }
        correctedEntity = CORRECTED_ENTITY.BUILDING;
    }

    private void closeDialog(AjaxRequestTarget target) {
        container.setVisible(false);
        target.addComponent(container);
        dialog.close(target);
    }

    public void open(AjaxRequestTarget target, Payment payment) {
        this.payment = payment;

        initCorrectedEntity();
        initSearchComponentState(componentState);
        SearchComponent newSearchComponent = new SearchComponent("searchComponent", componentState, initFilters(), null, true);
        searchComponent.replaceWith(newSearchComponent);
        searchComponent = newSearchComponent;
        container.setVisible(true);
        target.addComponent(container);
        dialog.open(target);
    }
}
