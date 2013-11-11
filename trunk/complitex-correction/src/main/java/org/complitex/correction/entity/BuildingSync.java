package org.complitex.correction.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.13 16:11
 */
public class BuildingSync extends AbstractSync{
    private String streetExternalId;
    private String part;

    public BuildingSync() {
    }

    public BuildingSync(String streetExternalId, String externalId, String name, String part) {
        super(externalId, name);

        this.part = part;
        this.streetExternalId = streetExternalId;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getStreetExternalId() {
        return streetExternalId;
    }

    public void setStreetExternalId(String streetExternalId) {
        this.streetExternalId = streetExternalId;
    }
}
