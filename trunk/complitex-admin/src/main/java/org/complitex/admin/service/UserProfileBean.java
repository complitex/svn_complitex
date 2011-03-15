package org.complitex.admin.service;

import org.complitex.admin.strategy.UserInfoStrategy;
import org.complitex.dictionary.service.IUserProfileBean;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.02.11 17:47
 */
@Stateless(name = "UserProfileBean")
public class UserProfileBean implements IUserProfileBean{
    private static final String USER_INFO_ENTITY = "user_info";

    @EJB
    private UserBean userBean;

    @EJB
    private UserInfoStrategy userInfoStrategy;

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    @Override
    public String getFullName(Long userId, Locale locale) {
        return userInfoStrategy.displayDomainObject(userBean.getUser(userId).getUserInfo(), locale);
    }
}
