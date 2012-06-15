package org.complitex.dictionary.service;

import org.complitex.dictionary.service.exception.WrongCurrentPasswordException;

import java.util.Locale;
import org.complitex.dictionary.mybatis.Transactional;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.02.11 17:31
 */
public interface IUserProfileBean {

    String getFullName(Long userId, Locale locale);

    @Transactional
    void updatePassword(String currentPassword, final String password) throws WrongCurrentPasswordException;

    @Transactional
    void updateMainUserOrganization(long mainUserOrganizationId);
}
