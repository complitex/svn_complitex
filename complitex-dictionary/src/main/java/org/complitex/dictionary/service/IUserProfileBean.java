package org.complitex.dictionary.service;

import javax.ejb.Local;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.02.11 17:31
 */
public interface IUserProfileBean {
    String getFullName(Long userId, Locale locale);
}
