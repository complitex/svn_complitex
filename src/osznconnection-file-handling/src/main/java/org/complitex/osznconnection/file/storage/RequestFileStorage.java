package org.complitex.osznconnection.file.storage;

import org.complitex.osznconnection.file.service.FileHandlingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:18:43
 */
public class RequestFileStorage {
    private static final Logger log = LoggerFactory.getLogger(RequestFileStorage.class);

    private static RequestFileStorage instance;

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
     * @throws StorageNotFoundException Директория не найдена
     */
    public List<File> getInputFiles(String child, FilenameFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();

        File dir = new File(FileHandlingConfig.LOAD_INPUT_FILE_STORAGE_DIR.getString(), child);

        if (!dir.exists()){
            throw new StorageNotFoundException("Директория входящих файлов запросов " + dir.getAbsolutePath() + " не найдена");
        }

        addFiles(files, dir, filter);

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

    public void checkOutputFileStorageExists() throws StorageNotFoundException {
        File parent = new File(FileHandlingConfig.SAVE_OUTPUT_FILE_STORAGE_DIR.getString());

        //Желательно чтобы директория для исходящих файлов запроса уже была создана
        if (!parent.exists()){
            throw new StorageNotFoundException("Директория для исходящих файлов запросов "
                    + parent.getAbsolutePath() + " не найдена");
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public File createOutputFile(String name, String child) throws StorageNotFoundException {
        checkOutputFileStorageExists();

        //Создаем директорию с именем кода района если что
        File dir = new File(FileHandlingConfig.SAVE_OUTPUT_FILE_STORAGE_DIR.getString(), child);
        if (!dir.exists()){
            dir.mkdir();
        }

        return new File(dir, name);
    }
}