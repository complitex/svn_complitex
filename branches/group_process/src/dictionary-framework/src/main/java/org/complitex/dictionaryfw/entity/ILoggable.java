package org.complitex.dictionaryfw.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.10.10 17:40
 */
public interface ILoggable extends ILongId{
    public String getLogObjectName();
    public LogChangeList getLogChangeList();
}
