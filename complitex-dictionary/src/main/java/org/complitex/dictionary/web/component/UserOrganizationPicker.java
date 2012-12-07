/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import javax.ejb.EJB;
import java.util.List;
import org.complitex.dictionary.web.component.organization.user.UserOrganizationPickerParameters;

/**
 *
 * @author Artem
 */
public class UserOrganizationPicker extends Panel {

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    public UserOrganizationPicker(String id, IModel<Long> organizationIdModel, Long... excludeOrganizationsId) {
        this(id, organizationIdModel, false, excludeOrganizationsId);
    }

    public UserOrganizationPicker(String id, IModel<Long> organizationIdModel, boolean updating, Long... excludeOrganizationsId) {
        super(id);
        init(organizationIdModel, updating, excludeOrganizationsId);
    }

    public UserOrganizationPicker(String id, IModel<Long> organizationIdModel, UserOrganizationPickerParameters parameters) {
        this(id, organizationIdModel, parameters.isUpdating());
    }

    private void init(final IModel<Long> organizationIdModel, boolean updating, final Long... excludeOrganizationsId) {
        final IModel<List<? extends DomainObject>> userOrganizationsModel = new LoadableDetachableModel<List<? extends DomainObject>>() {

            @Override
            protected List<? extends DomainObject> load() {
                // organizations visible to user
                List<? extends DomainObject> userOrganizatons =
                        organizationStrategy.getUserOrganizations(getLocale(), excludeOrganizationsId);

                //maybe current organization is not visible to user however we have to add it to list of user organizations as well
                final Long currentOrganizationId = organizationIdModel.getObject();
                if (currentOrganizationId != null) {
                    // check whether current organization within list already
                    boolean found = false;
                    for (DomainObject o : userOrganizatons) {
                        if (o.getId().equals(currentOrganizationId)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        userOrganizatons = ImmutableList.<DomainObject>builder().addAll(userOrganizatons).
                                add(organizationStrategy.findById(currentOrganizationId, true)).build();
                    }
                }
                return userOrganizatons;
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        final IModel<DomainObject> model = new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                final Long id = organizationIdModel.getObject();
                if (id != null) {
                    return Iterables.find(userOrganizationsModel.getObject(), new Predicate<DomainObject>() {

                        @Override
                        public boolean apply(DomainObject input) {
                            return id.equals(input.getId());
                        }
                    });
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                organizationIdModel.setObject(object != null ? object.getId() : null);
            }
        };
        DisableAwareDropDownChoice<DomainObject> select = new DisableAwareDropDownChoice<DomainObject>("select", model,
                userOrganizationsModel, renderer);
        select.setNullValid(true);
        select.setOutputMarkupId(true);

        if (updating) {
            select.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    UserOrganizationPicker.this.onUpdate(target, model.getObject());
                }
            });
        }

        add(select);
    }

    protected void onUpdate(AjaxRequestTarget target, DomainObject newOrganization) {
    }
}
