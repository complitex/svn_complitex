/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages.login;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.complitex.template.web.pages.welcome.WelcomePage;
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
        try {
            String login = request.getParameter("login");
            String password = request.getParameter("password");

            request.login(login, password);
            //TODO: investigate better solution because this will be broken as url mounting overrides.
            String url = context + "/?wicket:bookmarkablePage=:" + WelcomePage.class.getName();
            responce.sendRedirect(url);
        } catch (Exception e) {
            log.error("", e);
            String loginUrl = context + "/?wicket:bookmarkablePage=:" + Login.class.getName();
            try {
                responce.sendRedirect(loginUrl);
            } catch (IOException io) {
                log.error("Couldn't to redirect to login page", io);
            }
        }
    }
}
