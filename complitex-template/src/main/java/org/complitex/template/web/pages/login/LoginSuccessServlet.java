/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages.login;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.complitex.template.web.pages.welcome.WelcomePage;
import org.complitex.template.web.security.SecurityWebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@WebServlet(LoginSuccessServlet.PATH)
public class LoginSuccessServlet extends HttpServlet {

    static final String PATH = "/login_success";
    private static final Logger log = LoggerFactory.getLogger(LoginSuccessServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse responce) {
        String context = request.getServletContext().getContextPath();
        boolean loggedIn = false;
        try {
            String login = request.getParameter("login");
            String password = request.getParameter("password");

            request.login(login, password);
            request.getSession().setAttribute(SecurityWebListener.LOGGED_IN, login);
            loggedIn = true;
            
            //TODO: investigate better solution because this will be broken as url mounting overrides.
            String url = context + "/?wicket:bookmarkablePage=:" + WelcomePage.class.getName();
            responce.encodeRedirectURL(url);
            responce.sendRedirect(url);
        } catch (Exception e) {
            log.error("", e);

            if (loggedIn) {
                request.getSession().removeAttribute(SecurityWebListener.LOGGED_IN);
                try {
                    request.logout();
                } catch (ServletException se) {
                    log.error("Couldn't to log out user.", se);
                }
            }

            String loginUrl = context + "/?wicket:bookmarkablePage=:" + Login.class.getName();
            try {
                responce.sendRedirect(loginUrl);
            } catch (IOException io) {
                log.error("Couldn't to redirect to login page", io);
            }
        }
    }
}
