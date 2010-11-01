/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;

/**
 * Класс хранящий детальную информацию о клиентах ЦН(л/c, ФИО, ИНН).
 * @author Artem
 */
public class AccountDetail implements Serializable {

    private String accountNumber;

    private String ownerName;

    private String ownerINN;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerINN() {
        return ownerINN;
    }

    public void setOwnerINN(String ownerINN) {
        this.ownerINN = ownerINN;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return "Account : " + accountNumber + ", owner name : " + ownerName + ", owner INN : " + ownerINN;
    }
}
