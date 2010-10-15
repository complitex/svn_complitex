package org.complitex.dictionaryfw.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:28:03
 *
 * Модель лога журнала событий
 */
public class Log implements Serializable{
    public static enum EVENT {SYSTEM_START, SYSTEM_STOP, USER_LOGIN, USER_LOGOFF, LIST, VIEW, CREATE, EDIT, REMOVE}
    public static enum STATUS {OK, ERROR}

    private Long id;
    private Date date;
    private String login;
    private String module;
    private String controller;
    private String model;    
    private Long objectId;
    private EVENT event;
    private STATUS status;
    private String description;
    private List<LogChange> logChanges;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public EVENT getEvent() {
        return event;
    }

    public void setEvent(EVENT event) {
        this.event = event;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LogChange> getLogChanges() {
        return logChanges;
    }

    public void setLogChanges(List<LogChange> logChanges) {
        this.logChanges = logChanges;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", date=" + date +
                ", login='" + login + '\'' +
                ", module='" + module + '\'' +
                ", controller='" + controller + '\'' +
                ", model='" + model + '\'' +
                ", objectId=" + objectId +
                ", event=" + event +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", logChanges=" + logChanges +
                '}';
    }
}