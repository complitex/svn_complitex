package org.complitex.address.entity;

import org.complitex.dictionary.entity.IImportFile;

/**
* @author Anatoly A. Ivanov java@inheaven.ru
*         Date: 22.02.11 16:05
*/
public enum AddressImportFile implements IImportFile{
    COUNTRY("country.csv"),
    REGION("region.csv"),
    CITY_TYPE("city_type.csv"),
    CITY("city.csv"),
    DISTRICT("district.csv"),
    STREET_TYPE("street_type.csv"),
    STREET("street.csv"),
    BUILDING("building.csv");

    private String fileName;

    AddressImportFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
