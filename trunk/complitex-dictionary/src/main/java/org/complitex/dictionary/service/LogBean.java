package org.complitex.dictionary.service;

import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.util.DateUtil;
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
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.util.Numbers;
import org.complitex.dictionary.util.StringUtil;

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

    @EJB
    private StringCultureBean stringBean;
    
    /**
     * Records user log in action. Should be invoked from within user log in code. 
     * Due to glassfish bug: {@link SessionContext#getCallerPrincipal() } always returns null from within web listeners, 
     * logIn() method takes user login as argument.
     * @param userLogin user login
     * @param module
     * @param controllerClass
     * @param descriptionPattern
     * @param descriptionArguments 
     */
    public void logIn(String userLogin, String module, Class controllerClass, String descriptionPattern, 
            Object... descriptionArguments){
        log(userLogin, module, controllerClass != null ? controllerClass.getName() : null, null, null, Log.EVENT.USER_LOGIN, 
                Log.STATUS.OK, null, descriptionPattern, descriptionArguments);
    }
    
    /**
     * Records user log out action. Should be invoked from within user log out code. 
     * Due to glassfish bug: {@link SessionContext#getCallerPrincipal() } always returns null from within web listeners, 
     * logOut() method takes user login as argument.
     * @param userLogin user login
     * @param module
     * @param controllerClass
     * @param descriptionPattern
     * @param descriptionArguments 
     */
    public void logOut(String userLogin, String module, Class controllerClass, String descriptionPattern, 
            Object... descriptionArguments){
        log(userLogin, module, controllerClass != null ? controllerClass.getName() : null, null, null, Log.EVENT.USER_LOGOFF, 
                Log.STATUS.OK, null, descriptionPattern, descriptionArguments);
    }

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
            String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = DomainObject.class.getName() + "#" + strategy.getEntityTable();

        log(module, controller, model, newDomainObject.getId(), event, status,
                getLogChanges(strategy, oldDomainObject, newDomainObject),
                descriptionPattern, descriptionArguments);
    }

    public void logArchivation(Log.STATUS status, String module, Class controllerClass, String entityTable, long objectId,
            String descriptionPattern, Object... descriptionArguments){
        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = DomainObject.class.getName() + "#" + entityTable;
        log(module, controller, model, objectId, Log.EVENT.ARCHIVE, status, null, descriptionPattern, descriptionArguments);
    }

    public void logReplacePermissions(Log.STATUS status, String entity, Long objectId, String descriptionPattern, Object... descriptionArguments) {
        String controller = "replacePermissions";
        String model = DomainObject.class.getName() + "#" + entity;
        String module = org.complitex.dictionary.Module.NAME;
        log(module, controller, model, objectId, Log.EVENT.SETTING_PERMISSION, status, null, descriptionPattern, descriptionArguments);
    }

    public void logChangePermissions(Log.STATUS status, String entity, Long objectId, String descriptionPattern, Object... descriptionArguments) {
        String controller = "changePermissions";
        String model = DomainObject.class.getName() + "#" + entity;
        String module = org.complitex.dictionary.Module.NAME;
        log(module, controller, model, objectId, Log.EVENT.SETTING_PERMISSION, status, null, descriptionPattern, descriptionArguments);
    }

    public void logChangeActivity(Log.STATUS status, String entity, Long objectId, boolean enable, String descriptionPattern, Object... descriptionArguments){
        String controller = "changeActivity";
        String model = DomainObject.class.getName() + "#" + entity;
        String module = org.complitex.dictionary.Module.NAME;
        log(module, controller, model, objectId, enable ? Log.EVENT.ENABLE : Log.EVENT.DISABLE, status, null, descriptionPattern, descriptionArguments);
    }

    public void error(String module, Class controllerClass, Class modelClass, String entityName, Long objectId,
            Log.EVENT event, List<LogChange> changes, String descriptionPattern, Object... descriptionArguments) {

        String controller = controllerClass != null ? controllerClass.getName() : null;
        String model = modelClass != null ? modelClass.getName() + (entityName != null ? ":" + entityName : "") : null;

        log(module, controller, model, objectId, event, Log.STATUS.ERROR, changes, descriptionPattern, descriptionArguments);
    }
    
    private void log(String module, String controller, String model, Long objectId, Log.EVENT event, Log.STATUS status, 
            List<LogChange> logChanges, String descriptionPattern, Object... descriptionArguments){
        log(sessionContext.getCallerPrincipal().getName(), module, controller, model, objectId, event, status, 
                logChanges, descriptionPattern, descriptionArguments);
    }

    private void log(String login, String module, String controller, String model, Long objectId, Log.EVENT event, Log.STATUS status, 
            List<LogChange> logChanges, String descriptionPattern, Object... descriptionArguments) {
        Log log = new Log();

        log.setDate(DateUtil.getCurrentDate());
        log.setLogin(login);
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

    public List<LogChange> getLogChanges(IStrategy strategy, DomainObject oldDomainObject, DomainObject newDomainObject) {
        final Locale systemLocale = localeBean.getSystemLocale();
        List<LogChange> logChanges = new ArrayList<LogChange>();

        if (oldDomainObject == null) {
            for (Attribute na : newDomainObject.getAttributes()) {
                EntityAttributeType attributeType = strategy.getEntity().getAttributeType(na.getAttributeTypeId());
                String attributeValueType = attributeType.getAttributeValueType(na.getValueTypeId()).getValueType();

                if (SimpleTypes.isSimpleType(attributeValueType)) {
                    if (SimpleTypes.STRING_CULTURE.name().equals(attributeValueType.toUpperCase())) {
                        for (StringCulture newString : na.getLocalizedValues()) {
                            if (!Strings.isEqual(newString.getValue(), null)) {
                                logChanges.add(new LogChange(na.getAttributeId(), null,
                                        strategy.getAttributeLabel(na, systemLocale), null, newString.getValue(),
                                        localeBean.getLocaleObject(newString.getLocaleId()).getLanguage()));
                            }
                        }
                    } else {
                        logChanges.add(new LogChange(na.getAttributeId(), null,
                                strategy.getAttributeLabel(na, systemLocale), null,
                                stringBean.getSystemStringCulture(na.getLocalizedValues()).getValue(), null));
                    }
                } else {
                    logChanges.add(new LogChange(na.getAttributeId(), null, strategy.getAttributeLabel(na, systemLocale),
                            null, StringUtil.valueOf(na.getValueId()), null));
                }
            }
        } else {
            for (Attribute oa : oldDomainObject.getAttributes()) {
                EntityAttributeType oldAttributeType = strategy.getEntity().getAttributeType(oa.getAttributeTypeId());
                String oldAttributeValueType = oldAttributeType.getAttributeValueType(oa.getValueTypeId()).getValueType();

                boolean removed = true;
                for (Attribute na : newDomainObject.getAttributes()) {
                    if (oa.getAttributeTypeId().equals(na.getAttributeTypeId()) && oa.getAttributeId().equals(na.getAttributeId())) {
                        //the same attribute_type and the same attribute_id

                        EntityAttributeType newAttributeType = strategy.getEntity().getAttributeType(na.getAttributeTypeId());
                        String newAttributeValueType = newAttributeType.getAttributeValueType(na.getValueTypeId()).getValueType();

                        if (SimpleTypes.isSimpleType(newAttributeValueType) && SimpleTypes.isSimpleType(oldAttributeValueType)) {
                            if (SimpleTypes.STRING_CULTURE.name().equals(newAttributeValueType.toUpperCase())
                                    || SimpleTypes.STRING_CULTURE.name().equals(oldAttributeValueType.toUpperCase())) {
                                for (StringCulture oldString : oa.getLocalizedValues()) {
                                    for (StringCulture newString : na.getLocalizedValues()) {
                                        if (oldString.getLocaleId().equals(newString.getLocaleId())) {
                                            //compare strings
                                            if (!Strings.isEqual(oldString.getValue(), newString.getValue())) {
                                                logChanges.add(new LogChange(na.getAttributeId(), null,
                                                        strategy.getAttributeLabel(oa, systemLocale),
                                                        oldString.getValue(), newString.getValue(),
                                                        localeBean.getLocaleObject(oldString.getLocaleId()).getLanguage()));
                                            }
                                        }
                                    }
                                }
                            } else {
                                String oldValue = stringBean.getSystemStringCulture(oa.getLocalizedValues()).getValue();
                                String newValue = stringBean.getSystemStringCulture(na.getLocalizedValues()).getValue();
                                if (!Strings.isEqual(oldValue, newValue)) {
                                    logChanges.add(new LogChange(oa.getAttributeId(), null,
                                            strategy.getAttributeLabel(oa, systemLocale), oldValue, newValue, null));
                                }
                            }
                        } else {
                            if (!Numbers.isEqual(oa.getValueId(), na.getValueId())) {
                                logChanges.add(new LogChange(oa.getAttributeId(), null, strategy.getAttributeLabel(oa, systemLocale),
                                        StringUtil.valueOf(oa.getValueId()), StringUtil.valueOf(na.getValueId()), null));
                            }
                        }

                        removed = false;
                        break;
                    }
                }

                if (removed) {
                    if (SimpleTypes.isSimpleType(oldAttributeValueType)) {
                        if (SimpleTypes.STRING_CULTURE.name().equals(oldAttributeValueType.toUpperCase())) {
                            for (StringCulture oldString : oa.getLocalizedValues()) {
                                if (!Strings.isEqual(oldString.getValue(), null)) {
                                    logChanges.add(new LogChange(oa.getAttributeId(), null,
                                            strategy.getAttributeLabel(oa, systemLocale),
                                            oldString.getValue(), null,
                                            localeBean.getLocaleObject(oldString.getLocaleId()).getLanguage()));
                                }
                            }
                        } else {
                            logChanges.add(new LogChange(oa.getAttributeId(), null,
                                    strategy.getAttributeLabel(oa, systemLocale),
                                    stringBean.getSystemStringCulture(oa.getLocalizedValues()).getValue(),
                                    null, null));
                        }
                    } else {
                        logChanges.add(new LogChange(oa.getAttributeId(), null, strategy.getAttributeLabel(oa, systemLocale),
                                StringUtil.valueOf(oa.getValueId()), null, null));
                    }
                }
            }

            for (Attribute na : newDomainObject.getAttributes()) {
                EntityAttributeType newAttributeType = strategy.getEntity().getAttributeType(na.getAttributeTypeId());
                String newAttributeValueType = newAttributeType.getAttributeValueType(na.getValueTypeId()).getValueType();

                boolean added = true;
                for (Attribute oa : oldDomainObject.getAttributes()) {
                    if (oa.getAttributeTypeId().equals(na.getAttributeTypeId()) && oa.getAttributeId().equals(na.getAttributeId())) {
                        //the same attribute_type and the same attribute_id
                        added = false;
                        break;
                    }
                }

                if (added) {
                    if (SimpleTypes.isSimpleType(newAttributeValueType)) {
                        if (SimpleTypes.STRING_CULTURE.name().equals(newAttributeValueType.toUpperCase())) {
                            for (StringCulture newString : na.getLocalizedValues()) {
                                if (!Strings.isEqual(newString.getValue(), null)) {
                                    logChanges.add(new LogChange(na.getAttributeId(), null,
                                            strategy.getAttributeLabel(na, systemLocale),
                                            null, newString.getValue(),
                                            localeBean.getLocaleObject(newString.getLocaleId()).getLanguage()));
                                }
                            }
                        } else {
                            logChanges.add(new LogChange(na.getAttributeId(), null,
                                    strategy.getAttributeLabel(na, systemLocale), null,
                                    stringBean.getSystemStringCulture(na.getLocalizedValues()).getValue(), null));
                        }
                    } else {
                        logChanges.add(new LogChange(na.getAttributeId(), null, strategy.getAttributeLabel(na, systemLocale),
                                null, String.valueOf(na.getValueId()), null));
                    }
                }
            }
        }

        return logChanges;
    }
}
