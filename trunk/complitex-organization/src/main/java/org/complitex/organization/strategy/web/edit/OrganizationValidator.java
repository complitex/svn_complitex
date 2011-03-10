package org.complitex.organization.strategy.web.edit;

import org.apache.wicket.Component;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.organization.strategy.OrganizationStrategy;

import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class OrganizationValidator implements IValidator {
    private Locale systemLocale;

    public OrganizationValidator(Locale systemLocale) {
        this.systemLocale = systemLocale;
    }
    private OrganizationEditComponent organizationEditComponent;

    @Override
    public boolean validate(DomainObject object, DomainObjectEditPanel editPanel) {
        boolean valid = checkDistrict(object, getEditComponent(editPanel));

        if (valid) {
            valid = checkUniqueness(object, getEditComponent(editPanel));
        }

        return valid;
    }

    private OrganizationEditComponent getEditComponent(DomainObjectEditPanel editPanel) {
        if (organizationEditComponent == null) {
            editPanel.visitChildren(OrganizationEditComponent.class, new Component.IVisitor<OrganizationEditComponent>() {

                @Override
                public Object component(OrganizationEditComponent component) {
                    organizationEditComponent = component;
                    return STOP_TRAVERSAL;
                }
            });
        }

        return organizationEditComponent;
    }

    protected boolean checkDistrict(DomainObject object, OrganizationEditComponent editComponent) {
        return true;
    }

    private boolean checkUniqueness(DomainObject object, OrganizationEditComponent editComponent) {
        IOrganizationStrategy organizationStrategy = EjbBeanLocator.getBean(OrganizationStrategy.class);

        boolean valid = true;

        Long byName = organizationStrategy.validateName(object.getId(), organizationStrategy.getName(object, systemLocale),
                object.getParentId(), object.getParentEntityId(), systemLocale);
        if (byName != null) {
            valid = false;
            editComponent.error(MessageFormat.format(editComponent.getString("unique_name"), byName));
        }

        Long byCode = organizationStrategy.validateCode(object.getId(), organizationStrategy.getCode(object),
                object.getParentId(), object.getParentEntityId());
        if (byCode != null) {
            valid = false;
            editComponent.error(MessageFormat.format(editComponent.getString("unique_code"), byCode));
        }

        return valid;
    }
}
