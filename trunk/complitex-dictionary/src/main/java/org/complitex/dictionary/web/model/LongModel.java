package org.complitex.dictionary.web.model;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;

/**
 * @author Pavel Sknar
 */
public class LongModel implements IModel<Long> {

    private IModel<String> model;

    public LongModel(IModel<String> model) {
        this.model = model;
    }

    @Override
    public Long getObject() {
        return StringUtils.isEmpty(model.getObject())? null : Long.parseLong(model.getObject());
    }

    @Override
    public void setObject(Long object) {
        model.setObject(object == null? null : String.valueOf(object));
    }

    @Override
    public void detach() {

    }
}
