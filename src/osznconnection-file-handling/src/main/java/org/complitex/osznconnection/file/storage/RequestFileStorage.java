package org.complitex.osznconnection.file.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:18:43
 */
public class RequestFileStorage {
    private static final Logger log = LoggerFactory.getLogger(RequestFileStorage.class);
    public final static String PROCESSING_CONFIG = "file-handling.properties";

    private static RequestFileStorage instance;
    private String rootDir;

    private RequestFileStorage() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROCESSING_CONFIG);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            rootDir = properties.getProperty("oszn_request_dir");
        } catch (IOException e) {
            log.error("Файл настроек " + PROCESSING_CONFIG + " не найден", e);
        }
    }

    public static RequestFileStorage getInstance() {
        if (instance == null){
            instance = new RequestFileStorage();
        }

        return instance;
    }

    /**
     * @param child дочерняя директория
     * @param filter фильтр
     * @return Список файлов в дочерней директории корневой директории файлового хранилища
     */
    public List<File> getFiles(String child, FilenameFilter filter){
        List<File> files = new ArrayList<File>();

        addFiles(files, new File(rootDir, child), filter);

        return files;
    }

    private void addFiles(List<File> list, File dir, FilenameFilter filter){
        for(File file: dir.listFiles(filter)){
            if (file.isDirectory()){
                addFiles(list, file, filter);
            }else{
                list.add(file);
            }
        }
    }
}