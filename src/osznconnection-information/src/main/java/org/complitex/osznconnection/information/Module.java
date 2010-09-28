package org.complitex.osznconnection.information;

import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.LogManager;
import org.complitex.osznconnection.commons.web.pages.DomainObjectEdit;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.08.2010 18:41:01
 */
@Singleton(name="InformationModule")
@Startup
public class Module {
    public static final String NAME = "org.complitex.osznconnection.information";

    @PostConstruct
    public void init(){
        for (String e : BookEntities.getEntities()){
            LogManager.get().registerLink(DomainObject.class.getName(), e, DomainObjectEdit.class, "entity="+e, "object_id");            
        }
    }
}
