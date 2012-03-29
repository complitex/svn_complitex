/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import com.google.common.collect.Lists;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import static org.apache.wicket.markup.html.JavascriptPackageResource.*;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.Module;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import static org.complitex.dictionary.util.CloneUtil.*;
import static org.complitex.dictionary.util.DateUtil.*;
import org.complitex.dictionary.web.component.ChildrenContainer;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionsPanel;
import org.complitex.dictionary.web.component.permission.PermissionPropagationDialogPanel;
import static org.complitex.dictionary.web.component.scroll.ScrollToElementUtil.*;
import static org.complitex.resources.WebCommonResourceInitializer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.web.component.back.BackInfo;
import org.complitex.dictionary.web.component.back.BackInfoManager;
import org.complitex.dictionary.web.component.search.SearchComponentState;

/**
 *
 * @author Artem
 */
public class DomainObjectEditPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(DomainObjectEditPanel.class);
    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private LogBean logBean;
    @EJB
    private SessionBean sessionBean;
    private String entity;
    private String strategyName;
    private DomainObject oldObject;
    private DomainObject newObject;
    private Long parentId;
    private String parentEntity;
    private DomainObjectInputPanel objectInputPanel;
    private final String scrollListPageParameterName;
    private FeedbackPanel messages;
    private WebMarkupContainer permissionsPanelContainer;
    private Set<Long> parentSubjectIds;
    private final String backInfoSessionKey;

    public DomainObjectEditPanel(String id, String entity, String strategyName, Long objectId, Long parentId,
            String parentEntity, String scrollListPageParameterName, String backInfoSessionKey) {
        super(id);

        add(getHeaderContribution(SCROLL_JS));

        this.entity = entity;
        this.strategyName = strategyName;
        this.parentId = parentId;
        this.parentEntity = parentEntity;
        this.scrollListPageParameterName = scrollListPageParameterName;
        this.backInfoSessionKey = backInfoSessionKey;

        if (objectId == null) {
            //create new entity
            oldObject = null;
            newObject = getStrategy().newInstance();
        } else {
            //edit existing entity
            newObject = getStrategy().findById(objectId, false);
            if (newObject == null) {
                throw new RestartResponseException(getStrategy().getObjectNotFoundPage());
            }
            oldObject = cloneObject(newObject);
        }
        init();
    }

    protected IStrategy getStrategy() {
        return strategyFactory.getStrategy(strategyName, entity);
    }

    public DomainObject getNewObject() {
        return newObject;
    }

    protected DomainObject getOldObject() {
        return oldObject;
    }

    protected boolean isNew() {
        return oldObject == null;
    }

    public FeedbackPanel getMessages() {
        return messages;
    }

    protected void init() {
        IModel<String> labelModel = new LoadableDetachableModel<String>() {

            @Override
            protected String load() {
                final String entityName = stringBean.displayValue(getStrategy().getEntity().getEntityNames(), getLocale());
                return isNew() || !sessionBean.isAdmin() ? entityName
                        : MessageFormat.format(getString("label_edit"), entityName, newObject.getId());
            }
        };
        Label title = new Label("title", labelModel);
        add(title);
        final Label label = new Label("label", labelModel);
        label.setOutputMarkupId(true);
        add(label);

        messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        Form<Void> form = new Form<Void>("form");

        //input panel
        objectInputPanel = newInputPanel("domainObjectInputPanel", newObject, entity, strategyName, parentId, parentEntity);
        form.add(objectInputPanel);

        //children
        Component childrenContainer = new EmptyPanel("childrenContainer");
        if (!isNew()) {
            childrenContainer = new ChildrenContainer("childrenContainer", strategyName, entity, newObject);
        }
        form.add(childrenContainer);

        //history
        WebMarkupContainer historyContainer = new WebMarkupContainer("historyContainer");
        Link<Void> history = new Link<Void>("history") {

            @Override
            public void onClick() {
                setResponsePage(getStrategy().getHistoryPage(), getStrategy().getHistoryPageParams(newObject.getId()));
            }
        };
        historyContainer.add(history);
        historyContainer.setVisible(!isNew());
        form.add(historyContainer);

        //permissions panel
        permissionsPanelContainer = new WebMarkupContainer("permissionsPanelContainer");
        permissionsPanelContainer.setOutputMarkupId(true);
        form.add(permissionsPanelContainer);
        this.parentSubjectIds = initParentPermissions();
        permissionsPanelContainer.add(newPermissionsPanel("permissionsPanel", parentSubjectIds));

        //permissionPropagationDialogPanel
        final PermissionPropagationDialogPanel permissionPropagationDialogPanel =
                new PermissionPropagationDialogPanel("permissionPropagationDialogPanel") {

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
                            if (canPopagatePermissions && getStrategy().isNeedToChangePermission(oldObject.getSubjectIds(),
                                    newObject.getSubjectIds())) {
                                permissionPropagationDialogPanel.open(target);
                            } else {
                                save(false);
                            }
                        }
                    } else {
                        target.addComponent(messages);
                        scrollToMessages(target);
                    }
                } catch (Exception e) {
                    log.error("", e);
                    error(getString("db_error"));
                    target.addComponent(messages);
                    scrollToMessages(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.addComponent(messages);
                scrollToMessages(target);
            }

            private void scrollToMessages(AjaxRequestTarget target) {
                target.appendJavascript(scrollTo(label.getMarkupId()));
            }
        };
        submit.setVisible(DomainObjectAccessUtil.canEdit(strategyName, entity, newObject));
        form.add(submit);
        Link<Void> cancel = new Link<Void>("cancel") {

            @Override
            public void onClick() {
                back();
            }
        };
        cancel.setVisible(DomainObjectAccessUtil.canEdit(strategyName, entity, newObject));
        form.add(cancel);
        Link<Void> back = new Link<Void>("back") {

            @Override
            public void onClick() {
                back();
            }
        };
        back.setVisible(!DomainObjectAccessUtil.canEdit(strategyName, entity, newObject));
        form.add(back);
        add(form);
    }

    protected DomainObjectPermissionsPanel newPermissionsPanel(String id, Set<Long> parentSubjectIds) {
        return new DomainObjectPermissionsPanel(id, newObject.getSubjectIds(), parentSubjectIds,
                DomainObjectAccessUtil.canEdit(strategyName, entity, newObject));
    }

    protected DomainObjectInputPanel newInputPanel(String id, DomainObject newObject, String entity,
            String strategyName, Long parentId, String parentEntity) {
        return new DomainObjectInputPanel("domainObjectInputPanel", newObject, entity, strategyName, parentId, parentEntity);
    }

    protected boolean validate() {
        boolean valid = objectInputPanel.validateParent();
        valid &= validatePermissions();
        IValidator validator = getStrategy().getValidator();
        if (validator != null) {
            valid &= validator.validate(newObject, this);
        }
        return valid;
    }

    protected boolean validatePermissions() {
        if (newObject.getSubjectIds().isEmpty()) {
            error(getString("permissions_required"));
            return false;
        }
        return true;
    }

    protected void save(boolean propagate) {
        if (isNew()) {
            onInsert();
            getStrategy().insert(newObject, getCurrentDate());
        } else {
            onUpdate();
            if (!propagate) {
                getStrategy().update(oldObject, newObject, getCurrentDate());
            } else {
                getStrategy().updateAndPropagate(oldObject, newObject, getCurrentDate());
            }
        }

        logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                isNew() ? Log.EVENT.CREATE : Log.EVENT.EDIT, getStrategy(),
                oldObject, newObject, null);
        back();
    }

    protected Set<Long> initParentPermissions() {
        Set<Long> parentPermissions = null;
        if (isNew()) {
            if (getStrategy().getParentSearchFilters() != null && !getStrategy().getParentSearchFilters().isEmpty()) {
                List<String> inverseParentSearchFilters = Lists.newArrayList(getStrategy().getParentSearchFilters());
                Collections.reverse(inverseParentSearchFilters);
                SearchComponentState parentSearchComponentState = objectInputPanel.getParentSearchComponentState();
                for (String searchFilterEntity : inverseParentSearchFilters) {
                    DomainObject object = parentSearchComponentState.get(searchFilterEntity);
                    if (object != null && object.getId() != null && object.getId() > 0) {
                        parentPermissions = object.getSubjectIds();
                        break;
                    }
                }
            }
        }
        return parentPermissions;
    }

    public void updateParentPermissions(AjaxRequestTarget target, Set<Long> parentSubjectIds) {
        if (isNew()) {
            if (parentSubjectIds == null) {
                return;
            }
            if (parentSubjectIds.equals(this.parentSubjectIds)) {
                return;
            }
            this.parentSubjectIds = parentSubjectIds;
            permissionsPanelContainer.replace(newPermissionsPanel("permissionsPanel", this.parentSubjectIds));
            target.addComponent(permissionsPanelContainer);
        }
    }

    protected void onInsert() {
        visitChildren(AbstractComplexAttributesPanel.class, new IVisitor<AbstractComplexAttributesPanel>() {

            @Override
            public Object component(AbstractComplexAttributesPanel complexAttributesPanel) {
                complexAttributesPanel.onInsert();
                return CONTINUE_TRAVERSAL;
            }
        });
    }

    protected void onUpdate() {
        visitChildren(AbstractComplexAttributesPanel.class, new IVisitor<AbstractComplexAttributesPanel>() {

            @Override
            public Object component(AbstractComplexAttributesPanel complexAttributesPanel) {
                complexAttributesPanel.onUpdate();
                return CONTINUE_TRAVERSAL;
            }
        });
    }

    protected void back() {
        if (!Strings.isEmpty(backInfoSessionKey)) {
            BackInfo backInfo = BackInfoManager.get(this, backInfoSessionKey);
            if (backInfo != null) {
                backInfo.back(this);
                return;
            }
        }

        if (isNew() || (parentId == null && Strings.isEmpty(parentEntity))) {
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

    public void delete() {
        try {
            getStrategy().delete(newObject.getId(), getLocale());
            back();
        } catch (DeleteException e) {
            if (!Strings.isEmpty(e.getMessage())) {
                error(e.getMessage());
            } else {
                error(getString("delete_error"));
            }
        } catch (Exception e) {
            log.error("", e);
            error(getString("db_error"));
        }
    }
}
