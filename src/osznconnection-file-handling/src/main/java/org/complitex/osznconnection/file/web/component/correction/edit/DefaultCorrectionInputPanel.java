/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Artem
 */
public class DefaultCorrectionInputPanel extends Panel {

    public DefaultCorrectionInputPanel(String id, IModel<String> correctionModel) {
        super(id);
        add(new TextField<String>("correction", correctionModel));
    }
}
