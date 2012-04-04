package org.complitex.template.web.security;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:23:40
 */
public final class SecurityRole {

    public final static String AUTHORIZED = "AUTHORIZED";
    public final static String INFO_PANEL_ALLOWED = "INFO_PANEL_ALLOWED";
    public static final String ADDRESS_MODULE_EDIT = "ADDRESS_MODULE_EDIT";
    public static final String ADDRESS_MODULE_VIEW = "ADDRESS_MODULE_VIEW";
    public static final String APARTMENT_EDIT = "APARTMENT_EDIT";
    public static final String ROOM_EDIT = "ROOM_EDIT";
    public static final String ORGANIZATION_MODULE_EDIT = "ORGANIZATION_MODULE_EDIT";
    public static final String ORGANIZATION_MODULE_VIEW = "ORGANIZATION_MODULE_VIEW";
    public static final String OWNERSHIP_MODULE_EDIT = "OWNERSHIP_MODULE_EDIT";
    public static final String OWNERSHIP_MODULE_VIEW = "OWNERSHIP_MODULE_VIEW";
    public static final String PRIVILEGE_MODULE_EDIT = "PRIVILEGE_MODULE_EDIT";
    public final static String ADMIN_MODULE_EDIT = "ADMIN_MODULE_EDIT";
    /*
     * Pps office roles.
     */
    public static final String PERSON_MODULE_EDIT = "PERSON_MODULE_EDIT";
    public static final String PERSON_MODULE_VIEW = "PERSON_MODULE_VIEW";
    public static final String PERSON_MODULE_DESCRIPTION_EDIT = "PERSON_MODULE_DESCRIPTION_EDIT";
    public static final String REFERENCE_DATA_MODULE_EDIT = "REFERENCE_DATA_MODULE_EDIT";
    public static final String REFERENCE_DATA_MODULE_VIEW = "REFERENCE_DATA_MODULE_VIEW";

    private SecurityRole() {
    }
}
