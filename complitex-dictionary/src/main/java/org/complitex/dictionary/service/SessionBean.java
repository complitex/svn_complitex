package org.complitex.dictionary.service;

import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.*;
import java.util.*;
import org.apache.wicket.util.string.Strings;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateless
@DeclareRoles(SessionBean.CHILD_ORGANIZATION_VIEW_ROLE)
public class SessionBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();
    private static final String ORGANIZATION_ENTITY = "organization";
    public static final String CHILD_ORGANIZATION_VIEW_ROLE = "CHILD_ORGANIZATION_VIEW";
    public static final String ADMIN_LOGIN = "admin";
    @Resource
    private SessionContext sessionContext;
    @EJB
    private IUserProfileBean userProfileBean;
    @EJB
    private StrategyFactory strategyFactory;

    public boolean isAdmin() {
        return ADMIN_LOGIN.equals(sessionContext.getCallerPrincipal().getName());
    }

    public Long getCurrentUserId() {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId", getCurrentUserLogin());
    }

    public String getCurrentUserLogin() {
        return sessionContext.getCallerPrincipal().getName();
    }

    private List<Long> getUserOrganizationObjectIds() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationObjectIds", getCurrentUserLogin());
    }

    private List<Long> getOrganizationChildrenObjectId(Long parentObjectId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationChildrenObjectIds", parentObjectId);
    }

    private List<Long> getUserOrganizationTreeObjectIds() {
        List<Long> objectIds = new ArrayList<Long>();

        for (Long objectId : getUserOrganizationObjectIds()) {
            addChildOrganizations(objectIds, objectId);
        }

        return objectIds;
    }

    private void addChildOrganizations(List<Long> objectIds, Long objectId) {
        objectIds.add(objectId);

        for (Long id : getOrganizationChildrenObjectId(objectId)) {
            if (!objectIds.contains(id)) {
                addChildOrganizations(objectIds, id);
            }
        }
    }

    private List<Long> getUserOrganizationTreePermissionIds(String table) {
        String s = "";
        String d = "";

        for (Long p : getUserOrganizationTreeObjectIds()) {
            s += d + p;
            d = ", ";
        }

        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("table", table);
        parameter.put("organizations", !Strings.isEmpty(s) ? "(" + s + ")" : null);

        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationTreePermissionIds", parameter);
    }

    private Long getMainUserOrganizationObjectId() {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectMainOrganizationObjectId", getCurrentUserLogin());
    }

    private List<Long> getUserOrganizationPermissionIds(final String table) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationPermissionIds",
                new HashMap<String, String>() {

                    {
                        put("table", table);
                        put("login", getCurrentUserLogin());
                    }
                });
    }

    public String getPermissionString(String table) {
        List<Long> permissions = sessionContext.isCallerInRole(CHILD_ORGANIZATION_VIEW_ROLE)
                ? getUserOrganizationTreePermissionIds(table)
                : getUserOrganizationPermissionIds(table);
        permissions.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);

        String s = "";
        String d = "";

        for (Long p : permissions) {
            s += d + p;
            d = ", ";
        }

        return "(" + s + ")";
    }

    public String getCurrentUserFullName(Locale locale) {
        return userProfileBean.getFullName(getCurrentUserId(), locale);
    }

    public String getMainUserOrganizationName(Locale locale) {
        try {
            IStrategy strategy = strategyFactory.getStrategy(ORGANIZATION_ENTITY);
            Long oId = getMainUserOrganizationObjectId();
            return oId != null ? strategy.displayDomainObject(strategy.findById(oId, false), locale) : "";
        } catch (Exception e) {
            return "[NA]";
        }
    }
}
