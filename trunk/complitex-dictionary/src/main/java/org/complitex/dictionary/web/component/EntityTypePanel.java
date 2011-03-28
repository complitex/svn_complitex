/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.service.LocaleBean;
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
    private String entityType;
    private long entityTypeOrderByAttributeTypeId;
    private DomainObject object;
    private long entityTypeAttribute;
    private IModel<String> labelModel;
    private boolean enabled;

    public EntityTypePanel(String id, String entityType, long entityTypeOrderByAttributeTypeId, DomainObject object, long entityTypeAttribute,
            IModel<String> labelModel, boolean enabled) {
        super(id);

        this.entityType = entityType;
        this.entityTypeOrderByAttributeTypeId = entityTypeOrderByAttributeTypeId;
        this.object = object;
        this.entityTypeAttribute = entityTypeAttribute;
        this.labelModel = labelModel;
        this.enabled = enabled;

        init();
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
                final Long entityTypeObjectId = getEntityType();
                if (entityTypeObjectId != null) {
                    return Iterables.find(entityTypesModel.getObject(), new Predicate<DomainObject>() {

                        @Override
                        public boolean apply(DomainObject entityTypeId) {
                            return entityTypeId.getId().equals(entityTypeObjectId);
                        }
                    });
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                setEntityType(object.getId());
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return getEntityTypeStrategy().displayDomainObject(object, getLocale());
            }
        };
        DisableAwareDropDownChoice<DomainObject> entityTypeChoice = new DisableAwareDropDownChoice<DomainObject>("entityType",
                entityTypeModel, entityTypesModel, renderer);
        entityTypeChoice.setRequired(true);
        entityTypeChoice.setEnabled(enabled);
        entityTypeChoice.setLabel(labelModel);
        add(entityTypeChoice);
    }

    private Attribute findEntityTypeAttribute() {
        Attribute attr = object.getAttribute(entityTypeAttribute);
        if (attr != null) {
            return attr;
        } else {
            throw new RuntimeException("Couldn't find entity attribute with attribute type id = " + entityTypeAttribute);
        }
    }

    private IStrategy getEntityTypeStrategy() {
        return strategyFactory.getStrategy(entityType);
    }

    private Long getEntityType() {
        return findEntityTypeAttribute().getValueId();
    }

    private void setEntityType(Long entityTypeObjectId) {
        findEntityTypeAttribute().setValueId(entityTypeObjectId);
    }

    private List<? extends DomainObject> getEntityTypes() {
        IStrategy strategy = getEntityTypeStrategy();
        DomainObjectExample example = new DomainObjectExample();
        example.setLocaleId(localeBean.convert(getLocale()).getId());
        example.setOrderByAttributeTypeId(entityTypeOrderByAttributeTypeId);
        example.setAsc(true);
        strategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
        return strategy.find(example);
    }
}
