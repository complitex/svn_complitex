package org.complitex.dictionary.web.component.search;

import com.google.common.collect.ImmutableSet;
import org.complitex.dictionary.entity.DomainObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.complitex.dictionary.util.Numbers;

/**
 *
 * @author Artem
 */
public class SearchComponentState extends HashMap<String, DomainObject> implements Serializable {

    public static final Long NOT_SPECIFIED_ID = -1L;

    public boolean isEmptyState() {
        boolean empty = true;
        for (Entry<String, DomainObject> entry : entrySet()) {
            DomainObject object = entry.getValue();
            if (object != null && object.getId() != null && object.getId() > 0) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    private static boolean isEqual(DomainObject o1, DomainObject o2) {
        if (o1 == null && o2 == null) {
            //not changed
            return true;
        } else {
            if (o1 == null || o2 == null) {
                //changed
                return false;
            } else {
                return Numbers.isEqual(o1.getId(), o2.getId());
            }
        }
    }

    public void updateState(Map<String, ? extends DomainObject> state) {
        boolean clear = false;

        final Set<String> keys = ImmutableSet.<String>builder().addAll(state.keySet()).addAll(keySet()).build();

        for (String key : keys) {
            final DomainObject that = state.get(key);
            final DomainObject current = get(key);

            //clear if object changed
            clear = !isEqual(that, current);
            if (clear) {
                break;
            }
        }

        if (clear) {
            for (String k : keySet()) {
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
