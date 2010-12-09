/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.warning;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.util.EjbBeanLocator;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.RequestWarning;
import org.complitex.osznconnection.file.entity.RequestWarningParameter;
import org.complitex.osznconnection.file.entity.RequestWarningStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public abstract class AbstractWarningRenderer implements IWarningRenderer {

    private static final Logger log = LoggerFactory.getLogger(AbstractWarningRenderer.class);
    
    private static final Comparator<RequestWarningParameter> WARNING_PARAMETER_COMPARATOR = new Comparator<RequestWarningParameter>() {

        @Override
        public int compare(RequestWarningParameter o1, RequestWarningParameter o2) {
            return o1.getOrder().compareTo(o2.getOrder());
        }
    };

    @Override
    public String display(List<RequestWarning> requestWarnings, Locale locale) {
        String warning = "";

        for (RequestWarning requestWarning : requestWarnings) {
            String currentWarning = display(requestWarning, locale);
            String toAdd = null;
            if (Strings.isEmpty(warning)) {
                toAdd = currentWarning;
            } else {
                toAdd = " " + currentWarning;
            }
            warning = warning + toAdd;
        }
        return warning;
    }

    @Override
    public String display(RequestWarning requestWarning, Locale locale) {
        RequestWarningStatus warningStatus = requestWarning.getStatus();
        Object[] messageParams = null;
        List<RequestWarningParameter> parameters = requestWarning.getParameters();
        if (!parameters.isEmpty()) {
            Collections.sort(parameters, WARNING_PARAMETER_COMPARATOR);
            messageParams = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                Object parameterValue = handleParameterValue(parameters.get(i), locale);
                messageParams[i] = parameterValue;
            }
        }
        return getMessage(warningStatus, messageParams, locale);
    }

    protected String getMessage(RequestWarningStatus warningStatus, Object[] params, Locale locale) {
        return ResourceUtil.getFormatString(getBundle(), warningStatus.name(), locale, params);
    }

    protected abstract String getBundle();

    protected Object handleParameterValue(RequestWarningParameter parameter, Locale locale) {
        String type = parameter.getType();
        String value = parameter.getValue();

        if (Strings.isEmpty(type)) {
            return value;
        }

        return handleReferenceType(type, value, locale);
    }

    protected Object handleDouble(String value, Locale locale) {
        Double doubleValue = Double.valueOf(value);
        return doubleValue;
    }

    protected String handleReferenceType(String type, String value, Locale locale) {
        Long objectId = null;
        try {
            objectId = Long.valueOf(value);
            return displayObject(type, objectId, locale);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    protected String displayObject(String entity, long objectId, Locale locale) {
        Strategy strategy = EjbBeanLocator.getBean(StrategyFactory.class).getStrategy(entity);
        DomainObject object = strategy.findById(objectId);
        if (object != null) {
            return strategy.displayDomainObject(object, locale);
        }
        return null;
    }
}
