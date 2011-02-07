package org.complitex.dictionary.service;

import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import org.complitex.dictionary.strategy.IStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 17:50:26
 */
@Stateless(name = "LogBean")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class LogBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(LogBean.class);

    public static final String STATEMENT_PREFIX = LogBean.class.getCanonicalName();

    public static final int MAX_DESCRIPTION_LENGTH = 255;

    @EJB
    private LocaleBean localeBean;

    @Resource
    private SessionContext sessionContext;

    public void info(String module, Class controllerClass, Class modelClass, Long objectId, Log.EVENT event,
            String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() : null;

        log(module, controller, model, objectId, event, Log.STATUS.OK, null, descriptionPattern, descriptionArguments);
    }

    public void error(String module, Class controllerClass, Class modelClass, Long objectId, Log.EVENT event,
            String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() : null;

        log(module, controller, model, objectId, event, Log.STATUS.ERROR, null, descriptionPattern, descriptionArguments);
    }

    public void warn(String module, Class controllerClass, Class modelClass, Long objectId, Log.EVENT event,
            String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() : null;

        log(module, controller, model, objectId, event, Log.STATUS.WARN, null, descriptionPattern, descriptionArguments);
    }

    public void info(String module, Class controllerClass, Class modelClass, String entityName, Long objectId,
            Log.EVENT event, List<LogChange> changes, String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() + (entityName != null ? "#" + entityName : "") : null;

        log(module, controller, model, objectId, event, Log.STATUS.OK, changes, descriptionPattern, descriptionArguments);
    }

    public void log(Log.STATUS status, String module, Class controllerClass, Log.EVENT event,
            IStrategy strategy, DomainObject oldDomainObject, DomainObject newDomainObject,
            Locale locale, String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = DomainObject.class.getName() + "#" + strategy.getEntityTable();

        log(module, controller, model, newDomainObject.getId(), event, status,
                getLogChanges(strategy, oldDomainObject, newDomainObject, locale),
                descriptionPattern, descriptionArguments);
    }

    public void logPermissionChange(Log.STATUS status, String entity, Long objectId, String descriptionPattern, Object... descriptionArguments) {
        String controller = "changeChildrenPermissionProcess";
        String model = DomainObject.class.getName() + "#" + entity;
        String module = org.complitex.dictionary.Module.NAME;
        log(module, controller, model, objectId, Log.EVENT.SETTING_PERMISSION, status, null, descriptionPattern, descriptionArguments);
    }

    public void logChangeSubjectsAcrossTree(Log.STATUS status, String entity, Long objectId, String descriptionPattern, Object... descriptionArguments) {
        String controller = "changeSubjectsAcrossTreeProcess";
        String model = DomainObject.class.getName() + "#" + entity;
        String module = org.complitex.dictionary.Module.NAME;
        log(module, controller, model, objectId, Log.EVENT.SETTING_PERMISSION, status, null, descriptionPattern, descriptionArguments);
    }

    public void error(String module, Class controllerClass, Class modelClass, String entityName, Long objectId,
            Log.EVENT event, List<LogChange> changes, String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() + (entityName != null ? ":" + entityName : "") : null;

        log(module, controller, model, objectId, event, Log.STATUS.ERROR, changes, descriptionPattern, descriptionArguments);
    }

    private void log(String module, String controller, String model, Long objectId,
            Log.EVENT event, Log.STATUS status, List<LogChange> logChanges,
            String descriptionPattern, Object... descriptionArguments) {
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
                ? MessageFormat.format(descriptionPattern, descriptionArguments)
                : descriptionPattern);

        if (log.getDescription() != null && log.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.setDescription(log.getDescription().substring(0, MAX_DESCRIPTION_LENGTH));
        }

        //open new session
        SqlSession session = getSqlSessionManager().openSession();

        session.insert(STATEMENT_PREFIX + ".insertLog", log);

        if (log.getLogChanges() != null && !log.getLogChanges().isEmpty()) {
            for (LogChange logChange : log.getLogChanges()) {
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

    public List<LogChange> getLogChanges(IStrategy strategy, DomainObject oldDomainObject, DomainObject newDomainObject, Locale locale) {
        List<LogChange> logChanges = new ArrayList<LogChange>();

        if (oldDomainObject == null) {
            if (newDomainObject.getAttributes() != null) {
                for (Attribute na : newDomainObject.getAttributes()) {
                    if (na.getLocalizedValues() != null) {
                        for (StringCulture ns : na.getLocalizedValues()) {
                            if (ns.getValue() != null) {
                                logChanges.add(new LogChange(na.getAttributeId(), null, strategy.getAttributeLabel(na, locale),
                                        null, ns.getValue(), localeBean.getLocale(ns.getLocaleId()).getLanguage()));
                            }
                        }
                    }
                }
            }
        } else {
            for (Attribute oa : oldDomainObject.getAttributes()) {
                for (Attribute na : newDomainObject.getAttributes()) {
                    if (oa.getAttributeTypeId().equals(na.getAttributeTypeId())) {
                        if (oa.getLocalizedValues() == null) {
                            logChanges.add(new LogChange(na.getAttributeId(), null,
                                    strategy.getAttributeLabel(na, locale),
                                    StringUtil.valueOf(oa.getValueId()),
                                    StringUtil.valueOf(na.getValueId()),
                                    null));

                        } else {
                            for (StringCulture os : oa.getLocalizedValues()) {
                                if (na.getLocalizedValues() != null) {
                                    for (StringCulture ns : na.getLocalizedValues()) {
                                        if (os.getLocaleId().equals(ns.getLocaleId())) {
                                            if (!StringUtil.equal(os.getValue(), ns.getValue())) {
                                                logChanges.add(new LogChange(na.getAttributeId(), null,
                                                        strategy.getAttributeLabel(na, locale),
                                                        os.getValue(),
                                                        ns.getValue(),
                                                        localeBean.getLocale(ns.getLocaleId()).getLanguage()));
                                            }
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
