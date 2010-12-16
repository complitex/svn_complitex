package org.complitex.dictionary.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.07.2010 15:26:28
 */
public class UserGroup implements Serializable{
    public static enum GROUP_NAME{
        ADMINISTRATORS,
        EMPLOYEES        
    }

    private Long id;

    private String login;

    private GROUP_NAME groupName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public GROUP_NAME getGroupName() {
        return groupName;
    }

    public void setGroupName(GROUP_NAME groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "UserGroup{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", groupName=" + groupName +
                '}';
    }
}
