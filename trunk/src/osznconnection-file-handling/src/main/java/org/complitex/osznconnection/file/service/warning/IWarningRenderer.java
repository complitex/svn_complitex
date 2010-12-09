/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.warning;

import java.util.List;
import java.util.Locale;
import org.complitex.osznconnection.file.entity.RequestWarning;

/**
 *
 * @author Artem
 */
public interface IWarningRenderer {

    String display(List<RequestWarning> requestWarnings, Locale locale);

    String display(RequestWarning requestWarning, Locale locale);
}
