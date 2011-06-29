package org.complitex.dictionary.web.component.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.complitex.dictionary.entity.DomainObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Artem
 */
public class SearchComponentState extends HashMap<String, DomainObject> implements Serializable {
    public static final Long NOT_SPECIFIED_ID = -1L;

    public void updateState(Map<String, ? extends DomainObject> state) {
        boolean clean = false;

        for (String key : state.keySet()){
            DomainObject domainObject = state.get(key);
            DomainObject currentDomainObject = get(key);

            //clear if object changed
            if (domainObject != null && currentDomainObject != null && !domainObject.getId().equals(currentDomainObject.getId())){
                clean = true;
            }
        }

        if (clean){
            for (String k : this.keySet()){
                put(k, new DomainObject(NOT_SPECIFIED_ID));
            }
        }

        putAll(state);
    }

    public boolean isEqual(SearchComponentState searchComponentState, Collection<String> entityEqualCriteria) {
        if (entityEqualCriteria == null || entityEqualCriteria.isEmpty()) {
            throw new IllegalArgumentException("EntityEqualCriteria is null or empty.");
        }

        if (this.isEmpty() && (searchComponentState == null || searchComponentState.isEmpty())) {
            return true;
        }

        if (this.isEmpty() || searchComponentState == null || searchComponentState.isEmpty()) {
            return false;
        }

        for (String entity : entityEqualCriteria) {
            DomainObject thisObject = this.get(entity);
            DomainObject thatObject = searchComponentState.get(entity);
            if ((thisObject == null || thisObject.getId() == null || thisObject.getId().equals(NOT_SPECIFIED_ID))
                    && (thatObject == null || thatObject.getId() == null || thatObject.getId().equals(NOT_SPECIFIED_ID))) {
                //consider it as equal objects
                continue;
            }
            if (thisObject == null || thisObject.getId() == null || thisObject.getId().equals(NOT_SPECIFIED_ID)
                    || thatObject == null || thatObject.getId() == null || thatObject.getId().equals(NOT_SPECIFIED_ID)) {
                return false;
            }

            if (!thisObject.getId().equals(thatObject.getId())) {
                return false;
            }
        }
        return true;
    }
}
