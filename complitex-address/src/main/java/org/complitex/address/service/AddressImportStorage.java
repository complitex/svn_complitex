package org.complitex.address.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.entity.AddressConfig;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.02.11 19:02
 */
public class AddressImportStorage {
    private static final Logger log = LoggerFactory.getLogger(AddressImportStorage.class);

    private static AddressImportStorage addressImportStorage;

    public static AddressImportStorage getInstance(){
        if (addressImportStorage == null){
            addressImportStorage = new AddressImportStorage();
        }

        return addressImportStorage;
    }

    public CSVReader getCsvReader(AddressImportFile file) throws ImportFileNotFoundException {
        String dir = EjbBeanLocator.getBean(ConfigBean.class).getString(AddressConfig.ADDRESS_IMPORT_FILE_STORAGE_DIR, true);

        try {
            return new CSVReader(new FileReader(new File(dir, file.getFileName())), ',', '"', 1);
        } catch (FileNotFoundException e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        }
    }

    public int getRecordCount(AddressImportFile file) throws ImportFileNotFoundException, ImportFileReadException {
        String dir = EjbBeanLocator.getBean(ConfigBean.class).getString(AddressConfig.ADDRESS_IMPORT_FILE_STORAGE_DIR, true);

        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(new File(dir, file.getFileName())));

            String line;
            int index = -1;

            while ((line = reader.readLine()) != null){
                if (line.trim().length() > 0) index++;
            }

            return index;
        } catch (FileNotFoundException e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        } catch (IOException e) {
            throw new ImportFileReadException(e, file.getFileName(), -1);
        }
    }
}
