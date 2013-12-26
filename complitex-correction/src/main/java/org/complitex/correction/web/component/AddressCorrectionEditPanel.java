package org.complitex.correction.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.service.AddressCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Стандартная панель редактирования коррекции элемента адреса.
 */
public abstract class AddressCorrectionEditPanel<T extends Correction> extends AbstractCorrectionEditPanel<T> {
    @EJB
    private StrategyFactory strategyFactory;

    private class Callback implements ISearchCallback, Serializable {

        private Correction correction;
        private String entity;

        private Callback(Correction correction, String entity) {
            this.correction = correction;
            this.entity = entity;
        }

        @Override
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(entity);
            if (id != null && id > 0) {
                correction.setObjectId(id);
            } else {
                correction.setObjectId(null);
            }
        }
    }

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public AddressCorrectionEditPanel(String id, Long correctionId) {
        super(id, correctionId);
    }

    @Override
    protected IModel<String> internalObjectLabel(Locale locale) {
        return new ResourceModel("address");
    }

    @Override
    protected Panel internalObjectPanel(String id) {
        Correction correction = getCorrection();
        String entity = correction.getEntity();
        SearchComponentState componentState = new SearchComponentState();
        if (!isNew()) {
            long objectId = correction.getObjectId();
            IStrategy.SimpleObjectInfo info = getStrategy(entity).findParentInSearchComponent(objectId, null);
            if (info != null) {
                componentState = getStrategy(entity).getSearchComponentStateForParent(info.getId(), info.getEntityTable(), null);
                componentState.put(entity, findObject(objectId, entity));
            }
        }

        return new WiQuerySearchComponent(id, componentState, getSearchFilters(), new Callback(correction, entity),
                ShowMode.ACTIVE, true);
    }

    @Override
    protected String getNullObjectErrorMessage() {
        return getString("address_required");
    }

    protected IStrategy getStrategy(String entity) {
        return strategyFactory.getStrategy(entity);
    }

    protected DomainObject findObject(long objectId, String entity) {
        return getStrategy(entity).findById(objectId, true);
    }

    protected abstract List<String> getSearchFilters();

    @Override
    protected PageParameters getBackPageParameters() {
        return new PageParameters();
    }

    @Override
    protected abstract boolean validateExistence();
}
