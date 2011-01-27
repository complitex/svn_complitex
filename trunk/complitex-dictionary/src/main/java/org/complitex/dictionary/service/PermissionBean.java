package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Permission;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.01.11 16:11
 */
@Stateless(name = "PermissionBean")
public class PermissionBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = PermissionBean.class.getName();
    private static final String ENTITY_TABLE = "permission";

    @EJB(beanName = "SequenceBean")
    private SequenceBean sequenceBean;

    @SuppressWarnings({"unchecked"})
    public List<Permission> getPermissions(final String table, final String entity, final Long objectId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPermissions",
                new HashMap<String, Object>(){{
                    put("table", table);
                    put("entity", entity);
                    put("objectId", objectId);
                }});
    }

    public Permission createPermission(String table, String entity, Long objectId){
        Permission permission = new Permission();

        permission.setTable(table);
        permission.setEntity(entity);
        permission.setObjectId(objectId);

        Long permissionId = sequenceBean.nextId(ENTITY_TABLE);
        permission.setPermissionId(permissionId);

        sqlSession().insert(MAPPING_NAMESPACE + ".insertPermission", permission);

        return permission;
    }
}
