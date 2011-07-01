package org.complitex.admin.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.complitex.admin.strategy.UserInfoStrategy;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.IUserProfileBean;
import org.complitex.dictionary.service.exception.WrongCurrentPasswordException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.02.11 17:47
 */
@Stateless(name = "UserProfileBean")
public class UserProfileBean extends AbstractBean implements IUserProfileBean{
    public static final String NS = UserProfileBean.class.getName();

    @Resource
    private SessionContext sessionContext;

    @EJB
    private UserBean userBean;

    @EJB
    private UserInfoStrategy userInfoStrategy;

    @Override
    public String getFullName(Long userId, Locale locale) {
        return userInfoStrategy.displayDomainObject(userBean.getUser(userId).getUserInfo(), locale);
    }

    public void updatePassword(String currentPassword, final String password) throws WrongCurrentPasswordException{
        final String login = sessionContext.getCallerPrincipal().getName();

        String currentPasswordMD5 = (String) sqlSession().selectOne(NS + ".selectPassword", login);

        if (!currentPasswordMD5.equals(DigestUtils.md5Hex(currentPassword))){
            throw new WrongCurrentPasswordException();
        }

        sqlSession().update(NS + ".updatePassword", new HashMap<String, String>(){{
            put("login", login);
            put("password", DigestUtils.md5Hex(password));
        }});
    }

}
