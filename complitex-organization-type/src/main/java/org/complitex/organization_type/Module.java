package org.complitex.organization_type;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LogManager;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.pages.DomainObjectEdit;

@Singleton(name = "OrganizationTypeModule")
@Startup
public class Module {

    public static final String NAME = "org.complitex.organization_type";
    @EJB
    private OrganizationTypeStrategy organizationTypeStrategy;

    @PostConstruct
    public void init() {
        LogManager.get().registerLink(DomainObject.class.getName(), organizationTypeStrategy.getEntityTable(), DomainObjectEdit.class,
                organizationTypeStrategy.getEditPageParams(null, null, null), TemplateStrategy.OBJECT_ID);
    }
}
