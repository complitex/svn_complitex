package org.complitex.template.web.security;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.User;
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
    private static final Logger log = LoggerFactory.getLogger(SecurityWebListener.class);
    private final static String PRINCIPAL = "org.complitex.pspoffice.commons.web.security.PRINCIPAL";
    private final static ConcurrentHashMap<String, HttpSession> activeSession = new ConcurrentHashMap<String, HttpSession>();

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        activeSession.put(event.getSession().getId(), event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        activeSession.remove(event.getSession().getId());

        //logout
        long start = event.getSession().getCreationTime();
        long end = event.getSession().getLastAccessedTime();

        String time = DateUtil.getTimeDiff(start, end);

        logBean.info(Module.NAME, SecurityWebListener.class, User.class, null, Log.EVENT.USER_LOGOFF,
                "Длительность сессии: {0}", time);
        log.info("Сессия пользователя деактивированна [{}]", time);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

        //login
        if (request.getUserPrincipal() != null && request.getSession().getAttribute(PRINCIPAL) == null){
            request.getSession().setAttribute(PRINCIPAL, request.getUserPrincipal().getName());

            logBean.info(Module.NAME, SecurityWebListener.class, User.class, null, Log.EVENT.USER_LOGIN,
                    "ip: {0}", request.getRemoteAddr());
            log.info("Пользователь авторизирован [login: {}, ip: {}]", request.getUserPrincipal().getName(),
                    request.getRemoteAddr());
        }
    }

    //todo add secure role
    public static synchronized List<HttpSession> getSessions(String principal){
        List<HttpSession> sessions = new ArrayList<HttpSession>();

        for (HttpSession session : activeSession.values()){
            if(principal.equals(session.getAttribute(PRINCIPAL))){
                sessions.add(session);
            }
        }

        return sessions;
    }

    public static synchronized Collection<HttpSession> getSessions(){
        return activeSession.values();
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
