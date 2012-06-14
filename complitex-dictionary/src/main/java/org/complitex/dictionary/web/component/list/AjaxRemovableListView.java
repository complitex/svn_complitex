/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.list;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 *
 * @author Artem
 */
public abstract class AjaxRemovableListView<T extends Serializable> extends ListView<T> {

    public AjaxRemovableListView(String id, List<? extends T> list) {
        super(id, list);
        init();
    }

    public AjaxRemovableListView(String id, IModel<? extends List<? extends T>> model) {
        super(id, model);
        init();
    }

    protected AjaxRemovableListView<T> init() {
        setReuseItems(true);
        return this;
    }

    @Override
    protected IModel<T> getListItemModel(IModel<? extends List<T>> listViewModel, int index) {
        return new Model<T>(listViewModel.getObject().get(index));
    }

    protected AjaxSubmitLink addRemoveSubmitLink(String linkId, Form<?> form, ListItem<T> item, Component toFocus,
            Component... toUpdate) {
        AjaxSubmitLink removeSubmitLink = getRemoveSubmitLink(linkId, form, item, toFocus, toUpdate);
        item.add(removeSubmitLink);
        return removeSubmitLink;
    }

    protected AjaxLink<Void> addRemoveLink(String linkId, ListItem<T> item, Component toFocus, Component... toUpdate) {
        AjaxLink<Void> removeLink = getRemoveLink(linkId, toFocus, toUpdate);
        item.add(removeLink);
        return removeLink;
    }

    protected AjaxSubmitLink getRemoveSubmitLink(String linkId, Form<?> form, final ListItem<T> item,
            final Component toFocus, final Component... toUpdate) {
        AjaxSubmitLink link = new AjaxSubmitLink(linkId, form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (approveRemoval((ListItem<T>) this.getParent())) {
                    updateListViewOnRemoval(this, target, toFocus, toUpdate);
                } else {
                    for (Component comp : toUpdate) {
                        target.add(comp);
                    }
                }
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("name", "delete:" + item.getId());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        link.setDefaultFormProcessing(false);

        return link;
    }

    protected AjaxLink<Void> getRemoveLink(String linkId, final Component toFocus, final Component... toUpdate) {
        return new AjaxLink<Void>(linkId) {

            @SuppressWarnings({"unchecked"})
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (approveRemoval((ListItem<T>) this.getParent())) {
                    updateListViewOnRemoval(this, target, toFocus, toUpdate);
                } else {
                    for (Component comp : toUpdate) {
                        target.add(comp);
                    }
                }
            }
        };
    }

    protected boolean approveRemoval(ListItem<T> item) {
        return true;
    }

    protected final void updateListViewOnRemoval(AbstractLink link, AjaxRequestTarget target, Component toFocus, Component... toUpdate) {
        ListItem item = (ListItem) link.getParent();
        ListView list = (ListView) item.getParent();

        int removeIndex = item.getIndex();
        int last_index = list.getModelObject().size() - 1;

        //Copy childs from next list item and remove last
        for (int index = item.getIndex(); index < last_index; index++) {
            ListItem li = (ListItem) item.getParent().get(index);
            ListItem li_next = (ListItem) item.getParent().get(index + 1);

            li.removeAll();
            if (li.getIndex() == removeIndex) {
                for (int i = 0; i < li.size(); i++) {
                    li.get(i).remove();
                }
            }
            li.setModelObject(li_next.getModelObject());

            int size = li_next.size();
            Component[] childs = new Component[size];
            for (int i = 0; i < size; i++) {
                childs[i] = li_next.get(i);
            }
            li.add(childs);
        }
        item.getParent().get(last_index).remove();

        list.getModelObject().remove(removeIndex);

        if (toFocus != null) {
            target.focusComponent(toFocus);
        }

        for (Component comp : toUpdate) {
            target.add(comp);
        }
    }

    protected final ListItem<T> getCurrentItem(Component component) {
        return component.visitParents(ListItem.class, new IVisitor<Component, ListItem<T>>() {

            @Override
            public void component(Component object, IVisit<ListItem<T>> visit) {
                visit.stop((ListItem<T>) object);
            }
        });
    }

    protected final int getCurrentIndex(Component component) {
        return getCurrentItem(component).getIndex();
    }
}
