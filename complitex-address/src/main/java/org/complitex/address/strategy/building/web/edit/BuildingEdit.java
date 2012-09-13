/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.building.web.edit;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.component.toolbar.search.CollapsibleInputSearchToolbarButton;
import org.complitex.template.web.pages.DomainObjectEdit;

/**
 *
 * @author Artem
 */
public class BuildingEdit extends DomainObjectEdit {

    public BuildingEdit(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbarButtons = Lists.newArrayList();
        toolbarButtons.addAll(super.getToolbarButtons(id));
        toolbarButtons.add(new CollapsibleInputSearchToolbarButton(id));
        return toolbarButtons;
    }
}
