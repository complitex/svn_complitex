/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public class EntityTypePanel extends Panel {

    @EJB
    private StrategyFactory strategyFactory;
    @EJB
    private LocaleBean localeBean;
    @EJB
    private StringCultureBean stringBean;
    private String entityType;
    private long entityTypeDisplayAttributeTypeId;
    private IModel<Long> entityTypeObjectIdModel;
    private IModel<String> labelModel;
    private boolean enabled;
    private boolean required;

    public EntityTypePanel(String id, String entityType, long entityTypeDisplayAttributeTypeId,
            IModel<Long> entityTypeObjectIdModel, IModel<String> labelModel, boolean required, boolean enabled) {
        super(id);

        this.entityType = entityType;
        this.entityTypeDisplayAttributeTypeId = entityTypeDisplayAttributeTypeId;
        this.entityTypeObjectIdModel = entityTypeObjectIdModel;
        this.labelModel = labelModel;
        this.enabled = enabled;
        this.required = required;

        init();
    }

    private String displayEntityTypeObject(DomainObject entityTypeObject, Locale locale) {
        Attribute displayAttribute = entityTypeObject.getAttribute(entityTypeDisplayAttributeTypeId);
        return stringBean.displayValue(displayAttribute.getLocalizedValues(), locale);
    }

    private void init() {
        final IModel<List<? extends DomainObject>> entityTypesModel = new LoadableDetachableModel<List<? extends DomainObject>>() {

            @Override
            protected List<? extends DomainObject> load() {
                return getEntityTypes();
            }
        };
        IModel<DomainObject> entityTypeModel = new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                final Long entityTypeObjectId = entityTypeObjectIdModel.getObject();
                if (entityTypeObjectId != null) {
                    return find(entityTypesModel.getObject(), new Predicate<DomainObject>() {

                        @Override
                        public boolean apply(DomainObject entityTypeId) {
                            return entityTypeId.getId().equals(entityTypeObjectId);
                        }
                    });
                }
                return null;
            }

            @Override
            public void setObject(DomainObject entityTypeObject) {
                entityTypeObjectIdModel.setObject(entityTypeObject.getId());
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject entityTypeObject) {
                return displayEntityTypeObject(entityTypeObject, getLocale());
            }
        };
        DisableAwareDropDownChoice<DomainObject> entityTypeChoice = new DisableAwareDropDownChoice<DomainObject>("entityType",
                entityTypeModel, entityTypesModel, renderer);
        entityTypeChoice.setRequired(required);
        entityTypeChoice.setEnabled(enabled);
        entityTypeChoice.setLabel(labelModel);
        add(entityTypeChoice);
    }

    private List<? extends DomainObject> getEntityTypes() {
        IStrategy strategy = strategyFactory.getStrategy(entityType);
        DomainObjectExample example = new DomainObjectExample();
        example.setLocaleId(localeBean.convert(getLocale()).getId());
        example.setOrderByAttributeTypeId(entityTypeDisplayAttributeTypeId);
        example.setAsc(true);
        strategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
        return strategy.find(example);
    }
}
