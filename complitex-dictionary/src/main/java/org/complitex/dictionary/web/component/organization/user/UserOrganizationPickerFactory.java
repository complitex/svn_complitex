package org.complitex.dictionary.web.component.organization.user;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.dictionary.web.component.factory.WebComponentFactoryUtil;
import org.slf4j.LoggerFactory;

public class UserOrganizationPickerFactory {
    public static final String WEB_COMPONENT_NAME = "UserOrganizationPickerFactory";

    public static Panel create(String id, IModel<Long> userOrganizatioIdModel, UserOrganizationPickerParameters parameters) {
        Class<? extends Panel> userOrganizationPickerClass =
                (Class) WebComponentFactoryUtil.getComponentClass(WEB_COMPONENT_NAME);
        try {
            return userOrganizationPickerClass.getConstructor(
                    String.class, IModel.class, UserOrganizationPickerParameters.class).
                    newInstance(id, userOrganizatioIdModel, parameters);
        } catch (Exception e) {
            LoggerFactory.getLogger(UserOrganizationPickerFactory.class)
                    .warn("Couldn't to instantiate user organization picker. Default one will be used.", e);
            return new UserOrganizationPicker(id, userOrganizatioIdModel, parameters);
        }
    }

    private UserOrganizationPickerFactory() {
    }
}
