/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class EjbBeanLocator {

    private static final Logger log = LoggerFactory.getLogger(EjbBeanLocator.class);

    public static <T> T getBean(String beanName) {
        try {
            Context context = new InitialContext();
            return (T) context.lookup("java:module/" + beanName);
        } catch (NamingException e) {
            log.error("Couldn't get ejb bean by name " + beanName);
            throw new RuntimeException(e);
        }
    }

    public static <T> T getBean(Class<T> beanClass) {
        return (T)getBean(beanClass.getSimpleName());
    }
}
