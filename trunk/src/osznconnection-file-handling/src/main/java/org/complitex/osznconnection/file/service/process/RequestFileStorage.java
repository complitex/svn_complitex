package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.Config;
import org.complitex.osznconnection.file.service.ConfigStatic;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:18:43
 *
 * Класс одиночка для работы с файловой системой.
 * Поиск, сохранение, удаление.
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
     * @throws org.complitex.osznconnection.file.service.exception.StorageNotFoundException Директория не найдена
     */
    public List<File> getInputFiles(String child, FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();

        File dir = new File(ConfigStatic.get().getString(Config.LOAD_INPUT_FILE_STORAGE_DIR, true), child);

        if (!dir.exists()){
            throw new StorageNotFoundException(dir.getAbsolutePath());
        }

        addFiles(files, dir, filter);

        return files;
    }

    private void addFiles(List<File> list, File dir, FileFilter filter){
        for(File file: dir.listFiles(filter)){
            if (file.isDirectory()){
                addFiles(list, file, filter);
            }else{
                list.add(file);
            }
        }
    }

    public void checkOutputFileStorageExists() throws StorageNotFoundException {
        File parent = new File(ConfigStatic.get().getString(Config.SAVE_OUTPUT_FILE_STORAGE_DIR, true));

        //Желательно чтобы директория для исходящих файлов запроса уже была создана
        if (!parent.exists()){
            throw new StorageNotFoundException(parent.getAbsolutePath());
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public File createOutputFile(String name, String child) throws StorageNotFoundException {
        checkOutputFileStorageExists();

        //Создаем директорию с именем кода района если что
        File dir = new File(ConfigStatic.get().getString(Config.SAVE_OUTPUT_FILE_STORAGE_DIR, false), child);
        if (!dir.exists()){
            dir.mkdir();
        }

        return new File(dir, name);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public File createFile(String path, boolean replace){
        File file = new File(path);
        if (replace && file.exists()){
            file.delete();
        }

        return file;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void delete(String path){
       new File(path).delete();
    }
    
    public String getRelativeParent(File file){
        File root = new File(ConfigStatic.get().getString(Config.LOAD_INPUT_FILE_STORAGE_DIR, false));
        String absolutePath = file.getParent();

        return absolutePath.substring(root.getAbsolutePath().length());
    }
}