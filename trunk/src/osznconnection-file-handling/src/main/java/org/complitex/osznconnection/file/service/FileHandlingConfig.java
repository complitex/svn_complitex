package org.complitex.osznconnection.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Настройки модуля модуля взаимодействия с отделами соц. защиты населения
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.09.2010 18:35:55
 */
public enum FileHandlingConfig {
    LOAD_INPUT_FILE_STORAGE_DIR(".\\in", String.class),
    SAVE_OUTPUT_FILE_STORAGE_DIR(".\\out", String.class),
    LOAD_THREADS_SIZE(4, Integer.class),
    SAVE_THREADS_SIZE(4, Integer.class),
    LOAD_MAX_ERROR_FILE_COUNT(10, Integer.class),
    SAVE_MAX_ERROR_FILE_COUNT(10, Integer.class),
    LOAD_RECORD_BATCH_SIZE(10, Integer.class),    
    LOAD_RECORD_PROCESS_DELAY(0, Integer.class),
    SAVE_RECORD_PROCESS_DELAY(0, Integer.class),
    BINDING_THREAD_SIZE(10, Integer.class),
    PROCESSING_THREAD_SIZE(10, Integer.class);

    private Object value;
    private Class type;

    static{
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("file-handling.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            for (FileHandlingConfig p : FileHandlingConfig.values()){
                String s = properties.getProperty(p.name().toLowerCase());

                if (s != null){
                    if (p.getType().equals(Integer.class)){
                        try {
                            p.value = Integer.valueOf(s.trim());
                        } catch (NumberFormatException e) {
                            //heh...
                        }
                    }else{
                        p.value = s;
                    }
                }
            }

        } catch (IOException e) {
            Logger.getLogger(FileHandlingConfig.class.getName())
                    .log(Level.WARNING, "Файл настроек file-handling.properties не найден", e);
            
        }
    }

    FileHandlingConfig(Object value, Class type) {
        this.value = value;
        this.type = type;
    }

    public Object getObject() {
        return value;
    }

    public String getString(){
        return (String) value;
    }

    public Integer getInteger(){
        return (Integer) value;
    }

    public Class getType() {
        return type;
    }
}
