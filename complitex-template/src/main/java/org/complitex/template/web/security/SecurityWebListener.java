package org.complitex.template.web.security;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.template.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:24:34
 *
 * Класс слушает создание http сессий и запросов, и сохраняет в сессию информацию об авторизованном пользователе.
 */
@WebListener
public class SecurityWebListener implements HttpSessionListener, ServletRequestListener, ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(SecurityWebListener.class);

    private final static String USER_LOGIN = SecurityWebListener.class.getName() + ".USER_LOGIN";
    public static final String LOGGED_IN = SecurityWebListener.class.getName() + ".LOGGED_IN";
    private final static ConcurrentHashMap<String, HttpSession> activeSessions = new ConcurrentHashMap<String, HttpSession>();

    @EJB
    public LogBean logBean;

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        activeSessions.put(event.getSession().getId(), event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (activeSessions.remove(event.getSession().getId()) != null) {

            //check that it was not anonymous session
            String login = (String) event.getSession().getAttribute(USER_LOGIN);
            if (login != null) {
                //logout
                long start = event.getSession().getCreationTime();
                long end = event.getSession().getLastAccessedTime();

                String time = DateUtil.getTimeDiff(start, end);

                logBean.logOut(login, Module.NAME, SecurityWebListener.class, "Длительность сессии: {0}", time);
                log.info("Сессия пользователя деактивированна [login: {}, time: {}]", login, time);
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

        //login
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LOGGED_IN) != null) {
            String login = (String) session.getAttribute(LOGGED_IN);
            session.setAttribute(USER_LOGIN, login);

            logBean.logIn(login, Module.NAME, SecurityWebListener.class, "IP: {0}", request.getRemoteAddr());
            log.info("Пользователь авторизован [login: {}, ip: {}]", login, request.getRemoteAddr());

            session.removeAttribute(LOGGED_IN);
        }
    }

    //todo add secure role
    public static synchronized List<HttpSession> getSessions(String principal) {
        List<HttpSession> sessions = new ArrayList<HttpSession>();

        for (HttpSession session : activeSessions.values()) {
            if (principal.equals(session.getAttribute(USER_LOGIN))) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    public static synchronized Collection<HttpSession> getSessions() {
        return activeSessions.values();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logBean.info(Module.NAME, SecurityWebListener.class, null, null, Log.EVENT.SYSTEM_START, null);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //javax.ejb.EJBException: Attempt to invoke when container is in Undeployed
    }
}
