package org.complitex.dictionary.entity;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.util.ImportStorageUtil;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 16:40
 */
public abstract class AbstractImportService {
     @EJB
    private ConfigBean configBean;

    protected int getRecordCount(IImportFile file) throws ImportFileNotFoundException, ImportFileReadException {
        String dir = configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, false);

        return ImportStorageUtil.getRecordCount(dir, file);
    }

    protected CSVReader getCsvReader(IImportFile file) throws ImportFileNotFoundException {
        String dir = configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, false);

        return ImportStorageUtil.getCsvReader(dir, file);
    }
}
