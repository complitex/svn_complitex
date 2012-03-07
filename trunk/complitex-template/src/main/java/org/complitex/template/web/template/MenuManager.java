/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.template;

import javax.servlet.http.Cookie;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycle;

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
     * Main menu cookie's name const. This const must be the same as appropriate const in common.js.
     */
    public static final String MAIN_MENU_COOKIE = "MainMenuCookie";
    /**
     * Possible main menu cookie's values. Must be the same as appropriate const values in common.js.
     */
    private static final String MAIN_MENU_EXPANDED = "1";
    private static final String MAIN_MENU_COLLAPSED = "0";

    private MenuManager() {
    }

    public static void setMenuItem(String menuItem) {
        ((WebRequestCycle) RequestCycle.get()).getWebResponse().addCookie(new Cookie(SELECTED_MENU_ITEM_COOKIE, menuItem));
    }

    public static void hideMainMenu() {
        ((WebRequestCycle) RequestCycle.get()).getWebResponse().addCookie(new Cookie(MAIN_MENU_COOKIE, MAIN_MENU_COLLAPSED));
    }

    public static void openMainMenu() {
        ((WebRequestCycle) RequestCycle.get()).getWebResponse().addCookie(new Cookie(MAIN_MENU_COOKIE, MAIN_MENU_EXPANDED));
    }
}
