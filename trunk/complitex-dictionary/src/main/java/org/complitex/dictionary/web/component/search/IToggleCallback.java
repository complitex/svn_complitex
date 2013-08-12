package org.complitex.dictionary.web.component.search;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 * @author Pavel Sknar
 */
public interface IToggleCallback extends Serializable {

    void visible(boolean newState, AjaxRequestTarget target);
}
