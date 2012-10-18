/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.component;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;

/**
 *
 * @author Artem
 */
public class MainUserOrganizationPicker extends Panel implements IMainUserOrganizationPicker {

    @EJB
    private SessionBean sessionBean;
    @EJB
    private StrategyFactory strategyFactory;
    private final boolean visible;

    public MainUserOrganizationPicker(String id, final IModel<DomainObject> model) {
        super(id);

        final List<DomainObject> userOrganizations = getUserOrganizationObjects();
        final IModel<DomainObject> mainUserOrganizationModel = new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                DomainObject object = model.getObject();
                if (object != null && object.getId() != null) {
                    for (DomainObject o : userOrganizations) {
                        if (o.getId().equals(object.getId())) {
                            return o;
                        }
                    }
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                model.setObject(object);
            }
        };

        DisableAwareDropDownChoice<DomainObject> mainUserOrganizationPicker =
                new DisableAwareDropDownChoice<DomainObject>("select", mainUserOrganizationModel, userOrganizations,
                new DomainObjectDisableAwareRenderer() {

                    @Override
                    public Object getDisplayValue(DomainObject userOrganization) {
                        return displayOrganization(userOrganization);
                    }
                });

        if (userOrganizations.isEmpty()) {
            mainUserOrganizationPicker.setVisible(false);
        } else if (userOrganizations.size() == 1) {
            mainUserOrganizationPicker.setEnabled(false);
        } else {
            mainUserOrganizationPicker.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });
        }
        visible = mainUserOrganizationPicker.isVisible();
        add(mainUserOrganizationPicker);
    }

    protected String displayOrganization(DomainObject organization) {
        return getOrgaizationStrategy().displayDomainObject(organization, getLocale());
    }

    private List<DomainObject> getUserOrganizationObjects() {
        List<DomainObject> results = new ArrayList<>();
        List<Long> ids = sessionBean.getUserOrganizationObjectIds();
        if (!ids.isEmpty()) {
            for (long id : ids) {
                results.add(getOrgaizationStrategy().findById(id, true));
            }
        }
        return results;
    }

    protected String getOrganizationStrategyName() {
        return "OrganizationStrategy";
    }

    protected final IOrganizationStrategy getOrgaizationStrategy() {
        return (IOrganizationStrategy) strategyFactory.getStrategy(getOrganizationStrategyName(), "organization");
    }

    public boolean visible() {
        return visible;
    }
}
