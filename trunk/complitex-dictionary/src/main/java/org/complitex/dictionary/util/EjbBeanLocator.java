/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Artem
 */
public class EjbBeanLocator {

    private final Logger log = LoggerFactory.getLogger(EjbBeanLocator.class);

    private EjbBeanLocator() {
    }

    public static <T> T getBean(String beanName) {
        return (T) getBean(beanName, false);
    }

    public static <T> T getBean(String beanName, boolean suppressException) {
        try {
            Context context = new InitialContext();
            return (T) context.lookup("java:module/" + beanName);
        } catch (NamingException e) {
            if (!suppressException) {
                LoggerFactory.getLogger(EjbBeanLocator.class).error("Couldn't get ejb bean by name " + beanName);
                throw new RuntimeException(e);
            } else {
                LoggerFactory.getLogger(EjbBeanLocator.class).info("Couldn't get ejb bean by name {}", beanName);
            }
        }
        return null;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return (T) getBean(beanClass.getSimpleName());
    }
}
