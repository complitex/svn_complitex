package org.complitex.correction.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 15:58
 */
public class StreetSync extends AbstractSync {
    private String streetTypeShortName;

    public StreetSync() {
    }

    public StreetSync(String externalId, String name, String streetTypeShortName) {
        super(externalId, name);

        this.streetTypeShortName = streetTypeShortName;
    }

    public String getStreetTypeShortName() {
        return streetTypeShortName;
    }

    public void setStreetTypeShortName(String streetTypeShortName) {
        this.streetTypeShortName = streetTypeShortName;
    }
}
