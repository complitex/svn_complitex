/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.Module;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.ChildrenContainer;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionsPanel;
import org.complitex.dictionary.web.component.permission.PermissionPropagationDialogPanel;

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
    private final String scrollListPageParameterName;

    public DomainObjectEditPanel(String id, String entity, Long objectId, Long parentId, String parentEntity, String scrollListPageParameterName) {
        super(id);
        this.entity = entity;
        this.parentId = parentId;
        this.parentEntity = parentEntity;
        this.scrollListPageParameterName = scrollListPageParameterName;

        if (objectId == null) {
            //create new entity
            oldObject = null;
            newObject = getStrategy().newInstance();

        } else {
            //edit existing entity
            newObject = getStrategy().findById(objectId, false);
            oldObject = CloneUtil.cloneObject(newObject);
        }
        init();
    }

    private IStrategy getStrategy() {
        return strategyFactory.getStrategy(entity);
    }

    public DomainObject getObject() {
        return newObject;
    }

    private boolean isNew() {
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
        messages.setOutputMarkupId(true);
        add(messages);

        Form form = new Form("form");

        //input panel
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
        historyContainer.setVisible(!isNew());
        form.add(historyContainer);

        //permissions panel
        DomainObjectPermissionsPanel permissionsPanel = new DomainObjectPermissionsPanel("permissionsPanel", newObject.getSubjectIds());
        permissionsPanel.setEnabled(DomainObjectAccessUtil.canEdit(entity, newObject));
        form.add(permissionsPanel);

        //permissionPropagationDialogPanel
        final PermissionPropagationDialogPanel permissionPropagationDialogPanel = new PermissionPropagationDialogPanel("permissionPropagationDialogPanel") {

            @Override
            protected void applyPropagation(boolean propagate) {
                try {
                    save(propagate);
                } catch (Exception e) {
                    log.error("", e);
                    error(getString("db_error"));
                }

            }
        };
        add(permissionPropagationDialogPanel);

        //save-cancel functional
        AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    if (validate()) {
                        if (isNew()) {
                            save(false);
                        } else {
                            boolean canPopagatePermissions = getStrategy().canPropagatePermissions(newObject);
                            if (canPopagatePermissions && getStrategy().isNeedToChangePermission(oldObject.getSubjectIds(), newObject.getSubjectIds())) {
                                permissionPropagationDialogPanel.open(target);
                            } else {
                                save(false);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                    error(getString("db_error"));
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(messages);
            }
        };
        submit.setVisible(DomainObjectAccessUtil.canEdit(entity, newObject));
        form.add(submit);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back();
            }
        };
        cancel.setVisible(DomainObjectAccessUtil.canEdit(entity, newObject));
        form.add(cancel);
        Link back = new Link("back") {

            @Override
            public void onClick() {
                back();
            }
        };
        back.setVisible(!DomainObjectAccessUtil.canEdit(entity, newObject));
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

    protected void save(boolean propagate) {
        //permission related logic
        if (isNew()) {
            getStrategy().insert(newObject);
        } else {
            if (!propagate) {
                getStrategy().update(oldObject, newObject, DateUtil.getCurrentDate());
            } else {
                getStrategy().updateAndPropagate(oldObject, newObject, DateUtil.getCurrentDate());
            }
        }

        logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                isNew() ? Log.EVENT.CREATE : Log.EVENT.EDIT, getStrategy(),
                oldObject, newObject, getLocale(), null);
        back();
    }

    private void back() {
        if (!fromParent()) {
            //return to list page for current entity.
            PageParameters listPageParams = getStrategy().getListPageParams();
            listPageParams.put(scrollListPageParameterName, newObject.getId());
            setResponsePage(getStrategy().getListPage(), listPageParams);
        } else {
            //return to edit page for parent entity.
            setResponsePage(strategyFactory.getStrategy(parentEntity).getEditPage(),
                    strategyFactory.getStrategy(parentEntity).getEditPageParams(parentId, null, null));
        }
    }

    public void disable() {
        try {
            getStrategy().disable(newObject);
            back();
        } catch (Exception e) {
            log.error("", e);
            error(getString("db_error"));
        }
    }

    public void enable() {
        try {
            getStrategy().enable(newObject);
            back();
        } catch (Exception e) {
            log.error("", e);
            error(getString("db_error"));
        }
    }

    private boolean fromParent() {
        return parentId != null && !Strings.isEmpty(parentEntity);
    }

    public SearchComponentState getParentSearchComponentState() {
        return objectInputPanel.getParentSearchComponentState();
    }
}
