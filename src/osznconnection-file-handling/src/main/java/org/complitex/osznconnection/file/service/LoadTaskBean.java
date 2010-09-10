package org.complitex.osznconnection.file.service;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.exception.AlreadyLoadedException;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongTypeException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.*;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS_DETAIL.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.09.2010 15:36:11
 */
@Stateless(name = "LoadTaskBean")
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadTaskBean {
    private static final Logger log = LoggerFactory.getLogger(LoadTaskBean.class);

    public static final int BATCH_SIZE = FileHandlingConfig.LOAD_RECORD_BATCH_SIZE.getInteger();
    public static final int RECORD_PROCESS_DELAY = FileHandlingConfig.LOAD_RECORD_PROCESS_DELAY.getInteger();

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @Asynchronous
    public Future<RequestFile> load(RequestFile requestFile){
        try {
            //проверка загружен ли файл
            if (requestFileBean.isLoaded(requestFile)){
                throw new AlreadyLoadedException();
            }

            //Инициализация парсера
            DBFReader reader = new DBFReader(new FileInputStream(requestFile.getAbsolutePath()));
            reader.setCharactersetName("cp866");

            //Начало загрузки
            requestFile.setDbfRecordCount(reader.getRecordCount());
            requestFile.setLoaded(DateUtil.getCurrentDate());
            requestFile.setStatus(LOADING);

            try {
                requestFileBean.save(requestFile);
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            List<String> fieldIndex = new ArrayList<String>();

            for(int i = 0; i < reader.getFieldCount(); i++) {
                DBFField field = reader.getField(i);

                fieldIndex.add(field.getName());
            }

            //проверка наличия всех полей в файле
            checkExist(requestFile.getType(), fieldIndex);

            Object[] rowObjects;

            List<AbstractRequest> batch = new ArrayList<AbstractRequest>();

            while((rowObjects = reader.nextRecord()) != null) {
                AbstractRequest r = createObject(requestFile.getType());

                r.setRequestFileId(requestFile.getId());
                r.setOrganizationId(requestFile.getOrganizationObjectId());
                r.setStatus(Status.CITY_UNRESOLVED_LOCALLY);

                batch.add(r);

                //Заполнение колонок записи
                for (int i=0; i < rowObjects.length; ++i) {
                    DBFField field = reader.getField(i);
                    r.setField(field.getName(), rowObjects[i], getType(field.getDataType(), field.getDecimalCount()));
                }

                //Сохранение
                if (batch.size() > BATCH_SIZE){
                    try {
                        save(requestFile.getType(), batch);
                    } catch (Exception e) {
                        throw new SqlSessionException(e);
                    }
                    batch.clear();
                }

                //debug delay
                if(RECORD_PROCESS_DELAY > 0){
                    try {
                        Thread.sleep(RECORD_PROCESS_DELAY);
                    } catch (InterruptedException e) {
                        //hoh...
                    }
                }
            }
            try {
                save(requestFile.getType(), batch);
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            //Загрузка завершена
            requestFile.setStatus(LOADED);
        }catch (FieldNotFoundException e){
            requestFile.setStatus(LOAD_ERROR, FIELD_NOT_FOUND);
            log.error("Поле не найдено " + requestFile.getAbsolutePath(), e);
            error(requestFile, "Поле {0} не найдено в файле {1}", e.getMessage(), requestFile.getName());
        } catch (FieldWrongTypeException e) {
            requestFile.setStatus(LOAD_ERROR, FIELD_WRONG_TYPE);
            log.error("Неверный тип поля " + requestFile.getAbsolutePath(), e);
            error(requestFile, "Неверный тип поля {0} в файле {1}", e.getMessage(), requestFile.getName());
        } catch (AlreadyLoadedException e) {
            requestFile.setStatus(LOAD_ERROR, ALREADY_LOADED);
            log.warn("Файл уже загружен {}", requestFile.getAbsolutePath());
            error(requestFile, "Файл уже загружен {0}", requestFile.getName());
        } catch (SqlSessionException e){
            requestFile.setStatus(LOAD_ERROR, SQL_SESSION);
            log.error("Ошибка сохранения в базу данных при обработке файла " + requestFile.getAbsolutePath(), e);
            error(requestFile, "Ошибка сохранения в базу данных файла {0}. {1}", requestFile.getName(), e.getMessage());
        } catch (DBFException e){
            requestFile.setStatus(LOAD_ERROR, DBF);
            log.error("Ошибка формата файла " + requestFile.getAbsolutePath(), e);
            error(requestFile, "Ошибка формата файла {0} {1}", requestFile.getName(), e.getMessage());
        } catch (Throwable t){
            requestFile.setStatus(LOAD_ERROR, CRITICAL);
            log.error("Критическая ошибка загрузки файла " + requestFile.getAbsolutePath(), t);
            error(requestFile, "Критическая ошибка загрузки файла {0}", requestFile.getName());
        } finally {
            if (requestFile.getStatusDetail() != ALREADY_LOADED) {
                requestFile.setLoaded(DateUtil.getCurrentDate());

                try {
                    requestFileBean.save(requestFile);
                } catch (Exception e) {
                    log.error("Ошибка сохранения в базу данных при обработке файла " + requestFile.getAbsolutePath(), e);
                    error(requestFile, "Ошибка сохранения в базу данных файла {0}. {1}", requestFile.getName(), e.getMessage());
                }

                if (requestFile.getStatus() == LOADED){
                    log.info("Файл успешно загружен {}", requestFile.getName());
                    info(requestFile, "Файл успешно загружен {0}", requestFile.getName());
                }
            }
        }

        return new AsyncResult<RequestFile>(requestFile);
    }

    private Class getType(byte dataType, int scale){
        switch (dataType){
            case DBFField.FIELD_TYPE_C: return String.class;
            case DBFField.FIELD_TYPE_N: return scale == 0 ? Integer.class : Double.class;
            case DBFField.FIELD_TYPE_D: return Date.class;
            default: throw new IllegalArgumentException();
        }
    }

    private void checkExist(RequestFile.TYPE requestFileType, List<String> names) throws FieldNotFoundException {
        Enum[] values;

        switch (requestFileType){
            case BENEFIT:
                values = BenefitDBF.values();
                break;
            case PAYMENT:
                values = PaymentDBF.values();
                break;
            default:
                throw new IllegalArgumentException(requestFileType.name());
        }

        for (Enum v : values){
            if (!names.contains(v.name())){
                throw new FieldNotFoundException(v.name());
            }
        }
    }

    private AbstractRequest createObject(RequestFile.TYPE requestFileType){
        switch (requestFileType){
            case BENEFIT:
                return new Benefit();
            case PAYMENT:
                return new Payment();
            default:
                throw new IllegalArgumentException(requestFileType.name());
        }
    }

    private void save(RequestFile.TYPE requestFileType, List<AbstractRequest> abstractRequests){
        switch (requestFileType){
            case BENEFIT:
                benefitBean.insert(abstractRequests);
                break;
            case PAYMENT:
                paymentBean.insert(abstractRequests);
                break;
            default:
                throw new IllegalArgumentException(requestFileType.name());
        }
    }

    private void info(RequestFile requestFile, String decs, Object... args){
        logBean.info(
                Module.NAME,
                LoadRequestBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFileBean.getLogChangeList(requestFile),
                decs,
                args);
    }

    private void error(RequestFile requestFile, String decs, Object... args){
        logBean.error(
                Module.NAME,
                LoadRequestBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFileBean.getLogChangeList(requestFile),
                decs,
                args);
    }
}
