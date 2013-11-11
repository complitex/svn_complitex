package org.complitex.correction.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:54
 */
public abstract class AbstractSync {
    private String externalId;
    private String name;

    public AbstractSync() {
    }

    public AbstractSync(String externalId, String name) {
        this.externalId = externalId;
        this.name = name;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
