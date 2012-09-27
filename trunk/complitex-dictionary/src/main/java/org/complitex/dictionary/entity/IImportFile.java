package org.complitex.dictionary.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 18:58
 */
public interface IImportFile extends Serializable {
    public String getFileName();

    public String name();
}
