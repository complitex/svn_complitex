/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web.validate;

import java.util.Locale;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public abstract class CodeValidator implements IValidator {

    private static final String RESOURCE_BUNDLE = CodeValidator.class.getName();
    private final String entity;
    private final String strategyName;
    private final long codeAttributeTypeId;

    public CodeValidator(String entity, String strategyName, long codeAttributeTypeId) {
        this.entity = entity;
        this.strategyName = strategyName;
        this.codeAttributeTypeId = codeAttributeTypeId;
    }

    public CodeValidator(String entity, long codeAttributeTypeId) {
        this(entity, null, codeAttributeTypeId);
    }

    @Override
    public boolean validate(DomainObject object, DomainObjectEditPanel editPanel) {
        Attribute codeAttribute = object.getAttribute(codeAttributeTypeId);
        if (codeAttribute == null) {
            throw new IllegalStateException("Domain object(entity = " + entity + ", id = " + object.getId()
                    + ") has no attribute with attribute type id = " + codeAttributeTypeId + "!");
        }
        if (codeAttribute.getLocalizedValues() == null) {
            throw new IllegalStateException("Attribute of domain object(entity = " + entity + ", id = " + object.getId()
                    + ") with attribute type id = " + codeAttribute + " and attribute id = " + codeAttribute.getAttributeId()
                    + " has null lozalized values.");
        }

        LocaleBean localeBean = EjbBeanLocator.getBean(LocaleBean.class);
        String code = getStringBean().displayValue(codeAttribute.getLocalizedValues(), localeBean.getSystemLocale());

        Long existingId = validateCode(object.getId(), code);
        if (existingId != null) {
            editPanel.error(getErrorMessage(existingId, code, editPanel.getLocale()));
            return false;
        }
        return true;
    }

    protected String getErrorMessage(Long existingId, String code, Locale locale) {
        IStrategy strategy = EjbBeanLocator.getBean(StrategyFactory.class).getStrategy(strategyName, entity);
        String entityName = getStringBean().displayValue(strategy.getEntity().getEntityNames(), locale);
        return ResourceUtil.getFormatString(RESOURCE_BUNDLE, "code_validation_error", locale, entityName, existingId);
    }

    protected final StringCultureBean getStringBean() {
        return EjbBeanLocator.getBean(StringCultureBean.class);
    }

    protected abstract Long validateCode(Long id, String code);
}
