/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.base.Predicate;
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

    private void init(final IModel<Long> organizationIdModel, boolean updating, final Long... excludeOrganizationsId) {
        final IModel<List<? extends DomainObject>> allOrganizationsModel = new LoadableDetachableModel<List<? extends DomainObject>>() {

            @Override
            protected List<? extends DomainObject> load() {
                return organizationStrategy.getAllOrganizations(getLocale(), excludeOrganizationsId);
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
                    return Iterables.find(allOrganizationsModel.getObject(), new Predicate<DomainObject>() {

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
                if (object != null) {
                    organizationIdModel.setObject(object.getId());
                }
            }
        };
        DisableAwareDropDownChoice<DomainObject> select = new DisableAwareDropDownChoice<DomainObject>("select", model,
                allOrganizationsModel, renderer);
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
