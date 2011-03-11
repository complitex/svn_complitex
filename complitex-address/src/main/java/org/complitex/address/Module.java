package org.complitex.address;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.LogManager;
import org.complitex.template.web.pages.DomainObjectEdit;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.08.2010 18:41:01
 */
@Singleton(name = "InformationModule")
@Startup
public class Module {

    @EJB
    private AddressInfoProvider addressInfoProvider;

    public static final String NAME = "org.complitex.address";

    @PostConstruct
    public void init() {
        for (String e : addressInfoProvider.getAddressInfo().getAddresses()) {
            LogManager.get().registerLink(DomainObject.class.getName(), e, DomainObjectEdit.class, "entity=" + e, "object_id");
        }
    }
}
