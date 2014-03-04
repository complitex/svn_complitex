package org.complitex.address.strategy.building.entity;

import java.io.Serializable;

public class BuildingSegmentImport implements Serializable {
    private long id;
    private Long gek;
    private String code;
    private long buildingImportId;

    public BuildingSegmentImport() {
    }

    public BuildingSegmentImport(long id, Long gekId, String code, long buildingImportId) {
        this.id = id;
        this.gek = gekId;
        this.code = code;
        this.buildingImportId = buildingImportId;
    }

    public long getBuildingImportId() {
        return buildingImportId;
    }

    public void setBuildingImportId(long buildingImportId) {
        this.buildingImportId = buildingImportId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getGek() {
        return gek;
    }

    public void setGek(Long gek) {
        this.gek = gek;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
