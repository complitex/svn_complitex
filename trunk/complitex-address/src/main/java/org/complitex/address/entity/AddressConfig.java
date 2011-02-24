package org.complitex.address.entity;

import org.complitex.dictionary.entity.IConfig;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.02.11 15:31
 */
public enum AddressConfig implements IConfig{
    ADDRESS_IMPORT_FILE_STORAGE_DIR("c:\\storage\\import");

    private String defaultValue;

    AddressConfig(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}
