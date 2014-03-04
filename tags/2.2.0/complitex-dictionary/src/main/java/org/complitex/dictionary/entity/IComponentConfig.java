package org.complitex.dictionary.entity;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.02.14 15:38
 */
public interface IComponentConfig extends IConfig {
    public WebMarkupContainer getComponent(String id, IModel<String> model);
}
