/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.correction.entity;

/**
 *
 * @author Artem
 */
public class StreetCorrection extends Correction {

    private Long streetTypeCorrectionId;
    private Correction streetTypeCorrection;

    public StreetCorrection() {
        super("street");
    }

    public Correction getStreetTypeCorrection() {
        return streetTypeCorrection;
    }

    public void setStreetTypeCorrection(Correction streetTypeCorrection) {
        this.streetTypeCorrection = streetTypeCorrection;
        this.streetTypeCorrectionId = streetTypeCorrection.getId();
    }

    public Long getStreetTypeCorrectionId() {
        return streetTypeCorrectionId;
    }

    public void setStreetTypeCorrectionId(Long streetTypeId) {
        this.streetTypeCorrectionId = streetTypeId;
    }
}
