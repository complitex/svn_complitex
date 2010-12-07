package org.complitex.osznconnection.file.service.process;


import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.12.10 14:59
 */
public class SaveUtil {
    private final static Logger log = LoggerFactory.getLogger(SaveUtil.class);

    private final static String FILE_ENCODING = "cp1251";
    private final static String RESULT_FILE_NAME = "Result";
    private final static String RESULT_FILE_EXT = ".txt";

    private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    private final static SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private final static ResourceBundle bundle = ResourceBundle.getBundle(SaveUtil.class.getName());

    public static void createResult(List<RequestFileGroup> processed)
            throws StorageNotFoundException {
        Map<String, List<RequestFileGroup>> catalog = new HashMap<String, List<RequestFileGroup>>();

        //sort groups by directory
        for (RequestFileGroup group : processed){
            List<RequestFileGroup> list = catalog.get(group.getDirectory());

            if (list == null){
                list = new ArrayList<RequestFileGroup>();
                catalog.put(group.getDirectory(), list);
            }

            list.add(group);
        }

        for (String directory : catalog.keySet()){
            writeDirectory(directory, catalog.get(directory));
        }
    }

    private static void writeDirectory(String directory, List<RequestFileGroup> groups) throws StorageNotFoundException {
        try{
            Date now = DateUtil.getCurrentDate();
            String name = RESULT_FILE_NAME + "_" + sdfFile.format(now) + ".txt";

            File file = RequestFileStorage.getInstance().createOutputFile(name, directory);

            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), FILE_ENCODING));

            writer.write("Время выгрузки: " + sdf.format(now));
            writer.write("\n\nКаталог: " + directory);

            int requestCount = 0;

            for (RequestFileGroup group : groups){
                int count = group.getPaymentFile().getDbfRecordCount();

                writer.write("\n" + group.getPaymentFile().getName() + ", " + group.getBenefitFile().getName()
                        + " - Запросов: " + count);
                writeErrorStatus(group, writer);

                requestCount += count;
            }

            writer.write("\n\nВсего пар файлов: " + groups.size());
            writer.write("\nВсего запросов: " + requestCount);

            writer.close();
        } catch (IOException e) {
            log.error("Ошибка сохранения файла Result.txt", e);
        }
    }

    private static void writeErrorStatus(RequestFileGroup group, Writer fileWriter) throws IOException {
        List<AbstractRequest> payments = group.getPaymentFile().getRequests();

        if (payments != null) {
            for (AbstractRequest request : payments){
                if (!request.getStatus().equals(RequestStatus.PROCESSED)){

                        fileWriter.write("\n№" + request.getDbfFields().get(PaymentDBF.OWN_NUM_SR.name())
                                + " - "  + getString(request.getStatus().name()));
                }
            }
        }
    }

    private static String getString(String key){
        if (bundle.containsKey(key)){
            return bundle.getString(key);
        }

        return key;
    }
}
