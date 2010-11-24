package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.StatusDetail;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.11.10 16:51
 */
@Stateless(name = "StatusDetailBean")
public class StatusDetailBean extends AbstractBean{
    public static final String MAPPING_NAMESPACE = StatusDetailBean.class.getName();

    @SuppressWarnings({"unchecked"})
    public List<StatusDetail> getPaymentStatusDetails(RequestFile requestFile){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectPaymentRootStatusDetail", requestFile);
    }

}
