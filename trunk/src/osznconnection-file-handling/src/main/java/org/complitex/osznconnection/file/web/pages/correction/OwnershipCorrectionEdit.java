/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.base.Predicate;
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
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.commons.web.component.toolbar.DeleteItemButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;
import org.complitex.osznconnection.ownership.strategy.OwnershipStrategy;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class OwnershipCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    private class OwnershipCorrectionEditPanel extends Panel {

        @EJB(name = "OwnershipStrategy")
        private OwnershipStrategy ownershipStrategy;

        public OwnershipCorrectionEditPanel(String id, final ObjectCorrection ownershipCorrection) {
            super(id);

            final List<DomainObject> allOwnerships = ownershipStrategy.getAll();
            IModel<DomainObject> ownershipModel = new Model<DomainObject>() {

                @Override
                public DomainObject getObject() {
                    final Long ownershipId = ownershipCorrection.getInternalObjectId();
                    if (ownershipId != null) {
                        return Iterables.find(allOwnerships, new Predicate<DomainObject>() {

                            @Override
                            public boolean apply(DomainObject object) {
                                return object.getId().equals(ownershipId);
                            }
                        });
                    }
                    return null;
                }

                @Override
                public void setObject(DomainObject object) {
                    ownershipCorrection.setInternalObjectId(object.getId());
                }
            };
            DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

                @Override
                public Object getDisplayValue(DomainObject object) {
                    return ownershipStrategy.displayDomainObject(object, getLocale());
                }
            };
            DisableAwareDropDownChoice<DomainObject> ownership = new DisableAwareDropDownChoice<DomainObject>("ownership",
                    ownershipModel, allOwnerships, renderer);
            ownership.setRequired(true);
            add(ownership);
        }
    }

    private AbstractCorrectionEditPanel correctionEditPanel;

    public OwnershipCorrectionEdit(PageParameters params) {
        Long correctionId = params.getAsLong(CORRECTION_ID);
        add(correctionEditPanel = new AbstractCorrectionEditPanel("correctionEditPanel", "ownership", correctionId) {

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("ownership");
            }

            @Override
            protected Panel internalObjectPanel(String id) {
                return new OwnershipCorrectionEditPanel(id, getModel());
            }

            @Override
            protected boolean isOrganizationCodeRequired() {
                return true;
            }

            @Override
            protected void back() {
                PageParameters parameters = new PageParameters();
                parameters.put(OwnershipCorrectionList.CORRECTED_ENTITY, getEntity());
                setResponsePage(OwnershipCorrectionList.class, parameters);
            }
        });
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                correctionEditPanel.delete();
            }

            @Override
            public boolean isVisible() {
                return !correctionEditPanel.isNew();
            }
        });
        return toolbar;
    }
}

