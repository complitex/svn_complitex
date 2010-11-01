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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.web.pages.util.BuildingFormatter;
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
public abstract class AddressCorrectionPanel extends Panel {

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private String searchAddressEntity;

    private IModel<Payment> paymentModel;

    private Dialog dialog;

    private SearchComponent searchComponent;

    private FeedbackPanel messages;

    private MarkupContainer[] toUpdate;

    private static class FakeSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
        }
    }

    public AddressCorrectionPanel(String id, Payment payment, MarkupContainer[] toUpdate) {
        super(id);
        paymentModel = new Model<Payment>(payment);
        this.toUpdate = toUpdate;
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

        String name = paymentModel.getObject().getField(PaymentDBF.SUR_NAM) + " "
                + paymentModel.getObject().getField(PaymentDBF.F_NAM) + " "
                + paymentModel.getObject().getField(PaymentDBF.M_NAM);
        String address = paymentModel.getObject().getField(PaymentDBF.N_NAME) + ", "
                + paymentModel.getObject().getField(PaymentDBF.VUL_NAME) + ", "
                + BuildingFormatter.formatBuilding((String) paymentModel.getObject().getField(PaymentDBF.BLD_NUM),
                (String) paymentModel.getObject().getField(PaymentDBF.CORP_NUM), getLocale())
                + ", " + paymentModel.getObject().getField(PaymentDBF.FLAT);
        Long cityId = paymentModel.getObject().getInternalCityId();
        Long streetId = paymentModel.getObject().getInternalStreetId();
        Long buildingId = paymentModel.getObject().getInternalBuildingId();
        Long apartmentId = paymentModel.getObject().getInternalApartmentId();

        dialog.add(new Label("name", name));
        dialog.add(new Label("address", address));

        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        dialog.add(messages);

        searchAddressEntity = initSearchAddressEntity(cityId, streetId, buildingId);

        final SearchComponentState componentState = initSearchComponentState(cityId, streetId, buildingId, apartmentId);
        List<SearchComponent.SearchFilterSettings> searchFilterSettings = initFilters();

        searchComponent = new SearchComponent("searchComponent", componentState, searchFilterSettings, new FakeSearchCallback());
        searchComponent.setOutputMarkupPlaceholderTag(true);
        searchComponent.setVisible(false);
        dialog.add(searchComponent);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate(componentState)) {
                    correctAddress(getObjectId(componentState.get("city")),
                            getObjectId(componentState.get("street")),
                            getObjectTypeId(componentState.get("street")),
                            getObjectId(componentState.get("building")),
                            getObjectId(componentState.get("apartment")));

                    closeDialog(target);
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
        dialog.add(save);
        AjaxLink cancel = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                closeDialog(target);
            }
        };
        dialog.add(cancel);
    }

    private static Long getObjectId(DomainObject object) {
        return object == null ? null : object.getId();
    }

    private static Long getObjectTypeId(DomainObject object) {
        return object == null ? null : object.getEntityTypeId();
    }

    protected abstract void correctAddress(Long cityId, Long streetId, Long streetTypeId, Long buildingId, Long apartmentId);

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
            return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", false),
                    new SearchComponent.SearchFilterSettings("street", true));
        }
        if (searchAddressEntity.equalsIgnoreCase("building")) {
            return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", false),
                    new SearchComponent.SearchFilterSettings("street", false),
                    new SearchComponent.SearchFilterSettings("building", true));
        }
        return ImmutableList.of(new SearchComponent.SearchFilterSettings("city", false),
                new SearchComponent.SearchFilterSettings("street", false),
                new SearchComponent.SearchFilterSettings("building", false),
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
        searchComponent.setVisible(false);
        target.addComponent(searchComponent);
        dialog.close(target);
    }

    public void open(AjaxRequestTarget target) {
        searchComponent.setVisible(true);
        target.addComponent(searchComponent);
        dialog.open(target);
    }
}
