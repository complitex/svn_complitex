package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileGroupFilter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.09.2010 14:32:05
 */
@Stateless(name = "RequestFileGroupBean")
public class RequestFileGroupBean extends AbstractBean{
    public static final String MAPPING_NAMESPACE = RequestFileGroupBean.class.getName();

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @SuppressWarnings({"unchecked"})
    public List<RequestFileGroup> getRequestFileGroups(RequestFileGroupFilter filter){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectRequestFilesGroups", filter);
    }

    public int getRequestFileGroupsCount(RequestFileGroupFilter filter){
        return (Integer)sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesGroupsCount", filter);
    }

    public void delete(RequestFileGroup requestFileGroup) {
        if (requestFileGroup.getBenefitFile() != null){
            requestFileBean.delete(requestFileGroup.getBenefitFile());
        }

        if (requestFileGroup.getPaymentFile() != null){
            requestFileBean.delete(requestFileGroup.getPaymentFile());
        }

        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFileGroup", requestFileGroup);
    }

    public void save(RequestFileGroup group){
        if (group.getId() == null){
            sqlSession().insert(MAPPING_NAMESPACE + ".insertRequestFileGroup", group);
        }else {
            sqlSession().update(MAPPING_NAMESPACE + ".updateRequestFileGroup", group);
        }
    }

    public void clearEmptyGroup(){
        sqlSession().delete(MAPPING_NAMESPACE + ".clearEmptyGroup");
    }

    public void updateStatus(final Benefit benefit, final RequestFileGroup.STATUS status){
        sqlSession().update(MAPPING_NAMESPACE + ".updateStatus", new HashMap<String,  Object>(){{
            put("requestFileId", benefit.getRequestFileId());
            put("status", status);
        }});
    }
}
