package org.complitex.osznconnection.file.entity;

import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongSizeException;
import org.complitex.osznconnection.file.service.exception.FieldWrongTypeException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 13:15:40
 *
 * Абстрактное представление записи файла запроса.
 * Используется <code>Map<String, Object></code> для хранения названий и значений полей.
 *
 * @see org.complitex.osznconnection.file.entity.Benefit
 * @see org.complitex.osznconnection.file.entity.Payment
 * @see org.complitex.osznconnection.file.entity.Tarif
 */
public abstract class AbstractRequest implements Serializable {
    private Long id;
    private Long requestFileId;
    private Long organizationId;
    private RequestStatus status;
    private String accountNumber;

    protected Map<String, Object> dbfFields = new HashMap<String, Object>();

    /**
     * Проверяет допустимость имени поля и возвращает тип поля.
     * @param name Имя поля
     * @return Тип поля
     * @throws FieldNotFoundException Недопустимое значение имени поля
     */
    protected abstract Class getFieldType(String name) throws FieldNotFoundException;

    /**
     * Проверяет допустимый размер поля
     * @param name Имя поля
     * @param value Значение поля
     * @throws FieldWrongSizeException Недопустимый размер поля
     */
    protected abstract void checkSize(String name, Object value) throws FieldWrongSizeException;

    /**
     * Установка поля с проверкой допустимого значения типа и имени.
     * @param name Имя поля
     * @param value Значения поля
     * @param type Тип поля
     * @throws FieldNotFoundException Недопустимое значение имени поля
     * @throws FieldWrongTypeException Недопустимый тип поля
     * @throws FieldWrongSizeException Недопустимый размер поля 
     */
    public final void setField(String name, Object value, Class type) throws FieldNotFoundException, FieldWrongTypeException, FieldWrongSizeException {
        checkSize(name, value);

        try {
            if (!getFieldType(name).equals(type)){
                throw new FieldWrongTypeException(name);                
            }

            dbfFields.put(name, value);
        } catch (IllegalArgumentException e) {
            throw new FieldNotFoundException(e);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestFileId() {
        return requestFileId;
    }

    public void setRequestFileId(Long requestFileId) {
        this.requestFileId = requestFileId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Map<String, Object> getDbfFields() {
        return dbfFields;
    }

    public void setDbfFields(Map<String, Object> dbfFields) {
        this.dbfFields = dbfFields;
    }
}
