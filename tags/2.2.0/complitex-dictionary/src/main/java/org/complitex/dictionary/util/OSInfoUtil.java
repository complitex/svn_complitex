/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author Artem
 */
public final class OSInfoUtil {

    public static final String WIN_LINE_SEPARATOR = "\r\n";
    public static final String UNIX_LINE_SEPARATOR = "\n";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String SYSTEM_LINE_SEPARATOR_PROPERTY = "line.separator";

    private OSInfoUtil() {
    }

    public static String lineSeparator(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        if (!Strings.isEmpty(userAgent)) {
            return userAgent.contains("Win") ? WIN_LINE_SEPARATOR : UNIX_LINE_SEPARATOR;
        }
        return null;
    }

    public static String lineSeparator(Request request) {
        return lineSeparator((HttpServletRequest) request.getContainerRequest());
    }

    public static String lineSeparator() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {

            @Override
            public String run() {
                return System.getProperty(SYSTEM_LINE_SEPARATOR_PROPERTY);
            }
        });
    }
}
