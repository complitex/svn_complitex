package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Subject;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.*;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateless(name = "SessionBean")
@DeclareRoles("CHILD_ORGANIZATION_VIEW")
public class SessionBean extends AbstractBean {
    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();

    private static final String ORGANIZATION_ENTITY = "organization";
    private static final String CHILD_ORGANIZATION_VIEW_ROLE = "CHILD_ORGANIZATION_VIEW";

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
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId", getCurrentUserLogin());
    }

    public String getCurrentUserLogin(){
        return sessionContext.getCallerPrincipal().getName();
    }

    public boolean hasRole(String role){
        return sessionContext.isCallerInRole(role);
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getUserOrganizationObjectIds() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationObjectIds", getCurrentUserLogin());
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getOrganizationChildrenObjectId(Long parentObjectId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationChildrenObjectIds", parentObjectId);
    }

    public List<Long> getUserOrganizationTreeObjectIds(){
        List<Long> objectIds = new ArrayList<Long>();

        for (Long objectId : getUserOrganizationObjectIds()){
            addChildOrganizations(objectIds, objectId);
        }

        return objectIds;
    }

    private void addChildOrganizations(List<Long> objectIds, Long objectId){
        objectIds.add(objectId);

        for (Long id : getOrganizationChildrenObjectId(objectId)){
            if (!objectIds.contains(id)){
                addChildOrganizations(objectIds, id);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getUserOrganizationTreePermissionIds(String table){
        String s = "";
        String d = "";

        for (Long p : getUserOrganizationTreeObjectIds()) {
            s += d + p;
            d = ", ";
        }

        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("table", table);
        parameter.put("organizations", "(" + s + ")");

        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationTreePermissionIds", parameter);
    }

    @SuppressWarnings({"unchecked"})
    public Long getMainUserOrganizationObjectId() {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectMainOrganizationObjectId", getCurrentUserLogin());
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getUserOrganizationPermissionIds(final String table){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationPermissionIds",
                new HashMap<String, String>(){{
                    put("table", table);
                    put("login", getCurrentUserLogin());
                }});
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
        List<Long> permissions = hasRole(CHILD_ORGANIZATION_VIEW_ROLE)
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

    public String getOrganizationString(){
        List<Long> organizationObjectIds = hasRole(CHILD_ORGANIZATION_VIEW_ROLE)
                ? getUserOrganizationTreeObjectIds()
                : getUserOrganizationObjectIds();

        String s = "";
        String d = "";

        for (Long o : organizationObjectIds) {
            s += d + o;
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

    public boolean hasPermission(final Long permissionId){
        if (PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID.equals(permissionId) || isAdmin()){
            return true;
        }

        return (Boolean) sqlSession().selectOne(MAPPING_NAMESPACE + ".hasPermission",
                new HashMap<String, Object>(){{
                    put("permissionId", permissionId);
                    put("organizations", getOrganizationString());
                }});
    }
}
