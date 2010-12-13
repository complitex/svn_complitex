/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.exception;

import javax.ejb.ApplicationException;

/**
 *
 * @author Artem
 */
@ApplicationException(rollback = true)
public class DublicateCorrectionException extends RuntimeException {
}
