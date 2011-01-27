package org.complitex.dictionary.service;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateless
public class SessionBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();

    @Resource
    private SessionContext sessionContext;

    public Long getCurrentUserId(){
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId", sessionContext.getCallerPrincipal().getName());
    }


}
