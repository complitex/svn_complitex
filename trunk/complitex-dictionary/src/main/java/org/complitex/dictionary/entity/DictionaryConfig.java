package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 16:31
 */
public enum DictionaryConfig implements IConfig{
    IMPORT_FILE_STORAGE_DIR("c:\\storage\\import"), SYNC_DATA_SOURCE("jdbc/osznconnectionRemoteResource");

    private String defaultValue;

    DictionaryConfig(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getGroupKey() {
        return "import";
    }
}
