/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.description.EntityType;
import org.complitex.dictionaryfw.service.EntityBean;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.IDisableAwareChoiceRenderer;
import org.complitex.osznconnection.commons.web.component.toolbar.DeleteItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.EntityTypeCorrection;
import org.complitex.osznconnection.file.service.CorrectionBean;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;

/**
 * Страница для редактирования коррекций типов сущностей.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class EntityTypeCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    @EJB(name = "EntityBean")
    private EntityBean entityBean;

    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;

    private class EntityTypeCorrectionEditPanel extends Panel {

        public EntityTypeCorrectionEditPanel(String id, final EntityTypeCorrection entityTypeCorrection) {
            super(id);

            List<EntityType> allEntityTypes = entityBean.getAllEntityTypes();
            if (allEntityTypes == null) {
                allEntityTypes = Lists.newArrayList();
            }
            final List<EntityType> entityTypes = allEntityTypes;

            IModel<EntityType> entityTypeModel = new Model<EntityType>() {

                @Override
                public void setObject(EntityType entityType) {
                    entityTypeCorrection.setObjectId(entityType.getId());
                }

                @Override
                public EntityType getObject() {
                    if (entityTypeCorrection.getObjectId() != null) {
                        return Iterables.find(entityTypes, new Predicate<EntityType>() {

                            @Override
                            public boolean apply(EntityType entityType) {
                                return entityType.getId().equals(entityTypeCorrection.getObjectId());
                            }
                        });
                    } else {
                        return null;
                    }
                }
            };
            IDisableAwareChoiceRenderer<EntityType> renderer = new IDisableAwareChoiceRenderer<EntityType>() {

                @Override
                public boolean isDisabled(EntityType object) {
                    return object.getEndDate() != null;
                }

                @Override
                public Object getDisplayValue(EntityType object) {
                    return stringBean.displayValue(object.getEntityTypeNames(), getLocale());
                }

                @Override
                public String getIdValue(EntityType object, int index) {
                    return String.valueOf(object.getId());
                }
            };
            DisableAwareDropDownChoice entityType = new DisableAwareDropDownChoice<EntityType>("entityType", entityTypeModel, entityTypes, renderer);
            entityType.setRequired(true);
            add(entityType);
        }
    }

    private AbstractCorrectionEditPanel correctionEditPanel;

    public EntityTypeCorrectionEdit(PageParameters params) {
        Long correctionId = params.getAsLong(CORRECTION_ID);
        add(correctionEditPanel = new AbstractCorrectionEditPanel("correctionEditPanel", null, correctionId) {

            @EJB(name = "CorrectionBean")
            private CorrectionBean correctionBean;

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("entityType");
            }

            @Override
            protected EntityTypeCorrection initObjectCorrection(String entity, Long correctionId) {
                return correctionBean.findEntityTypeById(correctionId);
            }

            @Override
            protected EntityTypeCorrection newObjectCorrection() {
                return new EntityTypeCorrection();
            }

            @Override
            protected EntityTypeCorrection getModel() {
                return (EntityTypeCorrection) super.getModel();
            }

            @Override
            protected Panel internalObjectPanel(String id) {
                return new EntityTypeCorrectionEditPanel(id, getModel());
            }

            @Override
            protected void back() {
                PageParameters parameters = new PageParameters();
                parameters.put(EntityTypesCorrectionList.CORRECTED_ENTITY, getEntity());
                setResponsePage(EntityTypesCorrectionList.class, parameters);
            }

            @Override
            protected void save() {
                correctionBean.insertEntityType(getModel());
            }

            @Override
            protected void update() {
                correctionBean.updateEntityType(getModel());
            }

            @Override
            protected void delete() {
                correctionBean.delete(getModel());
            }
        });
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                correctionEditPanel.executeDeletion();
            }

            @Override
            public boolean isVisible() {
                return !correctionEditPanel.isNew();
            }
        });
        return toolbar;
    }
}

