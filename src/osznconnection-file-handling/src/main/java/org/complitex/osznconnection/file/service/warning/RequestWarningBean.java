/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.warning;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestWarning;
import org.complitex.osznconnection.file.entity.RequestWarningParameter;
import org.complitex.osznconnection.file.entity.RequestWarningStatus;

/**
 *
 * @author Artem
 */
@Stateless
public class RequestWarningBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestWarningBean.class.getName();

    @Transactional
    public void save(RequestWarning requestWarning) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertWarning", requestWarning);
        for (RequestWarningParameter parameter : requestWarning.getParameters()) {
            parameter.setRequestWarningId(requestWarning.getId());
            sqlSession().insert(MAPPING_NAMESPACE + ".insertParameter", parameter);
        }
    }

    @Transactional
    public List<RequestWarning> getWarnings(long requestId, RequestFile.TYPE requestFileType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestId", requestId);
        params.put("requestFileType", requestFileType);
        List<RequestWarning> warnings = sqlSession().selectList(MAPPING_NAMESPACE + ".getWarnings", params);
        return warnings;
    }

    public void delete(long requestFileId, RequestFile.TYPE requestFileType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", requestFileId);
        params.put("requestFileType", requestFileType);
        params.put("requestTableName", requestFileType.name().toLowerCase());
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteParameters", params);
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteWarnings", params);
    }

    /**
     * Helper methods
     */
    @Transactional
    public void save(RequestFile.TYPE requestFileType, long requestId, RequestWarningStatus warningStatus, RequestWarningParameter... parameters) {
        RequestWarning warning = new RequestWarning(requestId, requestFileType, warningStatus);
        if (parameters != null) {
            warning.setParameters(Lists.newArrayList(parameters));
        }
        save(warning);
    }
}
