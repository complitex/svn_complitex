/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.correction.service.exception;

/**
 *
 * @author Artem
 */
public class MoreOneCorrectionException extends Exception {

    private String entity;

    public MoreOneCorrectionException(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }
}
