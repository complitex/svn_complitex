package org.complitex.osznconnection.logging.service;

import org.complitex.dictionaryfw.entity.Log;
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
    public List<Log> getLogs(LogFilter filter){
        return sqlSession.selectList(STATEMENT_PREFIX + ".selectLogs", filter);
    }

    public int getLogsCount(LogFilter filter){
        return (Integer) sqlSession.selectOne(STATEMENT_PREFIX + ".selectLogsCount", filter);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getModules(){
        return sqlSession.selectList(STATEMENT_PREFIX + ".selectModules");
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getControllers(){
        return sqlSession.selectList(STATEMENT_PREFIX + ".selectControllers");
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getModels(){
        return sqlSession.selectList(STATEMENT_PREFIX + ".selectModels");
    }
}
