package org.complitex.address.service;

import org.complitex.address.entity.AddressSync;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.AbstractBean;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.04.2014 23:45
 */
@Stateless
public class AddressSyncBean extends AbstractBean {
    public final static String NS = AddressSyncBean.class.getName();

    public void save(AddressSync addressSync){
        if (addressSync.getId() == null){
            sqlSession().insert("insertAddressSync", addressSync);
        }
    }

    public AddressSync getObject(Long id){
        return sqlSession().selectOne(NS + ".selectAddressSync", id);
    }

    public List<AddressSync> getList(FilterWrapper<AddressSync> filterWrapper){
        return sqlSession().selectList(NS + ".selectAddressSyncList", filterWrapper);
    }

    public Long getCount(FilterWrapper<AddressSync> filterWrapper){
        return sqlSession().selectOne(NS + ".selectAddressSyncCount", filterWrapper);
    }

    public boolean isExist(AddressSync addressSync){
        return getCount(FilterWrapper.of(addressSync)) == 0;
    }

    public void delete(Long id){
        sqlSession().delete(NS + ".deleteAddressSync", id);
    }
}
