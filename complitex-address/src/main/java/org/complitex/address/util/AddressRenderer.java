package org.complitex.address.util;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.util.StringUtil;

import java.util.Locale;

/**
 *
 * @author Artem
 */
public final class AddressRenderer {

    private static final String RESOURCE_BUNDLE = AddressRenderer.class.getName();

    private AddressRenderer() {
    }

    public static String displayBuilding(String buildingNumber, String buildingCorp, Locale locale) {
        String result = "";
        if (!Strings.isEmpty(buildingNumber)) {
            result = buildingNumber;
            if (!Strings.isEmpty(buildingCorp)) {
                result += " " + ResourceUtil.getString(RESOURCE_BUNDLE, "building_corp", locale) + " " + buildingCorp;
            }
        }
        return result;
    }

    public static String displayApartment(String apartment, Locale locale) {
        if (!Strings.isEmpty(apartment)) {
            return ResourceUtil.getString(RESOURCE_BUNDLE, "apartment", locale) + " " + apartment;
        }
        return "";
    }

    public static String displayRoom(String room, Locale locale) {
        if (!Strings.isEmpty(room)) {
            return ResourceUtil.getString(RESOURCE_BUNDLE, "room", locale) + " " + room;
        }
        return "";
    }

    public static String displayStreet(String streetType, String street, Locale locale) {
        if (Strings.isEmpty(streetType)) {
            return street;
        } else {
            return streetType + " " + street;
        }
    }

    public static String displayCity(String cityType, String city, Locale locale) {
        if (Strings.isEmpty(cityType)) {
            return city;
        } else {
            return cityType + " " + city;
        }
    }

    public static String displayDistrict(String district, Locale locale) {
        return StringUtil.valueOf(district);
    }

    public static String displayAddress(String streetType, String street, String buildingNumber, String buildingCorp, String apartment, Locale locale) {
        String displayStreet = displayStreet(streetType, street, locale);
        String displayBuilding = displayBuilding(buildingNumber, buildingCorp, locale);
        String displayApartment = displayApartment(apartment, locale);
        return displayStrings(displayStreet, displayBuilding, displayApartment);
    }

    public static String displayAddress(String streetType, String street, String buildingNumber, String buildingCorp, String apartment, String room, Locale locale) {
        String displayStreet = displayStreet(streetType, street, locale);
        String displayBuilding = displayBuilding(buildingNumber, buildingCorp, locale);
        String displayApartment = displayApartment(apartment, locale);
        String displayRoom = displayRoom(room, locale);
        return displayStrings(displayStreet, displayBuilding, displayApartment, displayRoom);
    }

    public static String displayAddress(String cityType, String city, String streetType, String street, String buildingNumber,
            String buildingCorp, String apartment, Locale locale) {
        String displayCity = displayCity(cityType, city, locale);
        String displayStreet = displayStreet(streetType, street, locale);
        String displayBuilding = displayBuilding(buildingNumber, buildingCorp, locale);
        String displayApartment = displayApartment(apartment, locale);
        return displayStrings(displayCity, displayStreet, displayBuilding, displayApartment);
    }

    public static String displayAddress(String cityType, String city, String streetType, String street, String buildingNumber,
            String buildingCorp, String apartment, String room, Locale locale) {
        String displayCity = displayCity(cityType, city, locale);
        String displayStreet = displayStreet(streetType, street, locale);
        String displayBuilding = displayBuilding(buildingNumber, buildingCorp, locale);
        String displayApartment = displayApartment(apartment, locale);
        String displayRoom = displayRoom(room, locale);
        return displayStrings(displayCity, displayStreet, displayBuilding, displayApartment, displayRoom);
    }

    public static String displayAddress(String cityType, String city, String district, Locale locale) {
        String displayCity = displayCity(cityType, city, locale);
        String displayDistrict = displayDistrict(district, locale);
        return displayStrings(displayCity, displayDistrict);
    }

    private static String displayStrings(String... strings) {
        String result = "";
        for (String string : strings) {
            if (!Strings.isEmpty(string)) {
                if (!Strings.isEmpty(result)) {
                    result += ", " + string;
                } else {
                    result = string;
                }
            }
        }
        return result;
    }
}
