/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.util;

import java.util.Locale;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public final class BuildingFormatter {

    private static final String RESOURCE_BUNDLE = BuildingFormatter.class.getName();

    private BuildingFormatter() {
    }

    public static String getBuilding(String buildingNumber, String buildingCorp, Locale locale) {
        StringBuilder buildingBuilder = new StringBuilder();
        if (!Strings.isEmpty(buildingNumber)) {
            buildingBuilder.append(buildingNumber);
            if (!Strings.isEmpty(buildingCorp)) {
                buildingBuilder.append(ResourceUtil.getString(RESOURCE_BUNDLE, "building_corp", locale)).append(" ").append(buildingCorp);
            }
        }
        return buildingBuilder.toString();
    }
}
