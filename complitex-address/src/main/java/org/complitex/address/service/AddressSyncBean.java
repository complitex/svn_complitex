package org.complitex.address.service;

import org.complitex.address.entity.AbstractAddressSync;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.AbstractBean;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.04.2014 23:45
 */
public class AddressSyncBean extends AbstractBean {
    public final static String NS = AddressSyncBean.class.getName();

    public <T extends AbstractAddressSync> void save(T addressSync){
        if (addressSync.getId() == null){
            sqlSession().insert(NS + ".insert" + addressSync.getClass().getSimpleName(), addressSync);
        }
    }

    public <T extends AbstractAddressSync> T getObject(Class<T> objectClass, Long id){
        return sqlSession().selectOne(NS + ".select" + objectClass.getSimpleName(), id);
    }

    public <T extends AbstractAddressSync> List<T> getList(Class<T> objectClass, FilterWrapper<T> filterWrapper){
        return sqlSession().selectList(NS + ".select" + objectClass.getSimpleName() + "List", filterWrapper);
    }

    public <T extends AbstractAddressSync> Long getCount(Class<T> objectClass, FilterWrapper<T> filterWrapper){
        return sqlSession().selectOne(NS + ".select"  + objectClass.getSimpleName() + "Count", filterWrapper);
    }

    public <T extends AbstractAddressSync> void delete(Class<T> objectClass, Long id){
        sqlSession().delete(NS + ".delete" + objectClass.getSimpleName(), id);
    }
}
