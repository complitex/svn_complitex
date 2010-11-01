/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;

/**
 * Хранит информацию о текущем ЦН: id, класс адаптера для взаимодействия с ЦН, метод для получения экземпляра адаптера.
 * @author Artem
 */
public class CalculationCenterInfo implements Serializable {

    private long id;

    private String adapterClass;

    public CalculationCenterInfo(long id, String adapterClass) {
        this.id = id;
        this.adapterClass = adapterClass;
    }

    public long getId() {
        return id;
    }

    public Class<? extends ICalculationCenterAdapter> getAdapterClass() {
        try {
            return (Class<? extends ICalculationCenterAdapter>) Thread.currentThread().getContextClassLoader().loadClass(adapterClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ICalculationCenterAdapter getAdapterInstance() {
        try {
            return getAdapterClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
