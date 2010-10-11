package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.*;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS_DETAIL.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 18:49:12
 */
@Stateless(name = "LoadTaskBean2")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LoadTaskBean extends AbstractTaskBean{
    private static final Logger log = LoggerFactory.getLogger(LoadTaskBean.class);


    private Class getType(byte dataType, int scale){
        switch (dataType){
            case DBFField.FIELD_TYPE_C: return String.class;
            case DBFField.FIELD_TYPE_N: return scale == 0 ? Integer.class : BigDecimal.class;
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
            case TARIF:
                values = TarifDBF.values();
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
            case BENEFIT: return new Benefit();
            case PAYMENT: return new Payment();
            case TARIF: return new Tarif();
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
            case TARIF:
                tarifBean.insert(abstractRequests);
                break;
            default:
                throw new IllegalArgumentException(requestFileType.name());
        }
    }

    protected void execute(RequestFileGroup requestFileGroup) {
        requestFileGroupBean.save(requestFileGroup);

        load(requestFileGroup.getPaymentFile());

        if (!requestFileGroup.getPaymentFile().getStatus().equals(RequestFile.STATUS.LOADED)){
            load(requestFileGroup.getBenefitFile());
        }

        requestFileGroupBean.clearEmptyGroup();
    }

    @SuppressWarnings({"EjbProhibitedPackageUsageInspection", "ConstantConditions"})
    private void load(RequestFile requestFile){
        String currentFieldName = "-1";
        int index = 0;
        int batchSize = configBean.getInteger(ConfigName.LOAD_RECORD_BATCH_SIZE, true);

        try {
            //Инициализация парсера
            DBFReader reader = new DBFReader(new FileInputStream(requestFile.getAbsolutePath()));
            reader.setCharactersetName("cp866");

            //Начало загрузки
            requestFile.setDbfRecordCount(reader.getRecordCount());
            requestFile.setLoaded(DateUtil.getCurrentDate());
            requestFile.setStatus(LOADING);

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
                AbstractRequest request = createObject(requestFile.getType());

                //Заполнение колонок записи
                for (int i=0; i < rowObjects.length; ++i) {
                    Object value = rowObjects[i];

                    if (value != null && value instanceof String){
                        value = ((String)value).trim(); //string trim
                    }

                    DBFField field = reader.getField(i);

                    currentFieldName = field.getName();
                    request.setField(currentFieldName, value, getType(field.getDataType(), field.getDecimalCount()));
                }

                //обработка первой строки
                if (index++ == 0){
                    //установка номера реестра
                    Integer registry = (Integer) request.getDbfFields().get(PaymentDBF.REE_NUM.name());
                    if (registry != null){
                        requestFile.setRegistry(registry);
                    }

                    //проверка загружен ли файл
                    if (requestFileBean.checkLoaded(requestFile)){
                        throw new AlreadyLoadedException();
                    }

                    //сохранение
                    try {
                        requestFileBean.save(requestFile);
                    } catch (Exception e) {
                        throw new SqlSessionException(e);
                    }
                }

                request.setRequestFileId(requestFile.getId());
                request.setOrganizationId(requestFile.getOrganizationId());
                request.setStatus(Status.CITY_UNRESOLVED_LOCALLY);

                batch.add(request);

                //Сохранение
                if (batch.size() > batchSize){
                    try {
                        save(requestFile.getType(), batch);
                    } catch (Exception e) {
                        throw new SqlSessionException(e);
                    }
                    batch.clear();
                }
            }
            try {
                if (!batch.isEmpty()){
                    save(requestFile.getType(), batch);
                }
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            //Загрузка завершена
            requestFile.setStatus(LOADED);
        }catch (FieldNotFoundException e){
            requestFile.setStatus(LOAD_ERROR, FIELD_NOT_FOUND);
            log.error("Поле не найдено " + requestFile.getAbsolutePath() + ", строка: " + index + ", колонка " + currentFieldName, e);
            error(requestFile, "Поле {0} не найдено в файле {1}, строка: {2}, колонка: {3}", e.getMessage(), requestFile.getName(), index, currentFieldName);
        } catch (FieldWrongTypeException e) {
            requestFile.setStatus(LOAD_ERROR, FIELD_WRONG_TYPE);
            log.error("Недопустимый тип поля " + requestFile.getAbsolutePath()+ ", колонка: " + currentFieldName, e);
            error(requestFile, "Неверный тип поля {0} в файле {1}, колонка: {2}", e.getMessage(), requestFile.getName(), currentFieldName);
        } catch (FieldWrongSizeException e) {
            requestFile.setStatus(LOAD_ERROR, FIELD_WRONG_SIZE);
            log.error("Недопустимый размер поля " + requestFile.getAbsolutePath()+ ", строка: " + index, e);
            error(requestFile, "Неверный размер поля {0} в файле {1}, строка: {2}, колонка: {3}",
                    e.getMessage(), requestFile.getName(), index, currentFieldName);
        } catch (AlreadyLoadedException e) {
            requestFile.setStatus(SKIPPED, ALREADY_LOADED);
            log.warn("Файл уже загружен {}", requestFile.getAbsolutePath());
            info(requestFile, "Файл уже загружен {0}", requestFile.getName());
        } catch (SqlSessionException e){
            requestFile.setStatus(LOAD_ERROR, SQL_SESSION);
            log.error("Ошибка сохранения в базу данных при обработке файла " + requestFile.getAbsolutePath() + ", строка: " + index, e);
            error(requestFile, "Ошибка сохранения в базу данных файла {0}. {1}, строка: {2}", requestFile.getName(), e.getMessage(), index);
        } catch (DBFException e){
            requestFile.setStatus(LOAD_ERROR, DBF);
            log.error("Ошибка формата файла " + requestFile.getAbsolutePath()+ ", строка: " + index, e);
            error(requestFile, "Ошибка формата файла {0} {1}, строка: {2}", requestFile.getName(), e.getMessage(), index, currentFieldName);
        } catch (Throwable t){
            requestFile.setStatus(LOAD_ERROR, CRITICAL);
            log.error("Критическая ошибка загрузки файла " + requestFile.getAbsolutePath()+ ", строка: " + index + ", колонка: " + currentFieldName, t);
            error(requestFile, "Критическая ошибка загрузки файла {0}, строка: {1}, колонка: {2}", requestFile.getName(), index, currentFieldName);
        } finally {
            if (requestFile.getStatusDetail() != ALREADY_LOADED) {
                requestFile.setLoaded(DateUtil.getCurrentDate());

                try {
                    if (requestFile.getId() != null) { //update status
                        requestFileBean.save(requestFile);
                    }
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
    }
}
