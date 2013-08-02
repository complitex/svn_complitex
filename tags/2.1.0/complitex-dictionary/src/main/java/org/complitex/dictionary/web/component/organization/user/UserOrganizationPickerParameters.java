/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.organization.user;

/**
 *
 * @author Artem
 */
public class UserOrganizationPickerParameters {

    private boolean updating;

    public UserOrganizationPickerParameters(boolean updating) {
        this.updating = updating;
    }

    public boolean isUpdating() {
        return updating;
    }
}
