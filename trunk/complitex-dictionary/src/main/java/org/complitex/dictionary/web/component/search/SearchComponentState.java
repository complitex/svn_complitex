/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.complitex.dictionary.entity.DomainObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    public boolean isEqual(SearchComponentState searchComponentState, Collection<String> entityEqualCriteria) {
        if (entityEqualCriteria == null || entityEqualCriteria.isEmpty()) {
            throw new IllegalArgumentException("EntityEqualCriteria is null or empty.");
        }

        Map<String, DomainObject> thisState = getState();
        Map<String, DomainObject> thatState = searchComponentState.getState();

        if ((thisState == null || thisState.isEmpty()) && (thatState == null || thatState.isEmpty())) {
            return true;
        }

        if (thisState == null || thisState.isEmpty() || thatState == null || thatState.isEmpty()) {
            return false;
        }

        for (String entity : entityEqualCriteria) {
            DomainObject thisObject = thisState.get(entity);
            DomainObject thatObject = thatState.get(entity);
            if ((thisObject == null || thisObject.getId() == null || thisObject.getId().equals(SearchComponent.NOT_SPECIFIED_ID))
                    && (thatObject == null || thatObject.getId() == null || thatObject.getId().equals(SearchComponent.NOT_SPECIFIED_ID))) {
                //consider it as equal objects
                continue;
            }
            if (thisObject == null || thisObject.getId() == null || thisObject.getId().equals(SearchComponent.NOT_SPECIFIED_ID)
                    || thatObject == null || thatObject.getId() == null || thatObject.getId().equals(SearchComponent.NOT_SPECIFIED_ID)) {
                return false;
            }

            if (!thisObject.getId().equals(thatObject.getId())) {
                return false;
            }
        }
        return true;
    }

    public boolean isEqual(SearchComponentState searchComponentState) {
        Set<String> thisEntitySet = getEntitySet();
        Set<String> thatEntitySet = searchComponentState.getEntitySet();
        if ((thisEntitySet == null || thisEntitySet.isEmpty()) && (thatEntitySet == null || thatEntitySet.isEmpty())) {
            return true;
        }
        if (thisEntitySet == null || thisEntitySet.isEmpty() || thatEntitySet == null || thatEntitySet.isEmpty()) {
            return false;
        }

        if (!thisEntitySet.equals(thatEntitySet)) {
            throw new IllegalArgumentException("Entity set of searchComponentState parameter does not equal to current entity set.");
        }

        return isEqual(searchComponentState, thisEntitySet);
    }

    private Set<String> getEntitySet() {
        return ImmutableSet.copyOf(getState().keySet());
    }
}
