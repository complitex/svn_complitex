/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionaryfw.web;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.complitex.dictionaryfw.web.component.search.SearchComponentSessionState;

/**
 *
 * @author Artem
 */
public class DictionaryFwSession extends WebSession {

    private SearchComponentSessionState searchComponentSessionState = new SearchComponentSessionState();

    public DictionaryFwSession(Request request) {
        super(request);
    }

    public SearchComponentSessionState getSearchComponentSessionState() {
        return searchComponentSessionState;
    }
}
