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
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
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
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Панель для корректировки адреса вручную, когда нет соответствующей коррекции и поиск по локальной адресной базе не дал результатов.
 * @author Artem
 */
public class AddressCorrectionPanel extends Panel {

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "AddressService")
    private AddressService addressService;

    private String searchAddressEntity;

    private Dialog dialog;

    private SearchComponent searchComponent;
    private SearchComponentState componentState;

    private FeedbackPanel messages;

    private WebMarkupContainer container;

    private Payment payment;

    private static class FakeSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
        }
    }

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
        container.setOutputMarkupId(true);
        dialog.add(container);

        //Панель обратной связи
        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        container.add(messages);

        container.add(new Label("name", new LoadableDetachableModel<String>() {

            @Override
            protected String load() {
                if (payment == null) {
                    return "";
                }

                return payment.getField(PaymentDBF.SUR_NAM) + " "
                        + payment.getField(PaymentDBF.F_NAM) + " "
                        + payment.getField(PaymentDBF.M_NAM);
            }
        }));

        container.add(new Label("address", new LoadableDetachableModel<String>() {

            @Override
            protected String load() {
                if (payment == null) {
                    return "";
                }

                return payment.getField(PaymentDBF.N_NAME) + ", "
                        + payment.getField(PaymentDBF.VUL_NAME) + ", "
                        + BuildingFormatter.formatBuilding((String) payment.getField(PaymentDBF.BLD_NUM),
                        (String) payment.getField(PaymentDBF.CORP_NUM), getLocale()) + ", "
                        + payment.getField(PaymentDBF.FLAT);
            }
        }));

        searchAddressEntity = "apartment";

        componentState = new SearchComponentState();

        searchComponent = new SearchComponent("searchComponent", componentState, initFilters(), new FakeSearchCallback());
        searchComponent.setOutputMarkupPlaceholderTag(true);

        container.add(searchComponent);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate(componentState)) {
                    closeDialog(target);

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

    private void init(Payment payment){
        this.payment = payment;

        Long cityId = payment.getInternalCityId();
        Long streetId = payment.getInternalStreetId();
        Long buildingId = payment.getInternalBuildingId();
        Long apartmentId = payment.getInternalApartmentId();

        searchAddressEntity = initSearchAddressEntity(cityId, streetId, buildingId);

        componentState = initSearchComponentState(cityId, streetId, buildingId, apartmentId);

        searchComponent = new SearchComponent("searchComponent", componentState, initFilters(), new FakeSearchCallback());

        //todo update SearchComponent object
        container.remove("searchComponent");
        container.add(searchComponent);
    }

    private static Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private static Long getStreetTypeId(DomainObject streetObject) {
        return streetObject == null ? null : StreetStrategy.getStreetType(streetObject);
    }

    protected void correctAddress(Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId){
        addressService.correctLocalAddress(payment, cityId, streetId, streetTypeId, buildingId);
    }

    protected boolean validate(SearchComponentState componentState) {
        boolean validated = componentState.get(searchAddressEntity) != null && componentState.get(searchAddressEntity).getId() > 0;
        if (!validated) {
            error(getString("address_mistake"));
        }
        return validated;
    }

    private SearchComponentState initSearchComponentState(Long cityId, Long streetId, Long buildingId, Long apartmentId) {
        SearchComponentState componentState = new SearchComponentState();

        Map<String, Long> ids = Maps.newHashMap();
        if (cityId == null) {
            return componentState;
        } else {
            ids.put("city", cityId);
            componentState.put("city", findObject(cityId, "city", ids));
        }
        if (streetId == null) {
            return componentState;
        } else {
            ids.put("street", streetId);
            componentState.put("street", findObject(streetId, "street", ids));
        }
        if (buildingId == null) {
            return componentState;
        } else {
            ids.put("building", buildingId);
            componentState.put("building", findObject(buildingId, "building", ids));
        }
        if (apartmentId == null) {
            return componentState;
        } else {
            ids.put("apartment", apartmentId);
            componentState.put("apartment", findObject(apartmentId, "apartment", ids));
        }

        return componentState;
    }

    private DomainObject findObject(Long objectId, String entity, Map<String, Long> ids) {
        DomainObject object = null;
        DomainObjectExample example = new DomainObjectExample();
        example.setId(objectId);
        strategyFactory.getStrategy(entity).configureExample(example, ids, null);
        List<DomainObject> objects = strategyFactory.getStrategy(entity).find(example);
        if (objects != null && !objects.isEmpty()) {
            object = objects.get(0);
        }
        return object;
    }

    protected List<SearchComponent.SearchFilterSettings> initFilters() {
        if (searchAddressEntity.equalsIgnoreCase("city")) {
            return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", true));
        }
        if (searchAddressEntity.equalsIgnoreCase("street")) {
            return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", true),
                    new SearchComponent.SearchFilterSettings("street", true));
        }
        if (searchAddressEntity.equalsIgnoreCase("building")) {
            return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", true),
                    new SearchComponent.SearchFilterSettings("street", true),
                    new SearchComponent.SearchFilterSettings("building", true));
        }
        return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", true),
                new SearchComponent.SearchFilterSettings("street", true),
                new SearchComponent.SearchFilterSettings("building", true),
                new SearchComponent.SearchFilterSettings("apartment", true));
    }

    protected String initSearchAddressEntity(Long cityId, Long streetId, Long buildingId) {
        if (cityId == null) {
            return "city";
        }
        if (streetId == null) {
            return "street";
        }
        if (buildingId == null) {
            return "building";
        }
        return "apartment";
    }

    private void closeDialog(AjaxRequestTarget target) {
        dialog.close(target);
    }

    public void open(AjaxRequestTarget target, Payment payment) {
        init(payment);

        target.addComponent(container);

        dialog.open(target);
    }
}
