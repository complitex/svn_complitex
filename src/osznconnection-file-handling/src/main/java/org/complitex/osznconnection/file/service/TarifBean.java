package org.complitex.osznconnection.file.service;

import org.apache.ibatis.session.ExecutorType;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Tarif;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:16:17
 */
@Stateless
public class TarifBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = TarifBean.class.getName();

    @Transactional(executorType = ExecutorType.BATCH)
    public void insert(List<AbstractRequest> abstractRequests) {
        for (AbstractRequest abstractRequest : abstractRequests) {
            insert((Tarif) abstractRequest);
        }
    }

    @Transactional
    public void insert(Tarif tarif) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertTarif", tarif);
    }

    @Transactional
    public void delete(RequestFile requestFile) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteTarifs", requestFile.getId());
    }

    @Transactional
    public Integer getCODE2_1(Double T11_CS_UNI) {
        List<Integer> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".getCODE2_1", T11_CS_UNI);
        if (codes != null && (codes.size() == 1)) {
            return codes.get(0);
        }
        return null;
    }
}
