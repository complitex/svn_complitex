package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 07.04.2014 20:57
 */
public class ImportDistrictLinkException extends AbstractException {
    public ImportDistrictLinkException(String districtName, String buildingDistrictName, String streetName, String buildingName,String fileName, int index) {
        super("Неверный район {0} у существующего дома {1} {2} {3} при импорте файла {4} строки {5}",
                districtName, buildingDistrictName, streetName, buildingName, index, fileName);
    }
}
