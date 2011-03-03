/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.search;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.util.Map;

/**
 *
 * @author Artem
 */
public interface ISearchCallback {

    void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target);
}
