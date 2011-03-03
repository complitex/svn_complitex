package org.complitex.dictionary.util;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;

import java.io.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 19:01
 */
public class ImportStorageUtil {
    public static CSVReader getCsvReader(String dir, IImportFile file) throws ImportFileNotFoundException {
        try {
            return new CSVReader(new FileReader(new File(dir, file.getFileName())), ',', '"', 1);
        } catch (FileNotFoundException e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        }
    }

    public static int getRecordCount(String dir, IImportFile file) throws ImportFileNotFoundException, ImportFileReadException {
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
