package org.complitex.dictionary.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.01.11 16:30
 */
public class Subject {
    private String entity;

    private Long objectId;

    public Subject() {
    }

    public Subject(String entity, Long objectId) {
        this.entity = entity;
        this.objectId = objectId;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subject subject = (Subject) o;

        if (entity != null ? !entity.equals(subject.entity) : subject.entity != null) return false;
        if (objectId != null ? !objectId.equals(subject.objectId) : subject.objectId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        return result;
    }
}
