/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

/**
 *
 * @author Artem
 */
public final class UserOrganizationPicker extends Panel {

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    public UserOrganizationPicker(String id, IModel<Long> organizationIdModel, Long... excludeOrganizationsId) {
        super(id);
        init(organizationIdModel, excludeOrganizationsId);
    }

    private void init(final IModel<Long> organizationIdModel, final Long... excludeOrganizationsId) {
        final IModel<List<? extends DomainObject>> userOrganizationsModel = new LoadableDetachableModel<List<? extends DomainObject>>() {

            @Override
            protected List<? extends DomainObject> load() {
                return organizationStrategy.getUserOrganizations(getLocale(), excludeOrganizationsId);
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        IModel<DomainObject> model = new Model<DomainObject>() {

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
                organizationIdModel.setObject(object.getId());
            }
        };
        DisableAwareDropDownChoice<DomainObject> select = new DisableAwareDropDownChoice<DomainObject>("select", model, userOrganizationsModel, renderer);
        add(select);
    }
}
