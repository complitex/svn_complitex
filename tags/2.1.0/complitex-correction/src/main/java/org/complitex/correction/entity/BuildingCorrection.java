/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.correction.entity;

/**
 * Объект коррекции дома
 * @author Artem
 */
public class BuildingCorrection extends Correction {

    private String correctionCorp;

    public BuildingCorrection() {
        setEntity("building");
    }

    public String getCorrectionCorp() {
        return correctionCorp;
    }

    public void setCorrectionCorp(String correctionCorp) {
        this.correctionCorp = correctionCorp;
    }
}
