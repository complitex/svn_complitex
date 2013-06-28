package org.complitex.admin.service;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.complitex.admin.strategy.UserInfoStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.IUserProfileBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.service.exception.WrongCurrentPasswordException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.02.11 17:47
 */
@Stateless
public class UserProfileBean extends AbstractBean implements IUserProfileBean {

    public static final String NS = UserProfileBean.class.getName();
    @Resource
    private SessionContext sessionContext;
    @EJB
    private UserBean userBean;
    @EJB
    private UserInfoStrategy userInfoStrategy;
    @EJB
    private SessionBean sessionBean;

    @Override
    public String getFullName(Long userId, Locale locale) {
        DomainObject userInfo = userBean.getUser(userId, false).getUserInfo();
        return userInfo != null ? userInfoStrategy.displayDomainObject(userInfo, locale) : "";
    }

    @Transactional
    public void updatePassword(String currentPassword, final String password) throws WrongCurrentPasswordException {
        final String login = sessionContext.getCallerPrincipal().getName();

        String currentPasswordMD5 = (String) sqlSession().selectOne(NS + ".selectPassword", login);

        if (!currentPasswordMD5.equals(DigestUtils.md5Hex(currentPassword))) {
            throw new WrongCurrentPasswordException();
        }

        final String md5Password = DigestUtils.md5Hex(password);
        sqlSession().update(NS + ".updatePassword", ImmutableMap.of("login", login, "password", md5Password));
    }

    @Transactional
    public void updateMainUserOrganization(long mainUserOrganizationId) {
        long userId = sessionBean.getCurrentUserId();
        sqlSession().update(NS + ".clearMainOrganizationStatus", userId);
        sqlSession().update(NS + ".updateMainUserOrganization",
                ImmutableMap.of("userId", userId, "mainUserOrganizationId", mainUserOrganizationId));
    }
}
