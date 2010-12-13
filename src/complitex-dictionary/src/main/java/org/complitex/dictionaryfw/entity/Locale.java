/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.entity;

import java.io.Serializable;

/**
 *
 * @author Artem
 */
public class Locale implements Serializable {

    private Long id;
    private String language;
    private boolean system;

    public Locale(Long id, String language, boolean system) {
        this.id = id;
        this.language = language;
        this.system = system;
    }

    public Long getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isSystem() {
        return system;
    }
}
