/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.commons.web.component.toolbar.DeleteItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;
import org.complitex.osznconnection.information.strategy.street_type.StreetTypeStrategy;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class StreetTypeCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    private class StreetTypeChoicePanel extends Panel {

        @EJB(name = "StreetTypeStrategy")
        private StreetTypeStrategy streetTypeStrategy;

        public StreetTypeChoicePanel(String id, final Correction streetTypeCorrection) {
            super(id);

            final List<DomainObject> allStreetTypes = getEntityTypes();
            IModel<DomainObject> entityTypeModel = new Model<DomainObject>() {

                @Override
                public DomainObject getObject() {
                    final Long streetTypeId = streetTypeCorrection.getObjectId();
                    if (streetTypeId != null) {
                        return Iterables.find(allStreetTypes, new Predicate<DomainObject>() {

                            @Override
                            public boolean apply(DomainObject streetTypeObject) {
                                return streetTypeObject.getId().equals(streetTypeId);
                            }
                        });
                    }
                    return null;
                }

                @Override
                public void setObject(DomainObject object) {
                    streetTypeCorrection.setObjectId(object.getId());
                }
            };
            DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

                @Override
                public Object getDisplayValue(DomainObject object) {
                    return streetTypeStrategy.displayDomainObject(object, getLocale());
                }
            };
            DisableAwareDropDownChoice<DomainObject> streetType = new DisableAwareDropDownChoice<DomainObject>("streetType",
                    entityTypeModel, allStreetTypes, renderer);
            streetType.setRequired(true);
            add(streetType);
        }

        private List<DomainObject> getEntityTypes() {
            DomainObjectExample example = new DomainObjectExample();
            streetTypeStrategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
            return streetTypeStrategy.find(example);
        }
    }

    private AbstractCorrectionEditPanel correctionEditPanel;

    public StreetTypeCorrectionEdit(PageParameters params) {
        Long correctionId = params.getAsLong(CORRECTION_ID);
        add(correctionEditPanel = new AbstractCorrectionEditPanel("correctionEditPanel", "street_type", correctionId) {

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("street_type");
            }

            @Override
            protected Panel internalObjectPanel(String id) {
                return new StreetTypeChoicePanel(id, getModel());
            }

            @Override
            protected void back() {
                PageParameters parameters = new PageParameters();
                parameters.put(StreetTypeCorrectionList.CORRECTED_ENTITY, getEntity());
                setResponsePage(StreetTypeCorrectionList.class, parameters);
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
