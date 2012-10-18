/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.template.web.template.TemplateWebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class MainUserOrganizationPickerFactory {

    private static final Logger log = LoggerFactory.getLogger(MainUserOrganizationPickerFactory.class);

    public static Component create(String id, IModel<DomainObject> model) {
        Class<? extends Component> mainUserOrganizationPickerClass =
                TemplateWebApplication.getMainUserOrganizationPickerComponentClass();
        try {
            return mainUserOrganizationPickerClass.getConstructor(String.class, IModel.class).newInstance(id, model);
        } catch (Exception e) {
            log.warn("Couldn't to instantiate main user organization picker component. Default one will be used.", e);
            return new MainUserOrganizationPicker(id, model);
        }
    }

    private MainUserOrganizationPickerFactory() {
    }
}
