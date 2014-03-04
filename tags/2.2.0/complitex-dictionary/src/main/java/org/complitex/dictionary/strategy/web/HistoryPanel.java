/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.History;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;

import javax.ejb.EJB;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.wicket.Component;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public class HistoryPanel extends Panel {

    protected static final String DATE_FORMAT = "HH:mm:ss dd.MM.yyyy";
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StringCultureBean stringBean;
    private String entity;
    private String strategyName;
    private long objectId;

    public HistoryPanel(String id, String entity, long objectId) {
        super(id);
        this.entity = entity;
        this.objectId = objectId;
        init();
    }

    /**
     * For not standard strategies
     * @param id
     * @param entity
     * @param strategyName
     * @param objectId
     */
    public HistoryPanel(String id, String strategyName, String entity, long objectId) {
        super(id);
        this.strategyName = strategyName;
        this.entity = entity;
        this.objectId = objectId;
        init();
    }

    protected String getEntity() {
        return entity;
    }

    protected String getStrategyName() {
        return strategyName;
    }

    protected long getObjectId() {
        return objectId;
    }

    protected IStrategy getStrategy() {
        return strategyFactory.getStrategy(getStrategyName(), getEntity());
    }

    protected void init() {
        IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return MessageFormat.format(getString("label"), stringBean.displayValue(getStrategy().getEntity().
                        getEntityNames(), getLocale()), getObjectId());
            }
        };
        Label title = new Label("title", labelModel);
        add(title);
        Label label = new Label("label", labelModel);
        add(label);

        final List<History> historyList = getStrategy().getHistory(getObjectId());

        ListView<History> history = new ListView<History>("history", historyList) {

            @Override
            protected void populateItem(final ListItem<History> item) {
                final History currentHistory = item.getModelObject();

                IModel<String> dateModel = new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());
                        String dateAsString = dateFormat.format(currentHistory.getDate());
                        String nextDateAsString;
                        if (item.getIndex() < historyList.size() - 1) {
                            History nextHistory = historyList.get(item.getIndex() + 1);
                            Date nextDate = nextHistory.getDate();
                            nextDateAsString = dateFormat.format(nextDate);
                        } else {
                            nextDateAsString = getString("current_time");
                        }
                        return MessageFormat.format(getString("date_label"), dateAsString, nextDateAsString);
                    }
                };
                item.add(new Label("date", dateModel));
                item.add(newInputPanel("domainObjectInputPanel", currentHistory.getObject(), currentHistory.getDate()));
            }
        };
        add(history);
    }

    protected Component newInputPanel(String id, DomainObject historyObject, Date historyDate) {
        return new DomainObjectInputPanel(id, historyObject, getEntity(), getStrategyName(), null, null, historyDate);
    }
}
