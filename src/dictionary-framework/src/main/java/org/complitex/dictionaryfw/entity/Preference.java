package org.complitex.dictionaryfw.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 17:17
 */
public class Preference {
    private Long id;
    private Long userId;
    private String page;
    private String key;
    private String value;
    private Object object;

    public Preference() {
        //Default constructor
    }

    public Preference(Long userId, String page, String key, String value, Object object) {
        this.userId = userId;
        this.page = page;
        this.key = key;
        this.value = value;
        this.object = object;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
