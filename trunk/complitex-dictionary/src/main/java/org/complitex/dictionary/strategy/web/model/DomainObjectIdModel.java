/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy.web.model;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author Artem
 */
public final class DomainObjectIdModel implements IModel<String> {

    private final IModel<Long> idModel;
    private String value;

    public DomainObjectIdModel(IModel<Long> idModel) {
        this.idModel = idModel;
    }

    @Override
    public String getObject() {
        Long id = idModel.getObject();
        if (id != null && id > 0) {
            return String.valueOf(id);
        } else {
            return value;
        }
    }

    @Override
    public void setObject(String value) {
        this.value = value;
        if (!Strings.isEmpty(value)) {
            Long id = -1L;
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException e) {
            }
            idModel.setObject(id);
        }
    }

    @Override
    public void detach() {
        value = null;
    }
}
