package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.02.11 15:07
 */
public class Name {
    private Long id;
    private String name;

    public Name() {
    }

    public Name(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
