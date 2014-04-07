package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 07.04.2014 20:57
 */
public class ImportDistrictLinkException extends AbstractException {
    public ImportDistrictLinkException(String districtName, String streetName, String buildingName,String fileName, int index) {
        super("Неверное соответствие района {0} у существующего дома {1} {2} при импорте файла {3} строки {4}",
                districtName, streetName, buildingName, index, fileName);
    }
}
