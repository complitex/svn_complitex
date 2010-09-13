package org.complitex.osznconnection.file.service;

import org.apache.ibatis.session.ExecutorType;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.Tarif;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:16:17
 */
@Stateless(name = "TarifBean")
public class TarifBean extends AbstractBean{
    public static final String MAPPING_NAMESPACE = TarifBean.class.getName();

    @Transactional(executorType = ExecutorType.BATCH)
    public void insert(List<AbstractRequest> abstractRequests){
        for (AbstractRequest abstractRequest : abstractRequests){
            insert((Tarif) abstractRequest);
        }
    }

    @Transactional
    public void insert(Tarif tarif){
        sqlSession().insert(MAPPING_NAMESPACE + ".insertTarif", tarif);                
    }
}
