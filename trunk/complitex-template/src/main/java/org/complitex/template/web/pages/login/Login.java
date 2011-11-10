package org.complitex.template.web.pages.login;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.complitex.dictionary.entity.Log.EVENT;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.Module;
import org.complitex.template.web.security.SecurityWebListener;
import org.odlabs.wiquery.core.commons.CoreJavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:16:45
 */
public final class Login extends WebPage {

    private static final Logger log = LoggerFactory.getLogger(Login.class);
    @EJB
    private SessionBean sessionBean;
    @EJB
    private LogBean logBean;

    public Login() {
        init();
    }

    private void init() {
        add(JavascriptPackageResource.getHeaderContribution(CoreJavaScriptResourceReference.get()));
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.COMMON_JS));
        add(JavascriptPackageResource.getHeaderContribution(getClass(), getClass().getSimpleName() + ".js"));
        add(CSSPackageResource.getHeaderContribution(WebCommonResourceInitializer.STYLE_CSS));

        add(new Label("login.title", new ResourceModel("login.title")));
        final FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);
        final IModel<String> headerModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return getSession().getFeedbackMessages().hasMessage(new IFeedbackMessageFilter() {

                    @Override
                    public boolean accept(FeedbackMessage message) {
                        return message.isError();
                    }
                }) ? getString("login.error.label") : getString("login.title");
            }
        };
        final Label header = new Label("login.header", headerModel);
        header.setOutputMarkupId(true);
        add(header);

        Form<Void> form = new Form<Void>("form");
        final IModel<String> loginModel = new Model<String>();
        form.add(new TextField<String>("login", loginModel).setRequired(true));
        final IModel<String> passwordModel = new Model<String>();
        form.add(new PasswordTextField("password", passwordModel));

        form.add(new Button("submit") {

            @Override
            public void onSubmit() {
                HttpServletRequest request = ((WebRequestCycle) getRequestCycle()).getWebRequest().getHttpServletRequest();

                String login = loginModel.getObject();
                String password = passwordModel.getObject();

                boolean isError = false;

                try {
                    request.login(login, password);
                } catch (ServletException e) {
                    log.warn("Login failed. User login: " + login + ", password: " + password + ".", e);
                    error(getString("login.error.unauthorized"));
                    isError = true;
                }

                if (!isError) {
                    //additional check for blocked users
                    try {
                        if (sessionBean.isBlockedUser(login)) { // blocked user attempts to log in.
                            log.warn("Blocked user attempts to log in. User login: {}", login);
                            error(getString("login.error.blocked"));
                            isError = true;
                            logBean.warn(Module.NAME, SecurityWebListener.class, null, null, EVENT.USER_LOGIN,
                                    "Заблокированный пользователь пытается попасть в систему. Логин пользователя: {0}, IP: {1}",
                                    login, request.getRemoteAddr());
                        }
                    } catch (Exception e) {
                        log.error("", e);
                        error(getString("login.error.db_error"));
                        isError = true;
                    }

                    if (isError) {
                        //logout user.
                        try {
                            request.logout();
                        } catch (ServletException e) {
                            log.error("Couldn't to log out user.");
                            throw new IllegalStateException(e);
                        }
                    }
                }

                if (!isError) {
                    /*
                     * Custom algorithm to achieve correct loading of user preferences into session.
                     * 1. Log out
                     * 2. Session invalidate.
                     * 3. Redirect to LoginSuccessServlet in order to start fresh session, log in and load correct 
                     *  prefererences into session.
                     */
                    //1. Log out
                    try {
                        request.logout();
                    } catch (ServletException e) {
                        log.error("Couldn't to logout user.");
                        throw new IllegalStateException(e);
                    }

                    //2. Session invalidate
                    getSession().invalidateNow();

                    //3. Redirect
                    String url = LoginSuccessServlet.PATH + "?login=" + Hex.encodeHexString(login.getBytes())
                            + "&password=" + Hex.encodeHexString(password.getBytes());
                    getRequestCycle().setRequestTarget(new RedirectRequestTarget(url));
                }
            }
        });
        add(form);
    }
}
