package org.complitex.organization;

import org.apache.wicket.markup.html.WebPage;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LogManager;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.template.strategy.TemplateStrategy;

@Singleton(name = "DefaultOrganizationModule")
@Startup
public class DefaultOrganizationModule implements IOrganizationModule {

    public static final String NAME = "org.complitex.organization";
    public static final String CUSTOM_ORGANIZATION_MODULE_BEAN_NAME = "OrganizationModule";
    
    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    @PostConstruct
    public void init() {
        registerLink();
    }

    private void registerLink() {
        IOrganizationModule organizationModule = EjbBeanLocator.getBean(CUSTOM_ORGANIZATION_MODULE_BEAN_NAME, true);

        if(organizationModule == null){
            organizationModule = this;
        }
        
        LogManager.get().registerLink(DomainObject.class.getName(), organizationStrategy.getEntityTable(), 
                organizationModule.getEditPage(),
                organizationModule.getEditPageParams(), 
                TemplateStrategy.OBJECT_ID);
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return organizationStrategy.getEditPage();
    }

    @Override
    public String getEditPageParams() {
        return TemplateStrategy.ENTITY + "=" + organizationStrategy.getEntityTable();
    }
}
