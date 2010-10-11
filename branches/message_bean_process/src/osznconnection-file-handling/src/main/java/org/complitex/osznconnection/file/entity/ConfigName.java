package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum ConfigName {    
        LOAD_INPUT_FILE_STORAGE_DIR("C:\\storage\\in"),
        SAVE_OUTPUT_FILE_STORAGE_DIR("C:\\storage\\out"),
        LOAD_THREADS_SIZE("4"),
        SAVE_THREADS_SIZE("4"),
        LOAD_MAX_ERROR_FILE_COUNT("10"),
        SAVE_MAX_ERROR_FILE_COUNT("10"),
        LOAD_RECORD_BATCH_SIZE("64"),
        BIND_RECORD_BATCH_SIZE("64"),
        BIND_THREAD_SIZE("10"),
        FILL_RECORD_BATCH_SIZE("64"),
        FILL_THREAD_SIZE("10");

    private String defaultValue;

    ConfigName(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
