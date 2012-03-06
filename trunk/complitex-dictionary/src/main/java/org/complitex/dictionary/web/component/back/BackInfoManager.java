/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.back;

import javax.servlet.http.HttpSession;
import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebRequest;

/**
 *
 * @author Artem
 */
public final class BackInfoManager {

    private BackInfoManager() {
    }

    private static HttpSession session(Component pageComponent) {
        return ((WebRequest) pageComponent.getRequest()).getHttpServletRequest().getSession();
    }

    public static void put(Component pageComponent, String key, BackInfo backInfo) {
        session(pageComponent).setAttribute(key, backInfo);
    }

    public static BackInfo get(Component pageComponent, String key) {
        return (BackInfo) session(pageComponent).getAttribute(key);
    }
}
