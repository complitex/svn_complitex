package org.complitex.dictionary.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.converter.*;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.web.component.type.*;
import org.complitex.dictionary.web.model.AttributeStringModel;
import org.complitex.dictionary.web.model.SimpleTypeModel;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.complitex.dictionary.strategy.web.DomainObjectAccessUtil.canEdit;
import static org.complitex.dictionary.util.EjbBeanLocator.getBean;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.04.2014 16:59
 */
public class DomainObjectComponentUtil {
    public static final String INPUT_COMPONENT_ID = "input";

    private static StringCultureBean stringBean() {
        return getBean(StringCultureBean.class);
    }

    public static IModel<String> labelModel(final List<StringCulture> attributeNames, final Locale locale) {
        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return Strings.capitalize(stringBean().displayValue(attributeNames, locale).toLowerCase(locale));
            }
        };
    }

    public static Component newInputComponent(String entityTable, String strategyName, DomainObject object,
            Attribute attribute, final Locale locale, boolean isHistory) {
        StrategyFactory strategyFactory = getBean(StrategyFactory.class);
        IStrategy strategy = strategyFactory.getStrategy(strategyName, entityTable);

        Entity entity = strategy.getEntity();
        EntityAttributeType attributeType = entity.getAttributeType(attribute.getAttributeTypeId());
        IModel<String> labelModel = labelModel(attributeType.getAttributeNames(), locale);
        String valueType = attributeType.getEntityAttributeValueTypes().get(0).getValueType();
        SimpleTypes type = SimpleTypes.valueOf(valueType.toUpperCase());
        Component input = null;

        switch (type) {
            case STRING: {
                input = new StringPanel(INPUT_COMPONENT_ID, new AttributeStringModel(attribute), attributeType.isMandatory(),
                        labelModel, !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case BIG_STRING: {
                input = new BigStringPanel(INPUT_COMPONENT_ID, new AttributeStringModel(attribute), attributeType.isMandatory(),
                        labelModel, !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case STRING_CULTURE: {
                IModel<List<StringCulture>> model = new PropertyModel<>(attribute, "localizedValues");
                input = new StringCulturePanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case INTEGER: {
                IModel<Integer> model = new SimpleTypeModel<>(attribute, new IntegerConverter());
                input = new IntegerPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case DATE: {
                IModel<Date> model = new SimpleTypeModel<>(attribute, new DateConverter());
                input = new DatePanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case DATE2: {
                IModel<Date> model = new SimpleTypeModel<>(attribute, new DateConverter());
                input = new Date2Panel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case MASKED_DATE: {
                IModel<Date> model = new SimpleTypeModel<>(attribute, new DateConverter());
                input = new MaskedDateInputPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case BOOLEAN: {
                IModel<Boolean> model = new SimpleTypeModel<>(attribute, new BooleanConverter());
                input = new BooleanPanel(INPUT_COMPONENT_ID, model, labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case DOUBLE: {
                IModel<Double> model = new SimpleTypeModel<>(attribute, new DoubleConverter());
                input = new DoublePanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
            case GENDER: {
                IModel<Gender> model = new SimpleTypeModel<>(attribute, new GenderConverter());
                input = new GenderPanel(INPUT_COMPONENT_ID, model, attributeType.isMandatory(), labelModel,
                        !isHistory && canEdit(strategyName, entityTable, object));
            }
            break;
        }

        if (input == null) {
            throw new IllegalStateException("Input component for attribute type " + attributeType.getId() + " is not recognized.");
        }

        return input;
    }
}
