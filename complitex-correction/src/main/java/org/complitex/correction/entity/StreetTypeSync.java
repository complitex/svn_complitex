package org.complitex.correction.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:57
 */
public class StreetTypeSync extends AbstractSync {
    private String shortName;

    public StreetTypeSync() {
    }

    public StreetTypeSync(String externalId, String name, String shortName) {
        super(externalId, name);

        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
