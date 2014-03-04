package org.complitex.dictionary.web.component.search;

import org.complitex.dictionary.entity.DomainObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

        for (String key : state.keySet()) {
            DomainObject that = state.get(key);
            DomainObject current = get(key);

            //clear if object changed
            clear = !isEqual(that, current);
            if (clear) {
                break;
            }
        }

        if (clear) {
            for (String k : this.keySet()) {
                put(k, new DomainObject(NOT_SPECIFIED_ID));
            }
        }

        putAll(state);
    }
}
