package org.complitex.admin.web;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.admin.Module;
import org.complitex.admin.service.UserBean;
import org.complitex.admin.strategy.UserInfoStrategy;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.complitex.dictionary.entity.UserGroup.GROUP_NAME.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.07.2010 14:12:33
 *
 *  Страница создания и редактирования пользователя
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class UserEdit extends FormTemplatePage {
    private static final Logger log = LoggerFactory.getLogger(UserEdit.class);

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    @EJB(name = "UserBean")
    private UserBean userBean;

    @EJB
    private UserInfoStrategy userInfoStrategy;
    
    @EJB(name = "LogBean")
    private LogBean logBean;

    public UserEdit() {
        super();
        init(null);
    }

    public UserEdit(final PageParameters parameters) {
        super();
        init(parameters.getAsLong("user_id"));
    }

    private void init(final Long id) {
        add(new Label("title", new ResourceModel("title")));
        add(new FeedbackPanel("messages"));

        //Модель данных
        //todo catch exception
        final IModel<User> userModel = new Model<User>(id != null ? userBean.getUser(id) : userBean.newUser());

        final User oldUser = (id != null) ? CloneUtil.cloneObject(userModel.getObject()) : null;

        //Форма
        Form form = new Form<User>("form");
        add(form);

        //Сохранить
        Button save = new Button("save") {

            @Override
            public void onSubmit() {
                User user = userModel.getObject();

                try {
                    if (id == null && !userBean.isUniqueLogin(user.getLogin())) {
                        error(getString("error.login_not_unique"));
                        return;
                    }

                    userBean.save(user);

                    logBean.info(Module.NAME, UserEdit.class, User.class, null, user.getId(),
                            (id == null) ? Log.EVENT.CREATE : Log.EVENT.EDIT, getLogChanges(oldUser, user), null);

                    log.info("Пользователь сохранен: {}", user);
                    getSession().info(getString("info.saved"));
                    back(id);
                } catch (Exception e) {
                    log.error("Ошибка сохранения пользователя", e);
                    getSession().error(getString("error.saved"));
                }
            }
        };
        form.add(save);

        //Отмена
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
                back(id);
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);

        //Логин
        RequiredTextField login = new RequiredTextField<String>("login", new PropertyModel<String>(userModel, "login"));
        login.setEnabled(id == null);
        form.add(login);

        //Пароль
        PasswordTextField password = new PasswordTextField("password", new PropertyModel<String>(userModel, "newPassword"));
        password.setEnabled(id != null);
        password.setRequired(false);
        form.add(password);

        //Информация о пользователе
        DomainObjectInputPanel userInfo = new DomainObjectInputPanel("user_info", userModel.getObject().getUserInfo(),
                "user_info", "UserInfoStrategy", null, null);
        form.add(userInfo);

        //Группы привилегий
        CheckGroup<UserGroup> usergroups = new CheckGroup<UserGroup>("usergroups",
                new PropertyModel<Collection<UserGroup>>(userModel, "userGroups"));

        usergroups.add(new Check<UserGroup>("ADMINISTRATORS", getUserGroup(userModel.getObject(), ADMINISTRATORS)));
        usergroups.add(new Check<UserGroup>("EMPLOYEES", getUserGroup(userModel.getObject(), EMPLOYEES)));
        usergroups.add(new Check<UserGroup>("EMPLOYEES_CHILD_VIEW", getUserGroup(userModel.getObject(), EMPLOYEES_CHILD_VIEW)));

        form.add(usergroups);

        //Организация
        final WebMarkupContainer organizationContainer = new WebMarkupContainer("organizationContainer");
        organizationContainer.setOutputMarkupId(true);
        form.add(organizationContainer);

        RadioGroup<UserOrganization> organizationGroup = new RadioGroup<UserOrganization>("organizationGroup",
                new PropertyModel<UserOrganization>(userModel, "mainUserOrganization"));
        organizationContainer.add(organizationGroup);

        organizationGroup.add(new ListView<UserOrganization>("userOrganizations",
                new PropertyModel<List<? extends UserOrganization>>(userModel, "userOrganizations")){
            {
                setReuseItems(true);
            }

            @Override
            protected void populateItem(ListItem<UserOrganization> item) {
                final UserOrganization userOrganization = item.getModelObject();
                final ListView listView = this;

                item.add(new Radio<UserOrganization>("radio", item.getModel()));

                item.add(new Label("name", organizationStrategy.displayDomainObject(
                        organizationStrategy.findById(userOrganization.getOrganizationObjectId(), true), getLocale())));

                item.add(new AjaxLink("delete"){

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        List<UserOrganization> list = userModel.getObject().getUserOrganizations();
                        for (int i = 0; i < list.size(); ++i){
                            if (list.get(i).getOrganizationObjectId().equals(userOrganization.getOrganizationObjectId())){
                                list.remove(i);
                                break;
                            }
                        }

                        listView.removeAll();

                        target.addComponent(organizationContainer);
                    }
                });
            }
        });

        //Добавить организацию
        Form addOrganizationForm = new Form("addOrganizationForm");
        form.add(addOrganizationForm);

        final IModel<Long> addOrganization = new Model<Long>();

        addOrganizationForm.add(new UserOrganizationPicker("organization", addOrganization));
        addOrganizationForm.add(new AjaxSubmitLink("addOrganization", addOrganizationForm){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (addOrganization.getObject() == null){
                    return;
                }

                List<UserOrganization> list = userModel.getObject().getUserOrganizations();
                for (UserOrganization uo : list){
                    if (uo.getOrganizationObjectId().equals(addOrganization.getObject())){
                        error(getString("error.organization_already_added"));
                        return;
                    }
                }

                list.add(new UserOrganization(addOrganization.getObject()));
                target.addComponent(organizationContainer);
            }
        });
    }

    private IModel<UserGroup> getUserGroup(User user, UserGroup.GROUP_NAME group_name) {
        if (!user.getUserGroups().isEmpty()) {
            for (UserGroup userGroup : user.getUserGroups()) {
                if (userGroup.getGroupName().equals(group_name)) {
                    return new Model<UserGroup>(userGroup);
                }
            }
        }

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupName(group_name);
        return new Model<UserGroup>(userGroup);
    }

    private List<LogChange> getLogChanges(User oldUser, User newUser) {
        List<LogChange> logChanges = new ArrayList<LogChange>();

        //логин
        if (newUser.getId() == null) {
            logChanges.add(new LogChange(getString("login"), null, newUser.getLogin()));
        }

        //пароль
        if (newUser.getNewPassword() != null) {
            logChanges.add(new LogChange(getString("password"), oldUser.getPassword(),
                    DigestUtils.md5Hex(newUser.getNewPassword())));
        }

        //информация о пользователе
        List<LogChange> userInfoLogChanges = logBean.getLogChanges(userInfoStrategy,
                oldUser != null ? oldUser.getUserInfo() : null, newUser.getUserInfo(), getLocale());

        logChanges.addAll(userInfoLogChanges);

        //группы привилегий
        if (oldUser == null) {
            for (UserGroup ng : newUser.getUserGroups()) {
                logChanges.add(new LogChange(getString("usergroup"), null, getString(ng.getGroupName().name())));
            }
        } else {
            for (UserGroup og : oldUser.getUserGroups()) { //deleted group
                boolean deleted = true;

                for (UserGroup ng : newUser.getUserGroups()) {
                    if (ng.getGroupName().equals(og.getGroupName())) {
                        deleted = false;
                        break;
                    }
                }

                if (deleted) {
                    logChanges.add(new LogChange(getString("usergroup"), getString(og.getGroupName().name()), null));
                }
            }

            for (UserGroup ng : newUser.getUserGroups()) { //added group
                boolean added = true;

                for (UserGroup og : oldUser.getUserGroups()) {
                    if (og.getGroupName().equals(ng.getGroupName())) {
                        added = false;
                        break;
                    }
                }

                if (added) {
                    logChanges.add(new LogChange(getString("usergroup"), null, getString(ng.getGroupName().name())));
                }
            }
        }

        return logChanges;
    }

    private void back(Long userId) {
        if (userId != null) {
            PageParameters params = new PageParameters();
            params.put(UserList.SCROLL_PARAMETER, userId);
            setResponsePage(UserList.class, params);
        } else {
            setResponsePage(UserList.class);
        }
    }
}
