package org.complitex.dictionary.service;

import javax.annotation.Resource;
import javax.ejb.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateful
public class SessionBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();

    public static final long ADMIN_ID = 1;

    @Resource
    private SessionContext sessionContext;

    public Long getCurrentUserId(){
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId", sessionContext.getCallerPrincipal().getName());
    }

    public Long getCurrentOrganizationId(){

        //todo implement user organization
        return 0L;
    }

}
