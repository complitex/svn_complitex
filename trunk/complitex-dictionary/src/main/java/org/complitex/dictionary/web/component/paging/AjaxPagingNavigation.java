package org.complitex.dictionary.web.component.paging;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * @author Pavel Sknar
 */
public class AjaxPagingNavigation extends org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation {

    private List<IPagingNavigatorListener> listeners = Lists.newArrayList();

    public AjaxPagingNavigation(String id, IPageable pageable) {
        super(id, pageable);
    }

    public AjaxPagingNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
    }


    @Override
    protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
        return new AjaxPagingNavigationLink(id, pageable, pageIndex) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                super.onClick(target);

                //listeners
                for (IPagingNavigatorListener listener : listeners) {
                    listener.onChangePage();
                }
            }
        };
    }

    @Override
    protected void populateItem(LoopItem loopItem) {
        // Get the index of page this link shall point to
        final long pageIndex = getStartIndex() + loopItem.getIndex();

        loopItem.add(new AttributeAppender("class", new Model<>(
                pageable.getCurrentPage() == pageIndex ? "off" : "on"
        )));

        super.populateItem(loopItem);

    }

    public void addListener(IPagingNavigatorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IPagingNavigatorListener listener) {
        listeners.remove(listener);
    }
}
