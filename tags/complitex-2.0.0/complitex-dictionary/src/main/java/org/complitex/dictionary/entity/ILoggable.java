package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 18:48
 */
public interface  ILoggable extends ILongId{
    public String getLogObjectName();
    public LogChangeList getLogChangeList();
}