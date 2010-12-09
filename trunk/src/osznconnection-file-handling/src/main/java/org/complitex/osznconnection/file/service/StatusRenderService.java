/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.util.Locale;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.RequestStatus;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StatusRenderService {

    private static final String RESOURCE_BUNDLE = StatusRenderService.class.getName();

    public String displayStatus(RequestStatus status, Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, status.name(), locale);
    }
}
