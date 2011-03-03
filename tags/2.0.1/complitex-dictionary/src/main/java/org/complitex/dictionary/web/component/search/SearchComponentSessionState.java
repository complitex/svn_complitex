/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class SearchComponentSessionState implements Serializable {

    private Map<String, SearchComponentState> stateMap = Maps.newHashMap();

    public void put(String mainEntity, SearchComponentState state) {
        stateMap.put(mainEntity, state);
    }

    public SearchComponentState get(String mainEntity) {
        return stateMap.get(mainEntity);
    }

    public void clear() {
        stateMap.clear();
    }
}
