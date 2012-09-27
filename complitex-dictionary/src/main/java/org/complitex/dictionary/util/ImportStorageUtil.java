package org.complitex.dictionary.util;

import au.com.bytecode.opencsv.CSVReader;
import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.Table;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;

import java.io.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 19:01
 */
public final class ImportStorageUtil {

    private ImportStorageUtil() {
    }

    public static CSVReader getCsvReader(String dir, IImportFile file, String charsetName, char separator)
            throws ImportFileNotFoundException {
        try {
            return new CSVReader(new InputStreamReader(new FileInputStream(
                    new File(dir, file.getFileName())), charsetName), separator, '"', 1);
        } catch (Exception e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        }
    }

    public static Table getDbfTable(String dir, IImportFile file, String charsetName)
            throws ImportFileNotFoundException, ImportFileReadException {
        try {
            Table table = new Table(new File(dir, file.getFileName()), charsetName);
            table.open();

            return table;
        } catch (IOException e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        } catch (CorruptedTableException e) {
            throw new ImportFileReadException(e, file.getFileName(), -1);
        }
    }

    public static int getRecordCount(String dir, IImportFile file)
            throws ImportFileNotFoundException, ImportFileReadException {
        switch (file.getFileName().substring(file.getFileName().lastIndexOf('.') + 1).toLowerCase()){
            case "csv":
                return getCsvRecordCount(dir, file);
            case "dbf":
                return getDbfRecordCount(dir, file);
        }

        throw new IllegalArgumentException("File extension must be 'csv' or 'dbf");
    }

    public static int getCsvRecordCount (String dir, IImportFile file)
            throws ImportFileNotFoundException, ImportFileReadException {
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(new File(dir, file.getFileName())));

            String line;
            int index = -1;

            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    index++;
                }
            }

            return index;
        } catch (FileNotFoundException e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        } catch (IOException e) {
            throw new ImportFileReadException(e, file.getFileName(), -1);
        }
    }

    public static int getDbfRecordCount (String dir, IImportFile file)
            throws ImportFileNotFoundException, ImportFileReadException {
        try {
            Table table = new Table(new File(dir, file.getFileName()));
            table.open();

            return table.getRecordCount();
        } catch (CorruptedTableException e) {
            throw new ImportFileReadException(e, file.getFileName(), -1);
        } catch (IOException e) {
            throw new ImportFileNotFoundException(e, file.getFileName());
        }
    }

    public static String[] getFileList(String dir, final String extension){
        return new File(dir).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().contains("." + extension);
            }
        });
    }
}
