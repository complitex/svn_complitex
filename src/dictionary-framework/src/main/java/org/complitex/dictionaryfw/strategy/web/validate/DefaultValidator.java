/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.strategy.web.validate;

import java.util.Locale;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.LocaleBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionaryfw.util.EjbBeanLocator;
import org.complitex.dictionaryfw.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public class DefaultValidator implements IValidator {

    private static final String RESOURCE_BUNDLE = DefaultValidator.class.getName();

    private String entity;

    public DefaultValidator(String entity) {
        this.entity = entity;
    }

    @Override
    public boolean validate(DomainObject object, DomainObjectEditPanel editPanel) {
        Strategy strategy = EjbBeanLocator.getBean(StrategyFactory.class).getStrategy(entity);
        Locale systemLocale = new Locale(EjbBeanLocator.getBean(LocaleBean.class).getSystemLocale());
        Long existingObjectId = strategy.performDefaultValidation(object, systemLocale);

        if (existingObjectId != null) {
            StringCultureBean stringBean = EjbBeanLocator.getBean(StringCultureBean.class);
            String entityName = stringBean.displayValue(strategy.getEntity().getEntityNames(), editPanel.getLocale());
            editPanel.error(ResourceUtil.getFormatString(RESOURCE_BUNDLE, "default_validation_error", editPanel.getLocale(), entityName,
                    existingObjectId));
            return false;
        }
        return true;
    }
}
