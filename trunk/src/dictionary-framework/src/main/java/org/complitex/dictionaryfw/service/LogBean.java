package org.complitex.dictionaryfw.service;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.complitex.dictionaryfw.entity.*;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.dictionaryfw.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.lang.model.type.ExecutableType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 17:50:26
 */
@Stateless(name = "LogBean")
public class LogBean extends AbstractBean{
    private static final Logger log = LoggerFactory.getLogger(LogBean.class);

    public static final String STATEMENT_PREFIX = LogBean.class.getCanonicalName();

    public static final int MAX_DESCRIPTION_LENGTH = 255;

    @Resource
    private SessionContext sessionContext;

    public void info(String module, Class controllerClass, Class modelClass, Long objectId, Log.EVENT event,
                     String descriptionPattern,  Object... descriptionArguments){

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() : null;

        log(module, controller, model, objectId, event, Log.STATUS.OK, null, descriptionPattern, descriptionArguments);
    }

    public void error(String module, Class controllerClass, Class modelClass, Long objectId, Log.EVENT event,
                      String descriptionPattern,  Object... descriptionArguments){

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() : null;

        log(module, controller, model, objectId, event, Log.STATUS.ERROR, null, descriptionPattern, descriptionArguments);
    }        

    public void info(String module, Class controllerClass, Class modelClass, String entityName, Long objectId,
                     Log.EVENT event, List<LogChange> changes, String descriptionPattern, Object... descriptionArguments){

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() + (entityName != null ? "#" + entityName : "") : null;

        log(module, controller, model, objectId, event, Log.STATUS.OK, changes, descriptionPattern, descriptionArguments);
    }

    public void log(Log.STATUS status, String module, Class controllerClass, Log.EVENT event,
                    Strategy strategy, DomainObject oldDomainObject, DomainObject newDomainObject,
                    Locale locale, String descriptionPattern, Object... descriptionArguments){

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = DomainObject.class.getName() + "#" + strategy.getEntityTable();

        log(module, controller, model, newDomainObject.getId(), event, status,
                getLogChanges(strategy, oldDomainObject, newDomainObject, locale),
                descriptionPattern, descriptionArguments);
    }

    public void error(String module, Class controllerClass, Class modelClass, String entityName, Long objectId,
                      Log.EVENT event, List<LogChange> changes, String descriptionPattern, Object... descriptionArguments){

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() + (entityName != null ? ":" + entityName : "") : null;

        log(module, controller, model, objectId, event, Log.STATUS.ERROR, changes, descriptionPattern, descriptionArguments);
    }

    private void log(String module, String controller, String model, Long objectId,
                     Log.EVENT event, Log.STATUS status, List<LogChange> logChanges,
                     String descriptionPattern,  Object... descriptionArguments){
        Log log = new Log();

        log.setDate(DateUtil.getCurrentDate());
        log.setLogin(sessionContext.getCallerPrincipal().getName());
        log.setModule(module);
        log.setController(controller);
        log.setModel(model);
        log.setObjectId(objectId);
        log.setEvent(event);
        log.setStatus(status);
        log.setLogChanges(logChanges);
        log.setDescription(descriptionPattern != null && descriptionArguments != null
                ? MessageFormat.format(descriptionPattern, descriptionArguments )
                : descriptionPattern);

        if (log.getDescription() != null && log.getDescription().length() > MAX_DESCRIPTION_LENGTH){
            log.setDescription(log.getDescription().substring(0, MAX_DESCRIPTION_LENGTH));            
        }

        //open new session
        SqlSession session = getSqlSessionManager().openSession();

        session.insert(STATEMENT_PREFIX + ".insertLog", log);

        if (log.getLogChanges() != null && !log.getLogChanges().isEmpty()){
            for (LogChange logChange : log.getLogChanges()){
                logChange.setLogId(log.getId());
            }

            session.insert(STATEMENT_PREFIX + ".insertLogChanges", log.getLogChanges());
        }

        try {
            session.commit();
            session.close();
        } catch (Exception e) {
            LogBean.log.error("Ошибка записи журнала событий в базу данных", e);
        }
    }

    public List<LogChange> getLogChanges(Strategy strategy, DomainObject oldDomainObject, DomainObject newDomainObject,
                                         Locale locale){
        List<LogChange> logChanges = new ArrayList<LogChange>();

        if (oldDomainObject == null){
            if (newDomainObject.getAttributes() != null) {
                for (Attribute na : newDomainObject.getAttributes()){
                    if (na.getLocalizedValues() != null) {
                        for (StringCulture ns : na.getLocalizedValues()){
                            if (ns.getValue() != null){
                                logChanges.add(new LogChange(na.getAttributeId(), null, strategy.getAttributeLabel(na, locale),
                                        null, ns.getValue(), ns.getLocale()));
                            }
                        }
                    }
                }
            }
        }else{
            for (Attribute oa : oldDomainObject.getAttributes()){
                for (Attribute na : newDomainObject.getAttributes()){
                    if (oa.getAttributeTypeId().equals(na.getAttributeTypeId())){
                        if (oa.getLocalizedValues() == null){
                            logChanges.add(new LogChange(na.getAttributeId(), null,
                                    strategy.getAttributeLabel(na, locale),
                                    StringUtil.valueOf(oa.getValueId()),
                                    StringUtil.valueOf(na.getValueId()),
                                    null));

                        }else{
                            for (StringCulture os : oa.getLocalizedValues()){
                                for (StringCulture ns : na.getLocalizedValues()){
                                    if (os.getLocale().equals(ns.getLocale())){
                                        if (!StringUtil.equal(os.getValue(), ns.getValue())){
                                            logChanges.add(new LogChange(na.getAttributeId(), null,
                                                    strategy.getAttributeLabel(na, locale),
                                                    os.getValue(),
                                                    ns.getValue(),
                                                    ns.getLocale()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return logChanges;
    }
}
