package org.complitex.dictionary.web.component.organization.user;

public class UserOrganizationPickerParameters {

    private boolean updating;

    public UserOrganizationPickerParameters(boolean updating) {
        this.updating = updating;
    }

    public boolean isUpdating() {
        return updating;
    }
}
