/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 * Объект коррекции типа сущности
 * @author Artem
 */
public class EntityTypeCorrection extends Correction {

    public EntityTypeCorrection() {
    }

    public EntityTypeCorrection(long organizationId, long entityTypeId) {
        setOrganizationId(organizationId);
        setInternalObjectId(entityTypeId);
    }
}
