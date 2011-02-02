package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Subject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import javax.annotation.Resource;
import javax.ejb.*;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateful(name = "SessionBean")
public class SessionBean extends AbstractBean {
    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();

    private static final String ORGANIZATION_ENTITY = "organization";

    public static final String ADMIN_LOGIN = "admin";

    @Resource
    private SessionContext sessionContext;

    @EJB
    private PermissionBean permissionBean;

    @EJB
    private IUserProfileBean userProfileBean;

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    public boolean isAdmin() {
        return ADMIN_LOGIN.equals(sessionContext.getCallerPrincipal().getName());
    }

    public Long getCurrentUserId() {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId",
                sessionContext.getCallerPrincipal().getName());
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getUserOrganizationObjectIds() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationObjectIds",
                sessionContext.getCallerPrincipal().getName());
    }

    @SuppressWarnings({"unchecked"})
    public Long getMainUserOrganizationObjectId() {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectMainOrganizationObjectId",
                sessionContext.getCallerPrincipal().getName());
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getUserOrganizationPermissionIds(final String table){
        Map<String, String> parameter = new HashMap<String, String>(){{
                put("table", table);
                put("login", sessionContext.getCallerPrincipal().getName());
        }};

        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationPermissionIds", parameter);
    }

    public List<Subject> getCurrentSubjects() {
        List<Subject> subjects = new ArrayList<Subject>();

        //add organizations
        for (Long objectId : getUserOrganizationObjectIds()) {
            subjects.add(new Subject(ORGANIZATION_ENTITY, objectId));
        }

        return subjects;
    }

    public String getPermissionString(String table) {
        List<Long> permissions = getUserOrganizationPermissionIds(table);
        permissions.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);

        String s = "";
        String d = "";

        for (Long p : permissions) {
            s += d + p;
            d = ", ";
        }

        return "(" + s + ")";
    }

    public Long createPermissionId(String table) {
        return permissionBean.getPermission(table, getCurrentSubjects());
    }

    public String getCurrentUserFullName(Locale locale){
        return userProfileBean.getFullName(getCurrentUserId(), locale);
    }

    public String getMainUserOrganizationName(Locale locale){
        IStrategy strategy = strategyFactory.getStrategy(ORGANIZATION_ENTITY);

        Long oId = getMainUserOrganizationObjectId();

        return oId != null ? strategy.displayDomainObject(strategy.findById(oId), locale) : "";
    }
}
