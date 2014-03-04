package org.complitex.dictionary.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.complitex.dictionary.entity.Permission;
import org.complitex.dictionary.entity.Subject;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.01.11 16:11
 */
@Stateless
public class PermissionBean extends AbstractBean{
    private static final String MAPPING_NAMESPACE = PermissionBean.class.getName();
    private static final String ENTITY_TABLE = "permission";

    public static final Long VISIBLE_BY_ALL_PERMISSION_ID = 0L;
    
    @EJB(beanName = "SequenceBean")
    private SequenceBean sequenceBean;

    public List<Permission> findPermissions(String table, Subject subject){
        return findPermissions(table, subject.getEntity(), subject.getObjectId());
    }

    @SuppressWarnings({"unchecked"})
    public List<Permission> findPermissions(final String table, final String entity, final Long objectId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPermissions",
                new HashMap<String, Object>(){{
                    put("table", table);
                    put("entity", entity);
                    put("objectId", objectId);
                }});
    }

    @SuppressWarnings({"unchecked"})
    public List<Permission> findPermissions(Long permissionId){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPermissionsById", permissionId);
    }

    public Set<Long> findSubjectIds(Long permissionId) {
        Set<Long> subjectIds = Sets.newHashSet();
        for (Permission permission : findPermissions(permissionId)) {
            subjectIds.add(permission.getObjectId());
        }
        return subjectIds;
    }
    
     @SuppressWarnings({"unchecked"})
    public List<Long> findPermissionIds(final String table, final String entity, final Long objectId){
        return  sqlSession().selectList(MAPPING_NAMESPACE + ".selectPermissionIds",
                new HashMap<String, Object>(){{
                    put("table", table);
                    put("entity", entity);
                    put("objectId", objectId);
                }});
    }

    public Long getPermission(String table, Subject subject){
        return getPermission(table, subject.getEntity(), subject.getObjectId());
    }

    public Long getPermission(String table, String entity, Long objectId){
        //Ищем из имеющихся ключей безопасности
        for (Permission permission : findPermissions(table, entity, objectId)){
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
    public Long getPermission(String table, List<Subject> subjects){
        if (subjects == null || subjects.size() == 0){
            return null;
        }

        Subject first = subjects.get(0);

        //Одно разрешение
        if (subjects.size() == 1){
            return getPermission(table, first.getEntity(), first.getObjectId());
        }

        //Ищем из имеющихся ключей безопасности
        List<Subject> subList = subjects.subList(1, subjects.size());
        List<Permission> permissions = findPermissions(table, first.getEntity(), first.getObjectId());

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

    @Transactional
    public boolean isOrganizationPermissionExists(String organizationTable, long organizationId){
        Map<String, Object> params = Maps.newHashMap();
        params.put("organizationTable", organizationTable);
        params.put("organizationId", organizationId);
        return sqlSession().selectOne(MAPPING_NAMESPACE+".organizationPermissionExist", params) != null;
    }

    @Override
    public void setSqlSessionFactoryBean(SqlSessionFactoryBean sqlSessionFactoryBean) {
        super.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        sequenceBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
    }
}
