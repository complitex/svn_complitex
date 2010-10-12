package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.RequestFileGroup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 13:44:25
 */
public interface ITaskListener {
    public void complete(RequestFileGroup group);
    public void skip(RequestFileGroup group);
    public void error(RequestFileGroup group, Exception e);
}
