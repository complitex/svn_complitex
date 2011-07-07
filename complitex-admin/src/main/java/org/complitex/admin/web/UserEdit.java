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
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.PreferenceBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.template.web.component.LocalePicker;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.*;
import java.util.Locale;

import static org.complitex.dictionary.entity.UserGroup.GROUP_NAME.*;
import static org.complitex.dictionary.web.DictionaryFwSession.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.07.2010 14:12:33
 *
 *  Страница создания и редактирования пользователя
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class UserEdit extends FormTemplatePage {
    private static final Logger log = LoggerFactory.getLogger(UserEdit.class);

    private static final List<String> SEARCH_FILTERS = Arrays.asList("country", "region", "city", "street");

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    @EJB
    private UserBean userBean;

    @EJB
    private UserInfoStrategy userInfoStrategy;

    @EJB
    private LogBean logBean;

    @EJB
    private PreferenceBean preferenceBean;

    @EJB
    private LocaleBean localeBean;

    public UserEdit() {
        super();
        init(null, false);
    }

    public UserEdit(final PageParameters parameters) {
        super();
        init(parameters.getAsLong("user_id"), "copy".equals(parameters.getString("action")));
    }

    private void init(Long userId, boolean copyUser) {
        add(new Label("title", new ResourceModel("title")));
        add(new FeedbackPanel("messages"));

        //Модель данных
        User user = userId != null ? userBean.getUser(userId) : userBean.newUser();
        final IModel<User> userModel = new Model<User>(user);

        final Preference useDefaultPreference = preferenceBean.getOrCreatePreference(userId, GLOBAL_PAGE, IS_USE_DEFAULT_STATE_KEY);

        final SearchComponentState searchComponentState = new SearchComponentState();

        if (userId != null) {
            for (String s : SEARCH_FILTERS){
                searchComponentState.put(s, preferenceBean.getPreferenceDomainObject(userId, DEFAULT_STATE_PAGE, s));
            }
        }

        //Копирование
        if (copyUser){
            userId = null;
            user.setId(null);
            user.setUserInfoObjectId(null);

            for (UserOrganization userOrganization : user.getUserOrganizations()){
                userOrganization.setId(null);
                userOrganization.setUserId(null);
            }

            for (UserGroup userGroup : user.getUserGroups()){
                userGroup.setId(null);
                userGroup.setLogin(null);
            }

            useDefaultPreference.setId(null);
            useDefaultPreference.setUserId(null);
        }

        final User oldUser = (userId != null) ? CloneUtil.cloneObject(userModel.getObject()) : null;

        //Форма
        Form form = new Form<User>("form");
        add(form);

        //Логин
        RequiredTextField login = new RequiredTextField<String>("login", new PropertyModel<String>(userModel, "login"));
        login.setEnabled(userId == null);
        form.add(login);

        //Пароль
        PasswordTextField password = new PasswordTextField("password", new PropertyModel<String>(userModel, "newPassword"));
        password.setEnabled(userId != null);
        password.setRequired(false);
        form.add(password);

        //Информация о пользователе
        DomainObjectInputPanel userInfo = new DomainObjectInputPanel("user_info", userModel.getObject().getUserInfo(),
                "user_info", "UserInfoStrategy", null, null);
        form.add(userInfo);

        //Локаль
        Preference localePreference = preferenceBean.getPreference(userId, GLOBAL_PAGE, LOCALE_KEY);

        final IModel<java.util.Locale> localeModel = new Model<Locale>(localePreference != null
                ? new Locale(localePreference.getValue())
                : localeBean.getSystemLocale());
        form.add(new LocalePicker("locale", localeModel, false));

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

        final RadioGroup<Long> organizationGroup = new RadioGroup<Long>("organizationGroup",
                new PropertyModel<Long>(userModel, "mainUserOrganization"));
        organizationContainer.add(organizationGroup);

        organizationGroup.add(new ListView<UserOrganization>("userOrganizations",
                new PropertyModel<List<? extends UserOrganization>>(userModel, "userOrganizations")){
            {
                setReuseItems(true);
            }

            @Override
            protected void populateItem(final ListItem<UserOrganization> item) {
                final UserOrganization userOrganization = item.getModelObject();
                final ListView listView = this;

                item.add(new Radio<Long>("radio", new Model<Long>(userOrganization.getOrganizationObjectId())));

                item.add(new UserOrganizationPicker("picker", new PropertyModel<Long>(userOrganization, "organizationObjectId"), true));

                item.add(new AjaxLink("delete"){

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        userModel.getObject().getUserOrganizations().remove(item.getIndex());

                        listView.removeAll();

                        target.addComponent(organizationContainer);
                    }
                });
            }
        });

        //Добавить организацию
        Form addOrganizationForm = new Form("addOrganizationForm");
        form.add(addOrganizationForm);

        addOrganizationForm.add(new AjaxSubmitLink("addOrganization", addOrganizationForm){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                userModel.getObject().getUserOrganizations().add(new UserOrganization());

                target.addComponent(organizationContainer);
            }
        });

        //Адрес по умолчанию
        final Model<Boolean> useDefaultModel = new Model<Boolean>(Boolean.valueOf(useDefaultPreference.getValue()));
        form.add(new CheckBox("use_default_address", useDefaultModel));


        form.add(new WiQuerySearchComponent("searchComponent", searchComponentState, SEARCH_FILTERS, null, ShowMode.ALL, true));

        //Сохранить
        Button save = new Button("save") {

            @Override
            public void onSubmit() {
                User user = userModel.getObject();

                try {
                    if (user.getId() == null && !userBean.isUniqueLogin(user.getLogin())) {
                        error(getString("error.login_not_unique"));
                        return;
                    }

                    userBean.save(user);

                    //Локаль
                    preferenceBean.save(user.getId(), GLOBAL_PAGE, LOCALE_KEY, localeModel.getObject().getLanguage());

                    //Адрес по умолчанию
                    for (String s : SEARCH_FILTERS){
                        DomainObject domainObject =  searchComponentState.get(s);

                        if (domainObject != null) {
                            preferenceBean.save(user.getId(), DEFAULT_STATE_PAGE, s, domainObject.getId() + "");
                        }
                    }

                    //Использовать ли адрес по умолчанию при входе в систему
                    useDefaultPreference.setUserId(user.getId());
                    useDefaultPreference.setValue(useDefaultModel.getObject().toString());
                    preferenceBean.save(useDefaultPreference);

                    logBean.info(Module.NAME, UserEdit.class, User.class, null, user.getId(),
                            (user.getId() == null) ? Log.EVENT.CREATE : Log.EVENT.EDIT, getLogChanges(oldUser, user), null);

                    log.info("Пользователь сохранен: {}", user);
                    getSession().info(getString("info.saved"));
                    back(user.getId());
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
                back(userModel.getObject().getId());
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
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
