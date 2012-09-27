package org.complitex.dictionary;

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
}
