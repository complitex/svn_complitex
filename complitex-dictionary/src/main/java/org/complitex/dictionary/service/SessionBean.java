package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Permission;
import org.complitex.dictionary.entity.Subject;

import javax.annotation.Resource;
import javax.ejb.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateful
public class SessionBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();

    public static final long ADMIN_ID = 1;
    public static final String ADMIN_LOGIN = "admin";

    @Resource
    private SessionContext sessionContext;

    @EJB
    private PermissionBean permissionBean;

    public boolean isAdmin(){
        return ADMIN_LOGIN.equals(sessionContext.getCallerPrincipal().getName());
    }

    public Long getCurrentUserId(){
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId",
                sessionContext.getCallerPrincipal().getName());
    }

    @SuppressWarnings({"unchecked"})
    public List<Long> getUserOrganizationObjectIds(){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationObjectIds",
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

    public List<Subject> getCurrentSubjects(){
        List<Subject> subjects = new ArrayList<Subject>();

        //add organizations
        for (Long objectId : getUserOrganizationObjectIds()){
            subjects.add(new Subject("organization", objectId));
        }

        return subjects;
    }

    public String getPermissionString(String table){
        List<Long> permissions = getUserOrganizationPermissionIds(table);
        permissions.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);

        String s = "";
        String d = "";

        for (Long p : permissions){
            s += d + p;
            d = ", ";
        }

        return "(" + s + ")";
    }

    public Long createPermissionId(String table){
        return permissionBean.getPermission(table, getCurrentSubjects());
    }

}
