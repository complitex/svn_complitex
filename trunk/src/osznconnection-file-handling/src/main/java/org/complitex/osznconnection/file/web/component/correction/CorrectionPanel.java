/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public abstract class CorrectionPanel extends Panel {

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    public CorrectionPanel(String id, String name, String address, Long cityId, Long streetId, Long buildingId, Long apartmentId) {
        super(id);
        init(name, address, cityId, streetId, buildingId, apartmentId);
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

    private static class FakeSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
        }
    }

    private void init(String name, String address, Long cityId, Long streetId, Long buildingId, Long apartmentId) {
        add(new Label("name", name));
        add(new Label("address", address));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        final SearchComponentState componentState = initSearchComponentState(cityId, streetId, buildingId, apartmentId);

        SearchComponent searchComponent = new SearchComponent("searchComponent", componentState,
                ImmutableList.of("city", "street", "building", "apartment"), new FakeSearchCallback(), true);
        add(searchComponent);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate(componentState)) {
                    correctAddress(componentState.get("city").getId(), componentState.get("street").getId(), componentState.get("building").getId(),
                            componentState.get("apartment").getId());
                    back();
                } else {
                    target.addComponent(messages);
                }
            }
        };
        add(save);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back();
            }
        };
        add(cancel);
    }

    protected abstract void correctAddress(long cityId, long streetId, long buildingId, long apartmentId);

    protected boolean validate(SearchComponentState componentState) {
        boolean validated = componentState.get("city") != null && componentState.get("city").getId() > 0
                && componentState.get("street") != null && componentState.get("street").getId() > 0
                && componentState.get("building") != null && componentState.get("building").getId() > 0
                && componentState.get("apartment") != null && componentState.get("apartment").getId() > 0;
        if (!validated) {
            error(getString("address_mistake"));
        }
        return validated;
    }

    public abstract void back();
}
