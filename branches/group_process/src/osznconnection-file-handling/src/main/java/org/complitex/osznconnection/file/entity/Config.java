package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum Config {
        LOAD_INPUT_FILE_STORAGE_DIR("C:\\storage\\in"),
        SAVE_OUTPUT_FILE_STORAGE_DIR("C:\\storage\\out"),

        LOAD_THREADS_SIZE("2"),
        BIND_THREADS_SIZE("10"),
        FILL_THREADS_SIZE("10"),
        SAVE_THREADS_SIZE("2"),

        LOAD_RECORD_BATCH_SIZE("32"),
        BIND_RECORD_BATCH_SIZE("64"),
        FILL_RECORD_BATCH_SIZE("64"),

        LOAD_MAX_ERROR_COUNT("10"),
        BIND_MAX_ERROR_COUNT("10"),
        SAVE_MAX_ERROR_COUNT("10"),
        FILL_MAX_ERROR_COUNT("10");
    private String defaultValue;

    Config(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
