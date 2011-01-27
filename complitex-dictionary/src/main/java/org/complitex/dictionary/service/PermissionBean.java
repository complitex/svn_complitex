package org.complitex.dictionary.service;

import org.apache.ibatis.session.ExecutorType;
import org.complitex.dictionary.entity.Subject;
import org.complitex.dictionary.entity.Permission;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.*;

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

    @SuppressWarnings({"unchecked"})
    public List<Permission> getPermissions(Long permissionId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPermissionsById", permissionId);
    }

    public Long createPermission(String table, String entity, Long objectId){
        //Ищем из имеющихся ключей безопасности
        for (Permission permission : getPermissions(table, entity, objectId)){
            if (permission.getPermissions().size() == 0){
                return permission.getPermissionId();
            }
        }

        //Создаем новый ключ
        Long permissionId = sequenceBean.nextId(ENTITY_TABLE);

        sqlSession().insert(MAPPING_NAMESPACE + ".insertPermission",
                new Permission(permissionId, table, entity, objectId));

        return permissionId;
    }

    @Transactional
    public Long createPermission(String table, List<Subject> subjects){
        if (subjects == null || subjects.size() == 0){
            throw new IllegalArgumentException();
        }

        Subject first = subjects.get(0);

        //Одно разрешение
        if (subjects.size() == 1){
            return createPermission(table, first.getEntity(), first.getObjectId());
        }

        //Ищем из имеющихся ключей безопасности
        List<Subject> subList = subjects.subList(1, subjects.size());
        List<Permission> permissions = getPermissions(table, first.getEntity(), first.getObjectId());

        for (Permission p : permissions){ //находим все ключи по первому субъекту
            if (p.getPermissions().size() == subList.size()){ //если для ключа количество субъектов совпадает
                boolean foundAll = true;

                for (Permission owner : p.getPermissions()){ //проверяем что совпадают все субъекты
                    boolean found = false;

                    for (Subject subject : subList){
                        if (owner.getEntity().equals(subject.getEntity())
                                && owner.getObjectId().equals(subject.getObjectId())){
                            found = true;
                            break;
                        }
                    }

                    if (!found){
                        foundAll = false;
                        break;
                    }
                }

                if (foundAll){
                    return p.getPermissionId();
                }
            }
        }

        //Создаем новый ключ
        Long permissionId = sequenceBean.nextId(ENTITY_TABLE);

        for (Subject subject : subjects) {
            sqlSession().insert(MAPPING_NAMESPACE + ".insertPermission",
                    new Permission(permissionId, table, subject.getEntity(), subject.getObjectId()));
        }

        return permissionId;
    }
}
