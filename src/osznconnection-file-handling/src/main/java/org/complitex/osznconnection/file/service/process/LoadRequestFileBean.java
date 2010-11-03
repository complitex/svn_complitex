package org.complitex.osznconnection.file.service.process;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.ConfigBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.LoadException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 16:03
 */
@Stateless(name = "LoadRequestFileBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class LoadRequestFileBean {
    public interface ILoadRequestFile{
        public Enum[] getFieldNames();
        public AbstractRequest newObject();
        public void save(List<AbstractRequest> batch);
    }

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @SuppressWarnings({"EjbProhibitedPackageUsageInspection", "ConstantConditions"})
    public boolean load(RequestFile requestFile, ILoadRequestFile loadRequestFile) throws ExecuteException {
        String currentFieldName = "-1";
        int index = -1;
        int batchSize = configBean.getInteger(Config.LOAD_BATCH_SIZE, true);

        requestFile.setLoadedRecordCount(0);

        try {
            //Инициализация парсера
            DBFReader reader = new DBFReader(new FileInputStream(requestFile.getAbsolutePath()));
            reader.setCharactersetName("cp866");

            //Начало загрузки
            requestFile.setDbfRecordCount(reader.getRecordCount());
            requestFile.setLoaded(DateUtil.getCurrentDate());

            List<String> fieldIndex = new ArrayList<String>();

            for(int i = 0; i < reader.getFieldCount(); i++) {
                DBFField field = reader.getField(i);

                fieldIndex.add(field.getName());
            }

            //проверка наличия всех полей в файле
            for (Enum field : loadRequestFile.getFieldNames()){
                if (!fieldIndex.contains(field.name())){
                    throw new FieldNotFoundException(field.name());
                }
            }

            Object[] rowObjects;

            List<AbstractRequest> batch = new ArrayList<AbstractRequest>();

            while((rowObjects = reader.nextRecord()) != null) {
                index++;

                AbstractRequest request = loadRequestFile.newObject();

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
                if (index == 0){
                    //установка номера реестра
                    Integer registry = (Integer) request.getDbfFields().get(PaymentDBF.REE_NUM.name());
                    if (registry != null){
                        requestFile.setRegistry(registry);
                    }

                    //проверка загружен ли файл
                    if (requestFileBean.checkLoaded(requestFile)){
                        return false;
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
                        loadRequestFile.save(batch);
                    } catch (Exception e) {
                        throw new SqlSessionException(e);
                    }
                    batch.clear();
                }
            }
            try {
                if (!batch.isEmpty()){
                    loadRequestFile.save(batch);
                }
            } catch (Exception e) {
                throw new SqlSessionException(e);
            }

            //Загрузка завершена
            requestFile.setLoaded(DateUtil.getCurrentDate());
            requestFile.setLoadedRecordCount(index + 1);
            requestFileBean.save(requestFile);
        }catch (Exception e) {
            throw new LoadException(e, requestFile, index + 1, currentFieldName);
        }

        return true;
    }

     private Class getType(byte dataType, int scale){
        switch (dataType){
            case DBFField.FIELD_TYPE_C: return String.class;
            case DBFField.FIELD_TYPE_N: return scale == 0 ? Integer.class : BigDecimal.class;
            case DBFField.FIELD_TYPE_D: return Date.class;
            default: throw new IllegalArgumentException();
        }
    }
}