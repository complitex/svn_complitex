package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.ajax.AjaxLinkPanel;

import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 21.07.2014 22:10
 */
public class FilteredActionColumn<T> implements IColumn<T, String>, IFilteredColumn<T, String> {
    private ActionDialogPanel<T> actionDialogPanel;

    private List<Action<T>> actions;

    public FilteredActionColumn(List<Action<T>> actions) {
        this.actions = actions;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, final IModel<T> rowModel) {
        RepeatingView repeatingView = new RepeatingView(componentId);

        for (final Action<T> action : actions){
            repeatingView.add(new AjaxLinkPanel(repeatingView.newChildId(), action.getNameModel()) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    actionDialogPanel.open(target, action, rowModel);
                }

                @Override
                public boolean isVisible() {
                    return action.isVisible(rowModel);
                }
            });
        }

        cellItem.add(repeatingView);
    }

    @Override
    public Component getHeader(String componentId) {
        return new EmptyPanel(componentId);
    }

    @Override
    public String getSortProperty() {
        return null;
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    public void detach() {
    }

    @Override
    public Component getFilter(String componentId, FilterForm<?> form) {
        return actionDialogPanel = new ActionDialogPanel<>(componentId);
    }
}
