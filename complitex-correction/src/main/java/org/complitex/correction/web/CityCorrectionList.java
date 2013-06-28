/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.correction.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 *
 * @author Artem
 */
public class CityCorrectionList extends AddressCorrectionList {

    public CityCorrectionList() {
        super("city");
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }
}
