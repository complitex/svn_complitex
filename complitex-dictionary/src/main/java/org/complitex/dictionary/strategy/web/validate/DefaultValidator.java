/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web.validate;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.util.ResourceUtil;

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
        IStrategy strategy = EjbBeanLocator.getBean(StrategyFactory.class).getStrategy(entity);
        LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);
        Long existingObjectId = strategy.performDefaultValidation(object, localeBean.getSystemLocale());

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
