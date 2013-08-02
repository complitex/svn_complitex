package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.04.11 17:26
 */
public interface IExecutorObject extends ILoggable{
    void cancel();
    boolean isCanceled();
    String getErrorMessage();
    void setErrorMessage(String message);
}
