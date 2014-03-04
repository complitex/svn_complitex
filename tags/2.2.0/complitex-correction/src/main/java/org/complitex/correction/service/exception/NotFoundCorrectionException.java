/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.correction.service.exception;

/**
 *
 * @author Artem
 */
public class NotFoundCorrectionException extends Exception {

    private String entity;

    public NotFoundCorrectionException(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }
}
