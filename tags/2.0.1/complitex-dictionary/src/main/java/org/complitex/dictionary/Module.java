package org.complitex.dictionary;

import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.service.ConfigBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 20.08.2010 18:38:47
 */
@Singleton(name="DictionaryModule")
@Startup
public class Module {
    public final static String NAME = "org.complitex.dictionary";

    @EJB
    private ConfigBean configBean;

    @PostConstruct
    public void init(){
        configBean.init(DictionaryConfig.class.getCanonicalName(), DictionaryConfig.values());
    }
}
