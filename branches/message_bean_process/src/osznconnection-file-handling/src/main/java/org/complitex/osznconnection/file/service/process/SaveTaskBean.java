package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.complitex.osznconnection.file.storage.RequestFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.SAVED;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.SAVE_ERROR;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS_DETAIL.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 17:29:53
 */
public class SaveTaskBean extends AbstractTaskBean{
    private static final Logger log = LoggerFactory.getLogger(SaveTaskBean.class);

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

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

    @Override
    protected void execute(RequestFileGroup group) {
        save(group.getPaymentFile());
        save(group.getBenefitFile());
    }

    private void save(RequestFile requestFile){
        DBFWriter writer = null;

        try {
            //устанавливаем абсолютный путь для сохранения файла запроса
            File file = RequestFileStorage.getInstance().createOutputFile(requestFile.getName(), requestFile.getDirectory());
            requestFile.setAbsolutePath(file.getAbsolutePath());

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
            List<AbstractRequest> rows;
            try {
                rows = getAbstractRequests(requestFile);
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            for (AbstractRequest abstractRequest : rows) {
                Object[] rowData = new Object[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    rowData[i] = abstractRequest.getDbfFields().get(fields[i].getName());
                }

                writer.addRecord(rowData);
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
        } catch (Exception e) {
            if (writer != null) {
                writer.rollback();
            }
            requestFile.setStatus(SAVE_ERROR, CRITICAL);
            log.error("Критическая ошибка загрузки файла " + requestFile.getAbsolutePath(), e);
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
    }
}
