package org.complitex.organization.web.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

import javax.ejb.EJB;

public class UserOrganizationPicker extends Panel {

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    public UserOrganizationPicker(String id, final IModel<Long> organizationIdModel) {
        super(id);

        add(new OrganizationPicker("picker", new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                Long id = organizationIdModel.getObject();
                if (id != null) {
                    return organizationStrategy.findById(id, true);
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                organizationIdModel.setObject(object != null ? object.getId() : null);
            }
        }, OrganizationTypeStrategy.USER_ORGANIZATION_TYPE));
    }
}
