package org.complitex.osznconnection.file.service;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 12:11:15
 */
public class ConfigStatic {
    public static ConfigBean get(){
        try {
            InitialContext context = new InitialContext();
            return (ConfigBean) context.lookup("java:module/ConfigBean");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
