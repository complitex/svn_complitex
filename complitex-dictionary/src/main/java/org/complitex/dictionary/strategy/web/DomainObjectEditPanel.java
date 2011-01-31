/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.complitex.dictionary.strategy.web.validate.IValidator;
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
import org.complitex.dictionary.entity.Subject;
import org.complitex.dictionary.service.PermissionBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionsPanel;

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
    @EJB
    private PermissionBean permissionBean;
    private String entity;
    private DomainObject oldObject;
    private DomainObject newObject;
    private Long parentId;
    private String parentEntity;
    private DomainObjectInputPanel objectInputPanel;
    private final String scrollListPageParameterName;
    private Set<Long> oldSubjectIds;
    private Set<Long> newSubjectIds;

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
            newObject = getStrategy().findById(objectId);
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
        oldSubjectIds = Sets.newHashSet();
        if (!isNew()) {
            long permissionId = oldObject.getPermissionId();
            if (permissionId == PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) {
                oldSubjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
            } else {
                oldSubjectIds.addAll(permissionBean.findSubjectIds(newObject.getPermissionId()));
            }
            newSubjectIds = CloneUtil.cloneObject(oldSubjectIds);
        } else {
            newSubjectIds = Sets.newHashSet();
            newSubjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        }

        DomainObjectPermissionsPanel permissionsPanel = new DomainObjectPermissionsPanel("permissionsPanel", newSubjectIds);
        form.add(permissionsPanel);

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

    protected void handlePermission() {
        //check if visible-by-all subject has been selected along with some actual subjects(organizations)
        if(newSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) && newSubjectIds.size() > 1){
            newSubjectIds.clear();
            newSubjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        }

        if (oldSubjectIds.equals(newSubjectIds)) {
            // object references to the same subjects set therefore no need to modify permission_id
        } else {
            // object references to new subjects set therefore it has to modify permission_id
            List<Subject> subjects = Lists.newArrayList();
            for (Long subjectId : newSubjectIds) {
                subjects.add(new Subject("organization", subjectId));
            }

            if (subjects.size() == 1 && subjects.get(0).getObjectId() == PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) {
                newObject.setPermissionId(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
            } else {
                Long newPermissionId = permissionBean.getPermission(entity, subjects);
                newObject.setPermissionId(newPermissionId);
            }
        }
    }

    protected void save() {
        try {
            if (validate()) {
                //permission related logic
                handlePermission();

                if (isNew()) {
                    getStrategy().insert(newObject);
                } else {
                    getStrategy().update(oldObject, newObject, DateUtil.getCurrentDate());
                }

                logBean.log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                        isNew() ? Log.EVENT.CREATE : Log.EVENT.EDIT, getStrategy(),
                        oldObject, newObject, getLocale(), null);
                back();
            }
        } catch (Exception e) {
            log.error("", e);
            error(getString("db_error"));
        }
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
