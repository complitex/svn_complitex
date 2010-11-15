/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Хранит информацию о текущем ЦН: id, класс адаптера для взаимодействия с ЦН, метод для получения экземпляра адаптера.
 * @author Artem
 */
public class CalculationCenterPreference implements Serializable {
    private Long calculationCenterId;
    private String adapterClass;

    public Long getCalculationCenterId() {
        return calculationCenterId;
    }

    public void setCalculationCenterId(Long calculationCenterId) {
        this.calculationCenterId = calculationCenterId;
    }

    public String getAdapterClass() {
        return adapterClass;
    }

    public void setAdapterClass(String adapterClass) {
        this.adapterClass = adapterClass;
    }
}
