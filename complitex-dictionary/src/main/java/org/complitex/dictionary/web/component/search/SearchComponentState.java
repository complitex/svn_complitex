/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import com.google.common.collect.Maps;
import org.complitex.dictionary.entity.DomainObject;

import java.io.Serializable;
import java.util.Map;
import org.complitex.dictionary.entity.StatusType;

/**
 *
 * @author Artem
 */
public class SearchComponentState implements Serializable {

    private Map<String, DomainObject> state = Maps.newHashMap();

    public void put(String entity, DomainObject object) {
        state.put(entity, object);
    }

    public DomainObject get(String entity) {
        return state.get(entity);
    }

    public void updateState(Map<String, DomainObject> state) {
        for (Map.Entry<String, DomainObject> entry : state.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        state.clear();
    }

    public void updateState(SearchComponentState anotherState) {
        updateState(anotherState.getState());
    }

    public Map<String, DomainObject> getState() {
        return state;
    }

    public boolean hasDisabledObjects() {
        for (DomainObject object : getState().values()) {
            if (object.getStatus() == StatusType.INACTIVE) {
                return true;
            }
        }
        return false;
    }
}
