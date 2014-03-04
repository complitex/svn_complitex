package org.complitex.address;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LogManager;
import org.complitex.template.web.pages.DomainObjectEdit;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.template.strategy.TemplateStrategy;

@Singleton(name = "AddressModule")
@Startup
public class Module {

    public static final String NAME = "org.complitex.address";
    @EJB
    private AddressInfoProvider addressInfoProvider;

    @PostConstruct
    public void init() {
        for (String e : addressInfoProvider.getAddressInfo().getAddresses()) {
            LogManager.get().registerLink(DomainObject.class.getName(), e, DomainObjectEdit.class,
                    new PageParameters().set(TemplateStrategy.ENTITY, e), TemplateStrategy.OBJECT_ID);
        }
    }
}
