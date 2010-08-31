package org.complitex.osznconnection.logging.service;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 19.08.2010 13:07:35
 */
@Stateless(name = "LogListBean")
public class LogListBean extends AbstractBean {
    public static final String STATEMENT_PREFIX = LogListBean.class.getCanonicalName();

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<Log> getLogs(LogFilter filter){
        return sqlSession().selectList(STATEMENT_PREFIX + ".selectLogs", filter);
    }

    @Transactional
    public int getLogsCount(LogFilter filter){
        return (Integer) sqlSession().selectOne(STATEMENT_PREFIX + ".selectLogsCount", filter);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<String> getModules(){
        return sqlSession().selectList(STATEMENT_PREFIX + ".selectModules");
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<String> getControllers(){
        return sqlSession().selectList(STATEMENT_PREFIX + ".selectControllers");
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<String> getModels(){
        return sqlSession().selectList(STATEMENT_PREFIX + ".selectModels");
    }
}
