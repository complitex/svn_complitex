package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.exception.*;
import org.complitex.osznconnection.file.service.executor.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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
@Stateless(name = "LoadTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
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

    protected void execute(RequestFileGroup requestFileGroup) throws ExecuteException, AbstractSkippedException {
        try {
            requestFileGroupBean.save(requestFileGroup);

            requestFileGroup.updateGroupId(); //устанавливаем идентификатор группы

            load(requestFileGroup.getPaymentFile());

            if (requestFileGroup.getPaymentFile().getStatus().equals(RequestFile.STATUS.LOADED)){
                load(requestFileGroup.getBenefitFile());
            }else{
                requestFileGroup.setBenefitFile(null);
            }
        } finally {
            requestFileGroupBean.clearEmptyGroup();
        }        
    }

    @SuppressWarnings({"EjbProhibitedPackageUsageInspection", "ConstantConditions", "ThrowableInstanceNeverThrown"})
    private void load(RequestFile requestFile) throws ExecuteException, AbstractSkippedException {
        String currentFieldName = "-1";
        int index = 0;
        int batchSize = configBean.getInteger(Config.LOAD_RECORD_BATCH_SIZE, true);

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
                        throw new AlreadyLoadedException(requestFile);
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
                request.setStatus(RequestStatus.CITY_UNRESOLVED_LOCALLY);

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
            requestFile.setLoaded(DateUtil.getCurrentDate());
            requestFileBean.updateStatus(requestFile, LOADED);
            log.info("Файл успешно загружен {}", requestFile.getName());
            info(requestFile, "Файл успешно загружен {0}", requestFile.getName());
        }catch (AlreadyLoadedException e) {
            executionSkip(new LoadSkippedException(e, requestFile), SKIPPED, ALREADY_LOADED);
        } catch (FieldNotFoundException e){
            executionError(new LoadException(e, requestFile, index, currentFieldName), LOAD_ERROR, FIELD_NOT_FOUND);
        } catch (FieldWrongTypeException e) {
            executionError(new LoadException(e, requestFile, index, currentFieldName), LOAD_ERROR, FIELD_WRONG_TYPE);
        } catch (FieldWrongSizeException e) {
            executionError(new LoadException(e, requestFile, index, currentFieldName), LOAD_ERROR, FIELD_WRONG_SIZE);
        }  catch (SqlSessionException e){
            executionError(new LoadException(e, requestFile, index, currentFieldName), LOAD_ERROR, SQL_SESSION);
        } catch (DBFException e){
            executionError(new LoadException(e, requestFile, index, currentFieldName), LOAD_ERROR, DBF);
        } catch (Exception e){
            executionError(new LoadException(e, requestFile, index, currentFieldName), LOAD_ERROR, CRITICAL);
        }
    }
}
