package org.complitex.template.web.security;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.complitex.template.web.pages.login.Login;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 17:53:29
 *
 * Приложение Wicket, которое использует авторизацию сервлет контейнера.
 */
public abstract class ServletAuthWebApplication extends WebApplication implements IRoleCheckingStrategy,
        IUnauthorizedComponentInstantiationListener {

    @Override
    protected void init() {
        super.init();
        getSecuritySettings().setAuthorizationStrategy(new RoleAuthorizationStrategy(this));
        getSecuritySettings().setUnauthorizedComponentInstantiationListener(this);
    }

    @Override
    public boolean hasAnyRole(Roles roles) {
        HttpServletRequest request = ((WebRequestCycle) RequestCycle.get()).getWebRequest().getHttpServletRequest();
        if (roles != null) {
            for (String role : roles) {
                if (request.isUserInRole(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyRole(String... roles) {
        return hasAnyRole(new Roles(roles));
    }

    @Override
    public void onUnauthorizedInstantiation(Component component) {
        WebRequestCycle webRequestCycle = (WebRequestCycle) RequestCycle.get();
        HttpServletRequest servletRequest = webRequestCycle.getWebRequest().getHttpServletRequest();
        boolean sessionNotExist = servletRequest.getSession(false) == null;
        if (sessionNotExist) {
            Session.get().invalidateNow();
            RequestCycle.get().setRedirect(true);
            throw new RestartResponseException(getApplicationSettings().getPageExpiredErrorPage());
        } else {
            throw new UnauthorizedInstantiationException(component.getClass());
        }
    }

    /**
     * Helper method in order for logout. Must be used in pages where logout action is required.
     */
    public void logout() {
        Session.get().invalidateNow();
        RequestCycle.get().setRedirect(true);
        RequestCycle.get().setResponsePage(Login.class);
    }
}
