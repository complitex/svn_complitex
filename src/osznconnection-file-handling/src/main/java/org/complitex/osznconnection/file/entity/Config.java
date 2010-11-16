package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum Config {
    LOAD_INPUT_FILE_STORAGE_DIR("C:\\storage\\in"),
    SAVE_OUTPUT_FILE_STORAGE_DIR("C:\\storage\\out"),

    LOAD_THREAD_SIZE("2"),
    BIND_THREAD_SIZE("4"),
    FILL_THREAD_SIZE("4"),
    SAVE_THREAD_SIZE("4"),

    LOAD_BATCH_SIZE("16"),
    BIND_BATCH_SIZE("64"),
    FILL_BATCH_SIZE("64"),

    LOAD_MAX_ERROR_COUNT("10"),
    BIND_MAX_ERROR_COUNT("10"),
    FILL_MAX_ERROR_COUNT("10"),
    SAVE_MAX_ERROR_COUNT("10");

    private String defaultValue;

    Config(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}

