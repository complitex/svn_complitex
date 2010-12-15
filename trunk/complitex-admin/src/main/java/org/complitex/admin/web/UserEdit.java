package org.complitex.admin.web;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.LogChange;
import org.complitex.dictionary.entity.User;
import org.complitex.dictionary.entity.UserGroup;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.admin.Module;
import org.complitex.admin.service.UserBean;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.complitex.dictionary.entity.UserGroup.GROUP_NAME.ADMINISTRATORS;
import static org.complitex.dictionary.entity.UserGroup.GROUP_NAME.EMPLOYEES;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.07.2010 14:12:33
 *
 *  Страница создания и редактирования пользователя
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class UserEdit extends FormTemplatePage{
    private static final Logger log = LoggerFactory.getLogger(UserEdit.class);

    @EJB(name = "UserBean")
    private UserBean userBean;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringCultureBean;

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

    private void init(final Long id){
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
        Button save = new Button("save"){
            @Override
            public void onSubmit() {
                User user = userModel.getObject();

                try {
                    if (id == null && !userBean.isUniqueLogin(user.getLogin())){
                        error(getString("error.login_not_unique"));
                        return;
                    }

                    userBean.save(user);

                    logBean.info(Module.NAME, UserEdit.class, User.class, null, user.getId(),
                            (id == null) ? Log.EVENT.CREATE : Log.EVENT.EDIT, getLogChanges(oldUser, user), null);

                    log.info("Пользователь сохранен: {}", user);
                    getSession().info(getString("info.saved"));

                } catch (Exception e) {
                    log.error("Ошибка сохранения пользователя", e);
                    getSession().error(getString("error.saved"));
                }

                setResponsePage(UserList.class);
            }
        };
        form.add(save);

        //Отмена
        Button cancel = new Button("cancel"){
            @Override
            public void onSubmit() {
               setResponsePage(UserList.class);
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
                UserBean.USER_INFO_ENTITY_TABLE, null, null);
        form.add(userInfo);

        //Группы привилегий
        CheckGroup<UserGroup> usergroups = new CheckGroup<UserGroup>("usergroups",
                new PropertyModel<Collection<UserGroup>>(userModel, "userGroups"));

        usergroups.add(new Check<UserGroup>("ADMINISTRATORS", getUserGroup(userModel.getObject(), ADMINISTRATORS)));
        usergroups.add(new Check<UserGroup>("EMPLOYEES", getUserGroup(userModel.getObject(), EMPLOYEES)));

        form.add(usergroups);
    }

    private IModel<UserGroup> getUserGroup(User user, UserGroup.GROUP_NAME group_name){
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

    private List<LogChange> getLogChanges(User oldUser, User newUser){
        List<LogChange> logChanges = new ArrayList<LogChange>();

        //логин
        if (newUser.getId() == null){
            logChanges.add(new LogChange(getString("login"), null, newUser.getLogin()));
        }

        //пароль
        if (newUser.getNewPassword() != null){
            logChanges.add(new LogChange(getString("password"), oldUser.getPassword(),
                    DigestUtils.md5Hex(newUser.getNewPassword())));
        }

        //информация о пользователе
        List<LogChange> userInfoLogChanges = logBean.getLogChanges( userBean.getUserInfoStrategy(),
                oldUser != null ? oldUser.getUserInfo() : null, newUser.getUserInfo(), getLocale());

        logChanges.addAll(userInfoLogChanges);

        //группы привилегий
        if (oldUser == null){
            for (UserGroup ng : newUser.getUserGroups()){
                logChanges.add(new LogChange(getString("usergroup"), null, getString(ng.getGroupName().name())));
            }
        }else{
            for (UserGroup og : oldUser.getUserGroups()){ //deleted group
                boolean deleted = true;

                for (UserGroup ng : newUser.getUserGroups()){
                    if (ng.getGroupName().equals(og.getGroupName())){
                        deleted = false;
                        break;
                    }
                }

                if (deleted){
                    logChanges.add(new LogChange(getString("usergroup"), getString(og.getGroupName().name()), null));
                }
            }

            for (UserGroup ng : newUser.getUserGroups()){ //added group
                boolean added = true;

                for (UserGroup og : oldUser.getUserGroups()){
                    if (og.getGroupName().equals(ng.getGroupName())){
                        added = false;
                        break;
                    }
                }

                if (added){
                    logChanges.add(new LogChange(getString("usergroup"), null, getString(ng.getGroupName().name())));
                }
            }
        }

        return logChanges;
    }

}
