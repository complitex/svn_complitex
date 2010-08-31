/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.strategy.web;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.Module;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.util.CloneUtil;
import org.complitex.dictionaryfw.web.component.ChildrenContainer;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
public class DomainObjectEditPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(DomainObjectEditPanel.class);

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;

    @EJB(name = "LogBean")
    private LogBean logBean;

    private String entity;

    private DomainObject oldObject;

    private DomainObject newObject;

    private Long parentId;

    private String parentEntity;

    private DomainObjectInputPanel objectInputPanel;

    public DomainObjectEditPanel(String id, String entity, Long objectId, Long parentId, String parentEntity) {
        super(id);
        this.entity = entity;
        this.parentId = parentId;
        this.parentEntity = parentEntity;

        if (objectId == null) {
            //create new entity
            oldObject = null;
            newObject = getStrategy().newInstance();

        } else {
            //edit existing entity
            newObject = getStrategy().findById(objectId);
            oldObject = CloneUtil.cloneObject(newObject);
        }
        init();
    }

    private Strategy getStrategy() {
        return strategyFactory.getStrategy(entity);
    }

    public DomainObject getObject() {
        return newObject;
    }

    public boolean isNew() {
        return oldObject == null;
    }

    private void init() {
        IModel<String> labelModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(getStrategy().getEntity().getEntityNames(), getLocale());
            }
        };
        Label title = new Label("title", labelModel);
        add(title);
        Label label = new Label("label", labelModel);
        add(label);

        final FeedbackPanel messages = new FeedbackPanel("messages");
//        messages.setOutputMarkupId(true);
        add(messages);

        Form form = new Form("form");

        objectInputPanel = new DomainObjectInputPanel("domainObjectInputPanel", newObject, entity, parentId, parentEntity);
        form.add(objectInputPanel);

        //children
        Component childrenContainer = new EmptyPanel("childrenContainer");
        if (oldObject != null) {
            childrenContainer = new ChildrenContainer("childrenContainer", entity, newObject);
        }
        form.add(childrenContainer);

        //history
        WebMarkupContainer historyContainer = new WebMarkupContainer("historyContainer");
        Link history = new Link("history") {

            @Override
            public void onClick() {
                setResponsePage(getStrategy().getHistoryPage(), getStrategy().getHistoryPageParams(newObject.getId()));
            }
        };
        historyContainer.add(history);
        historyContainer.setVisible(getStrategy().hasHistory(newObject.getId()));
        form.add(historyContainer);

        //save-cancel functional
        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                save();
            }
        };
        submit.setVisible(CanEditUtil.canEdit(newObject));
        form.add(submit);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back();
            }
        };
        cancel.setVisible(CanEditUtil.canEdit(newObject));
        form.add(cancel);
        Link back = new Link("back") {

            @Override
            public void onClick() {
                back();
            }
        };
        back.setVisible(!CanEditUtil.canEdit(newObject));
        form.add(back);
        add(form);
    }

    protected boolean validate() {
        boolean valid = objectInputPanel.validateParent();
        IValidator validator = getStrategy().getValidator();
        if (validator != null) {
            valid = validator.validate(newObject, this);
        }
        return valid;
    }

    protected void save() {
        if (validate()) {
            if (isNew()) {
                getStrategy().insert(newObject);
            } else {
                getStrategy().update(oldObject, newObject);
            }

            logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                    isNew() ? Log.EVENT.CREATE :  Log.EVENT.EDIT, getStrategy(),
                    oldObject, newObject, getLocale(), null);

            //todo: add catch database exception

            back();
        }
    }

    private void back() {
        if (!fromParent()) {
            //return to list page for current entity.
            setResponsePage(getStrategy().getListPage(), getStrategy().getListPageParams());
        } else {
            //return to edit page for parent entity.
            setResponsePage(strategyFactory.getStrategy(parentEntity).getEditPage(),
                    strategyFactory.getStrategy(parentEntity).getEditPageParams(parentId, null, null));
        }
    }

    public void disable() {
        getStrategy().disable(newObject);
        back();
    }

    public void enable() {
        getStrategy().enable(newObject);
        back();
    }

    private boolean fromParent() {
        return parentId != null && !Strings.isEmpty(parentEntity);
    }

    public SearchComponentState getParentSearchComponentState() {
        return objectInputPanel.getParentSearchComponentState();
    }
}
