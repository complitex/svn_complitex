package org.complitex.logging.service;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.AbstractFilter;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 19.08.2010 13:08:36
 */
public class LogFilter extends AbstractFilter{
    private Date date;
    private String login;
    private String module;
    private String controller;
    private String model;
    private Long objectId;
    private Log.EVENT event;
    private Log.STATUS status;
    private String description;

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

    public Log.EVENT getEvent() {
        return event;
    }

    public void setEvent(Log.EVENT event) {
        this.event = event;
    }

    public Log.STATUS getStatus() {
        return status;
    }

    public void setStatus(Log.STATUS status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "LogFilter{" +
                "date=" + date +
                ", login='" + login + '\'' +
                ", module='" + module + '\'' +
                ", controller='" + controller + '\'' +
                ", model='" + model + '\'' +
                ", objectId=" + objectId +
                ", event=" + event +
                ", status=" + status +
                ", description='" + description + '\'' +
                '}';
    }
}
