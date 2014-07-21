package org.complitex.dictionary.web.component.datatable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.web.component.ajax.AjaxLinkPanel;
import org.complitex.dictionary.web.component.wiquery.DialogPanel;
import org.odlabs.wiquery.ui.dialog.AjaxDialogButton;

/**
 * @author Anatoly Ivanov
 *         Date: 21.07.2014 22:10
 */
public class FilteredActionColumn<T> implements IColumn<T, String>, IFilteredColumn<T, String> {
    private DialogPanel dialogPanel;
    private IModel<String> actionModel;
    private IModel<String> messageModel;

    public FilteredActionColumn(IModel<String> actionModel, IModel<String> messageModel) {
        this.actionModel = actionModel;
        this.messageModel = messageModel;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
        cellItem.add(new AjaxLinkPanel(componentId, actionModel) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialogPanel.getDialog().open(target);
            }
        });
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
        dialogPanel = new DialogPanel(componentId, actionModel, messageModel);

        dialogPanel.getDialog().setButtons(new AjaxDialogButton("OK") {
            @Override
            protected void onButtonClicked(AjaxRequestTarget target) {
                dialogPanel.getDialog().close(target);
            }
        });

        return dialogPanel;
    }
}
