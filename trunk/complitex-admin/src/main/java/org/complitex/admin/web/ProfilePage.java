package org.complitex.admin.web;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.service.exception.WrongCurrentPasswordException;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.template.web.component.LocalePicker;
import org.complitex.template.web.pages.welcome.WelcomePage;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.complitex.dictionary.web.DictionaryFwSession.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.07.11 16:40
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ProfilePage extends FormTemplatePage{
    @EJB
    private LocaleBean localeBean;

    @EJB
    private SessionBean sessionBean;

    public ProfilePage() {
        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        //Форма
        Form form = new Form("form");
        add(form);

        //Локаль
        String language = getTemplateSession().getPreferenceString(GLOBAL_PAGE, LOCALE_KEY);

        final IModel<Locale> localeModel = new Model<Locale>(language != null ? new Locale(language) : localeBean.getSystemLocale());
        form.add(new LocalePicker("locale", localeModel, false));

        //Пароль
        final PasswordTextField currentPassword = new PasswordTextField("current_password", new Model<String>(""));
        form.add(currentPassword.setRequired(false));

        final PasswordTextField password = new PasswordTextField("password", new Model<String>(""));
        form.add(password.setRequired(false));

        final PasswordTextField password2 = new PasswordTextField("password2", new Model<String>(""));
        form.add(password2.setRequired(false));

        //Адрес по умолчанию
        final List<String> searchFilters = Arrays.asList("country", "region", "city", "street");

        Preference useDefaultPreference = getTemplateSession().getOrCreatePreference(GLOBAL_PAGE, IS_USE_DEFAULT_STATE_KEY);
        final Model<Boolean> useDefaultModel = new Model<Boolean>(Boolean.valueOf(useDefaultPreference.getValue()));
        form.add(new CheckBox("use_default_address", useDefaultModel));

        final SearchComponentState searchComponentState = new SearchComponentState();

        for (String s : searchFilters){
            searchComponentState.put(s, getTemplateSession().getPreferenceDomainObject(DEFAULT_STATE_PAGE, s));
        }

        form.add(new WiQuerySearchComponent("searchComponent", searchComponentState, searchFilters, null, ShowMode.ALL, true));

        //Сохранение
        form.add(new Button("save"){
            @Override
            public void onSubmit() {
                if (password.getModelObject() != null && !password.getModelObject().isEmpty()) {
                    //Пароль
                    if (!password.getModelObject().equals(password2.getModelObject())){
                        error(getString("error_passwords_not_match"));

                        return;
                    }

                    if (currentPassword.getModelObject() != null) {
                        try {
                            sessionBean.updatePassword(currentPassword.getModelObject(), password.getModelObject());
                        } catch (WrongCurrentPasswordException e) {
                            error(getString("error_wrong_current_password"));
                        }
                    } else {
                        error(getString("error_wrong_current_password"));
                    }
                }

                //Локаль
                getSession().setLocale(localeModel.getObject());

                //Адрес по умолчанию
                for (String s : searchFilters){
                    DomainObject domainObject = searchComponentState.get(s);

                    if (domainObject != null) {
                        getTemplateSession().putPreference(DEFAULT_STATE_PAGE, s, domainObject.getId() + "", true);
                    }
                }

                //Использовать ли адрес по умолчанию при входе в систему
                getTemplateSession().putPreference(GLOBAL_PAGE, IS_USE_DEFAULT_STATE_KEY, useDefaultModel.getObject().toString(), true);

                info(getString("info_saved"));
            }
        });

        form.add(new Button("cancel"){
            @Override
            public void onSubmit() {
                setResponsePage(WelcomePage.class);
            }
        });
    }
}
