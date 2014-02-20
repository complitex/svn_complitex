package org.complitex.dictionary.entity;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.02.11 13:10
 */
public interface IConfig {
    public String name();

    public String getDefaultValue();

    public String getGroupKey();

    public WebMarkupContainer getContainer(String id, IModel<String> model);
}
