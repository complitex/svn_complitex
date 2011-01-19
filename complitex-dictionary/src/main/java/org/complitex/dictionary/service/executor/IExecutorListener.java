package org.complitex.dictionary.service.executor;

import org.complitex.dictionary.entity.ILoggable;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.12.10 14:49
 */
public interface IExecutorListener<T extends ILoggable>{
    public void onComplete(List<T> processed);
}