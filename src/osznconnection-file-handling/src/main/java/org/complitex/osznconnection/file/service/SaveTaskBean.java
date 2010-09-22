package org.complitex.osznconnection.file.service;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.SAVED;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.SAVE_ERROR;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS_DETAIL.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.09.2010 14:38:36
 *
 * Асинхронная задача сохранения файла запроса.
 *
 * @see org.complitex.osznconnection.file.service.SaveRequestBean
 */
@Stateless(name = "SaveTaskBean")
public class SaveTaskBean {
    private static final Logger log = LoggerFactory.getLogger(SaveTaskBean.class);

    public static final int RECORD_PROCESS_DELAY = FileHandlingConfig.SAVE_RECORD_PROCESS_DELAY.getInteger();

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @Asynchronous
    @SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
    public Future<RequestFile> save(RequestFile requestFile) {
        DBFWriter writer = null;
        try {
            requestFile.setStatus(RequestFile.STATUS.SAVING);
            try {
                requestFileBean.save(requestFile);
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            //Удаляем файл есть такой есть
            RequestFileStorage.getInstance().delete(requestFile.getAbsolutePath());

            writer = new DBFWriter(RequestFileStorage.getInstance().createFile(requestFile.getAbsolutePath(), true));
            writer.setCharactersetName("cp866");

            //Создание полей
            DBFField[] fields = getDbfField(requestFile.getType());
            writer.setFields(fields);

            //Сохранение строк
            List<AbstractRequest> rows = getAbstractRequests(requestFile);

            for (AbstractRequest abstractRequest : rows) {
                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    rowData[i] = abstractRequest.getDbfFields().get(fields[i].getName());
                }

                writer.addRecord(rowData);

                //debug delay
                if (RECORD_PROCESS_DELAY > 0) {
                    try {
                        Thread.sleep(RECORD_PROCESS_DELAY);
                    } catch (InterruptedException e) {
                        //hoh...
                    }
                }
            }

            //Выгрузка завершена
            writer.write();
            requestFile.setStatus(SAVED);
        } catch (DBFException e) {
            if (writer != null) {
                writer.rollback();
            }
            requestFile.setStatus(SAVE_ERROR, DBF);
            log.error("Ошибка выгрузки файла " + requestFile.getName(), e);
            error(requestFile, "Ошибка выгрузки файла {0}. {1}", requestFile.getName(), e.getMessage());
        } catch (SqlSessionException e) {
            if (writer != null) {
                writer.rollback();
            }
            requestFile.setStatus(SAVE_ERROR, SQL_SESSION);
            log.error("Ошибка сохранения в базу данных при обработке файла " + requestFile.getAbsolutePath(), e);
            error(requestFile, "Ошибка сохранения в базу данных файла {0}. {1}", requestFile.getName(), e.getMessage());
        } catch (Throwable t) {
            if (writer != null) {
                writer.rollback();
            }
            requestFile.setStatus(SAVE_ERROR, CRITICAL);
            log.error("Критическая ошибка загрузки файла " + requestFile.getAbsolutePath(), t);
            error(requestFile, "Критическая ошибка загрузки файла {0}", requestFile.getName());
        }

        try {
            requestFileBean.save(requestFile);
        } catch (Exception e) {
            log.error("Ошибка сохранения в базу данных  при обработке файла " + requestFile.getAbsolutePath(), e);
            error(requestFile, "Ошибка сохранения в базу данных  при обработке файла {0}. {1}", requestFile.getName(), e.getMessage());
        }

        if (requestFile.getStatus() == SAVED){
            log.info("Файл {} выгружен успешно", requestFile.getName());
            info(requestFile, "Файл {0} выгружен успешно", requestFile.getName());
        }

        return new AsyncResult<RequestFile>(requestFile);
    }

    private byte getDataType(Class type){
        if (type.equals(String.class)){
            return DBFField.FIELD_TYPE_C;
        }else if (type.equals(Integer.class) || type.equals(BigDecimal.class)){
            return DBFField.FIELD_TYPE_N;
        }else if (type.equals(Date.class)){
            return DBFField.FIELD_TYPE_D;
        }

        throw  new IllegalArgumentException(type.toString());
    }

    private DBFField[] getDbfField(RequestFile.TYPE type){
        DBFField[] dbfFields;

        switch (type){
            case BENEFIT:
                BenefitDBF[] benefitDBFs = BenefitDBF.values();
                dbfFields = new DBFField[benefitDBFs.length];

                for (int i = 0; i < benefitDBFs.length; ++i){
                    BenefitDBF benefitDBF = benefitDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(benefitDBF.name());
                    dbfFields[i].setDataType(getDataType(benefitDBF.getType()));
                    if (!benefitDBF.getType().equals(Date.class)){
                        dbfFields[i].setFieldLength(benefitDBF.getLength());
                        dbfFields[i].setDecimalCount(benefitDBF.getScale());
                    }
                }

                return dbfFields;
            case PAYMENT:
                PaymentDBF[] paymentDBFs = PaymentDBF.values();
                dbfFields = new DBFField[paymentDBFs.length];

                for (int i = 0; i < paymentDBFs.length; ++i){
                    PaymentDBF paymentDBF = paymentDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(paymentDBF.name());
                    dbfFields[i].setDataType(getDataType(paymentDBF.getType()));
                    if (!paymentDBF.getType().equals(Date.class)) {
                        dbfFields[i].setFieldLength(paymentDBF.getLength());
                        dbfFields[i].setDecimalCount(paymentDBF.getScale());
                    }
                }

                return dbfFields;
            default: throw new IllegalArgumentException(type.name());
        }
    }

    public List<AbstractRequest> getAbstractRequests(RequestFile requestFile){
        switch (requestFile.getType()){
            case BENEFIT:
                return benefitBean.getBenefits(requestFile);
            case PAYMENT:
                return paymentBean.getPayments(requestFile);
            default: throw new IllegalArgumentException(requestFile.getType().name());
        }
    }

    private void info(RequestFile requestFile, String decs, Object... args){
        logBean.info(
                Module.NAME,
                SaveTaskBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFile.getLogChangeList(),
                decs,
                args);
    }

    private void error(RequestFile requestFile, String decs, Object... args){
        logBean.error(
                Module.NAME,
                SaveTaskBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFile.getLogChangeList(),
                decs,
                args);
    }
}
