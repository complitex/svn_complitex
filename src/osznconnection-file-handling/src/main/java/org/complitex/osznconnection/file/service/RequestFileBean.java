package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.WrongFieldTypeException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import org.complitex.dictionaryfw.entity.DomainObject;

import static org.complitex.osznconnection.file.entity.RequestFile.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:15:53
 */
@Stateless(name = "RequestFileBean")
public class RequestFileBean extends AbstractBean {
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);
    private static final String MAPPING_NAMESPACE = RequestFileBean.class.getName();

    public RequestFile findById(long fileId) {
        return (RequestFile) sqlSession.selectOne(MAPPING_NAMESPACE + ".findById", fileId);
    }

    @SuppressWarnings({"unchecked"})
    public List<RequestFile> getRequestFiles(RequestFileFilter filter){
        return sqlSession.selectList(MAPPING_NAMESPACE + ".selectRequestFiles", filter);        
    }

    public int size(RequestFileFilter filter){
        return (Integer) sqlSession.selectOne(MAPPING_NAMESPACE + ".selectRequestFilesCount", filter);
    }

    public void save(RequestFile requestFile){
        if (requestFile.getId() == null){
            sqlSession.insert(MAPPING_NAMESPACE + ".insertRequestFile", requestFile);
        }else{
            sqlSession.insert(MAPPING_NAMESPACE + ".updateRequestFile", requestFile);
        }

    }
}
