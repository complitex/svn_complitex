/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web.component;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;

/**
 *
 * @author Artem
 */
public class EntityTypePanel extends Panel {

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

    private String entityType;

    private DomainObject object;

    private long entityTypeAttribute;

    private IModel<String> labelModel;

    public EntityTypePanel(String id, String entityType, DomainObject object, long entityTypeAttribute, IModel<String> labelModel) {
        super(id);
        this.entityType = entityType;
        this.object = object;
        this.entityTypeAttribute = entityTypeAttribute;
        this.labelModel = labelModel;
        init();
    }

    private void init() {
        final List<DomainObject> entityTypes = getEntityTypes();
        IModel<DomainObject> entityTypeModel = new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                final Long entityTypeObjectId = getEntityType();
                if (entityTypeObjectId != null) {
                    return Iterables.find(entityTypes, new Predicate<DomainObject>() {

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
                entityTypeModel, entityTypes, renderer);
        entityTypeChoice.setRequired(true);
        entityTypeChoice.setLabel(labelModel);
        add(entityTypeChoice);
    }

    private Attribute findEntityTypeAttribute() {
        return Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(entityTypeAttribute);
            }
        });
    }

    private Strategy getEntityTypeStrategy() {
        return strategyFactory.getStrategy(entityType);
    }

    private Long getEntityType() {
        return findEntityTypeAttribute().getValueId();
    }

    private void setEntityType(Long entityTypeObjectId) {
        findEntityTypeAttribute().setValueId(entityTypeObjectId);
    }

    private List<DomainObject> getEntityTypes() {
        Strategy strategy = getEntityTypeStrategy();
        DomainObjectExample example = new DomainObjectExample();
        strategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
        return strategy.find(example);
    }
}
