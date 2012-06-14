/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.template;

import javax.servlet.http.Cookie;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

/**
 * Manages template menu items from server side.
 * 
 * @see common.js
 * @author Artem
 */
public final class MenuManager {

    /**
     * Selected menu item cookie's name const. This const must be the same as appropriate const in common.js.
     */
    public static final String SELECTED_MENU_ITEM_COOKIE = "SelectedMenuItem";
    /**
     * Value of selected menu item's cookie when there are no item selected.
     */
    private static final String UNDEFINED_MENU_ITEM = "UNDEFINED";
    /**
     * Main menu (info left panel) cookie's name const. This const must be the same as appropriate const in common.js.
     */
    public static final String MAIN_MENU_COOKIE = "MainMenuCookie";
    /**
     * Possible main menu cookie's values. Must be the same as appropriate const values in common.js.
     */
    private static final String MAIN_MENU_EXPANDED = "1";
    private static final String MAIN_MENU_COLLAPSED = "0";

    private MenuManager() {
    }

    private static Cookie newCookie(String key, String value, String contexPath) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath(contexPath + "/");
        return cookie;
    }

    private static void setCookie(String key, String value) {
        RequestCycle requestCycle = RequestCycle.get();
        WebRequest request = (WebRequest) requestCycle.getRequest();
        String contextPath = request.getContextPath();

        Cookie cookie = request.getCookie(key);
        if (cookie == null) {
            cookie = newCookie(key, value, contextPath);
        } else {
            cookie.setValue(value);
            cookie.setPath(contextPath + "/");
        }
        ((WebResponse) requestCycle.getResponse()).addCookie(cookie);
    }

    public static void setMenuItem(String menuItem) {
        setCookie(SELECTED_MENU_ITEM_COOKIE, menuItem);
    }

    public static void hideMainMenu() {
        setCookie(MAIN_MENU_COOKIE, MAIN_MENU_COLLAPSED);
    }

    public static void openMainMenu() {
        setCookie(MAIN_MENU_COOKIE, MAIN_MENU_EXPANDED);
    }

    public static void removeMenuItem() {
        setCookie(SELECTED_MENU_ITEM_COOKIE, UNDEFINED_MENU_ITEM);
    }
}
