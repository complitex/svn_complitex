package org.complitex.dictionary.web.component.paging;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.image.StaticImage;
import org.complitex.resources.WebCommonResourceInitializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.complitex.dictionary.entity.PreferenceKey.CURRENT_PAGE;

/**
 *
 * @author Artem
 */
public class PagingNavigator extends Panel {

    private static final int LEFT_OFFSET = 3;
    private static final int RIGHT_OFFSET = 3;
    private static final List<Long> SUPPORTED_PAGE_SIZES = Arrays.asList(10L, 20L, 30L, 50L, 100L);
    private DataView<?> dataView;
    private WebMarkupContainer pageBar;
    private Form<Void> newPageForm;
    private WebMarkupContainer allPagesRegion;
    private IModel<Long> rowsPerPagePropertyModel;
    private Component[] toUpdate;
    private List<IPagingNavigatorListener> listeners = new ArrayList<IPagingNavigatorListener>();

    /**
     * The same as general constructor except that navigator don't persist number of rows per page to preferences.
     * 
     * @param id Wicket component's id.
     * @param dataView Data view.
     * @param toUpdate List of components to be updated on navigation events.
     */
    public PagingNavigator(String id, final DataView<?> dataView, Component... toUpdate) {
        this(id, dataView, null, toUpdate);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(WebCommonResourceInitializer.SCROLL_JS));
    }

    /**
     * General constructor.
     * 
     * @param id Wicket component's id.
     * @param dataView Data view.
     * @param page Preference page. If {@code page} is not not {@code null} then it is used to persist number of rows per page as 
     * {@link PreferenceKey#ROWS_PER_PAGE} preference.
     * @param toUpdate List of components to be updated on navigation events.
     */
    public PagingNavigator(String id, final DataView<?> dataView, final String page, Component... toUpdate) {
        super(id);
        setOutputMarkupId(true);

        this.dataView = dataView;
        this.toUpdate = toUpdate;

        rowsPerPagePropertyModel = new IModel<Long>() {
            @Override
            public Long getObject() {
                return dataView.getItemsPerPage();
            }

            @Override
            public void setObject(Long items) {
                dataView.setItemsPerPage(items);
            }

            @Override
            public void detach() {
            }
        };

        //retrieve table page size from preferences.
        Long rowsPerPage;
        if (page != null) {
            rowsPerPage = getSession().getPreferenceLong(page, PreferenceKey.ROWS_PER_PAGE, SUPPORTED_PAGE_SIZES.get(0));
        } else {
            rowsPerPage = SUPPORTED_PAGE_SIZES.get(0);
        }

        rowsPerPagePropertyModel.setObject(rowsPerPage);
        dataView.setCurrentPage(getSession().getPreferenceInteger(page, CURRENT_PAGE, 0));

        //preference
        addListener(new IPagingNavigatorListener() {
            @Override
            public void onChangePage() {
                //preference
                if (page != null) {
                    getSession().putPreference(page, CURRENT_PAGE, dataView.getCurrentPage(), true);
                }
            }
        });

        WebMarkupContainer pageNavigator = new WebMarkupContainer("pageNavigator");
        add(pageNavigator);
        pageBar = new WebMarkupContainer("pageBar");
        pageNavigator.add(pageBar);

        // Add additional page links
        pageBar.add(newPagingNavigationLink("first", dataView, 0).
                add(new StaticImage("firstImage", new SharedResourceReference("images/pageNavStart.gif"))).
                add(new TitleResourceAppender("PagingNavigator.first")));
        pageBar.add(newPagingNavigationIncrementLink("prev", dataView, -1).
                add(new StaticImage("prevImage", new SharedResourceReference("images/pageNavPrev.gif"))).
                add(new TitleResourceAppender("PagingNavigator.previous")));
        pageBar.add(newPagingNavigationIncrementLink("next", dataView, 1).
                add(new StaticImage("nextImage", new SharedResourceReference("images/pageNavNext.gif"))).
                add(new TitleResourceAppender("PagingNavigator.next")));
        pageBar.add(newPagingNavigationLink("last", dataView, -1).
                add(new StaticImage("lastImage", new SharedResourceReference("images/pageNavEnd.gif"))).
                add(new TitleResourceAppender("PagingNavigator.last")));

        //navigation before
        IModel<List<Long>> navigationBeforeModel = new AbstractReadOnlyModel<List<Long>>() {

            @Override
            public List<Long> getObject() {
                List<Long> result = new ArrayList<>();

                long currentPage = dataView.getCurrentPage();
                for (long i = LEFT_OFFSET; i > 0; i--) {
                    if ((currentPage - i) >= 0) {
                        result.add(currentPage - i);
                    }
                }
                return result;
            }
        };
        pageBar.add(newNavigation("navigationBefore", "pageLinkBefore", "pageNumberBefore", dataView, navigationBeforeModel));

        //navigation after
        IModel<List<Long>> navigationAfterModel = new AbstractReadOnlyModel<List<Long>>() {

            @Override
            public List<Long> getObject() {
                List<Long> result = new ArrayList<>();

                long currentPage = dataView.getCurrentPage();
                for (int i = 1; i <= RIGHT_OFFSET; i++) {
                    if ((currentPage + i) < dataView.getPageCount()) {
                        result.add(currentPage + i);
                    }
                }
                return result;
            }
        };
        pageBar.add(newNavigation("navigationAfter", "pageLinkAfter", "pageNumberAfter", dataView, navigationAfterModel));

        //navigation current
        IModel<List<Long>> navigationCurrentModel = new AbstractReadOnlyModel<List<Long>>() {

            @Override
            public List<Long> getObject() {
                return Arrays.asList(dataView.getCurrentPage());
            }
        };
        pageBar.add(newNavigation("navigationCurrent", "pageLinkCurrent", "pageNumberCurrent", dataView, navigationCurrentModel));

        //new page form
        newPageForm = new Form<>("newPageForm");
        IModel<String> newPageNumberModel = new Model<String>() {

            @Override
            public void setObject(String input) {
                if (!Strings.isEmpty(input)) {
                    Integer newPageNumber = null;
                    try {
                        newPageNumber = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        //shit...
                    }

                    if (newPageNumber != null) {
                        if (newPageNumber <= 0) {
                            dataView.setCurrentPage(0);
                        } else if (newPageNumber > dataView.getPageCount()) {
                            dataView.setCurrentPage(dataView.getPageCount() - 1);
                        } else {
                            dataView.setCurrentPage(newPageNumber - 1);
                        }

                        getSession().putPreference(page, CURRENT_PAGE, dataView.getCurrentPage(), true);
                    }
                }
            }
        };
        TextField<String> newPageNumber = new TextField<>("newPageNumber", newPageNumberModel);
        AjaxButton goToPage = new AjaxButton("goToPage", newPageForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                //update model - newPageNumberModel
                updatePageComponents(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };

        newPageForm.add(newPageNumber);
        newPageForm.add(goToPage);
        pageNavigator.add(newPageForm);

        //page size
        IModel<Long> pageSizeModel = new Model<Long>() {

            @Override
            public Long getObject() {
                return rowsPerPagePropertyModel.getObject();
            }

            @Override
            public void setObject(Long rowsPerPage) {
                if (page != null) {
                    getSession().putPreference(page, PreferenceKey.ROWS_PER_PAGE, rowsPerPage, true);
                }
                rowsPerPagePropertyModel.setObject(rowsPerPage);
            }
        };
        DropDownChoice<Long> pageSize = new DropDownChoice<>("pageSize", pageSizeModel, SUPPORTED_PAGE_SIZES);
        pageSize.setNullValid(false);

        pageSize.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                //update model - pageSizeModel
                updatePageComponents(target);
            }
        });
        pageNavigator.add(pageSize);

        //all pages region
        allPagesRegion = new WebMarkupContainer("allPagesRegion");
        Label allPages = new Label("allPages", new AbstractReadOnlyModel<Long>() {

            @Override
            public Long getObject() {
                return dataView.getPageCount();
            }
        });
        allPagesRegion.add(allPages);
        pageNavigator.add(allPagesRegion);
    }

    @Override
    public DictionaryFwSession getSession() {
        return (DictionaryFwSession) super.getSession();
    }

    protected ListView<Long> newNavigation(String navigationId, final String pageLinkId, final String pageNumberId,
                                              final IPageable pageable, IModel<List<Long>> navigationModel) {
        return new ListView<Long>(navigationId, navigationModel) {

            @Override
            protected void populateItem(ListItem<Long> item) {
                Long pageIndex = item.getModelObject();
                AbstractLink pageLink = newPagingNavigationLink(pageLinkId, pageable, pageIndex);
                pageLink.add(new TitlePageNumberAppender(pageIndex));
                Label pageNumber = new Label(pageNumberId, String.valueOf(pageIndex + 1));
                pageLink.add(pageNumber);
                item.add(pageLink);
            }
        };
    }

    /**
     * Create a new increment link. May be subclassed to make use of specialized links, e.g. Ajaxian
     * links.
     *
     * @param id
     *            the link id
     * @param pageable
     *            the pageable to control
     * @param increment
     *            the increment
     * @return the increment link
     */
    protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment) {
        return new AjaxPagingNavigationIncrementLink(id, pageable, increment) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                super.onClick(target);
                //appendScrollupJavascript(target);
                updatePageComponents(target);
            }
        };
    }

    protected void updatePageComponents(AjaxRequestTarget target) {
        if (toUpdate != null) {
            for (Component container : toUpdate) {
                target.add(container);
            }
        }
        target.add(this);
    }

    /**
     * Create a new pagenumber link. May be subclassed to make use of specialized links, e.g.
     * Ajaxian links.
     *
     * @param id
     *            the link id
     * @param pageable
     *            the pageable to control
     * @param pageNumber
     *            the page to jump to
     * @return the pagenumber link
     */
    protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, long pageNumber) {
        return new AjaxPagingNavigationLink(id, pageable, pageNumber) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                super.onClick(target);
                //appendScrollupJavascript(target);
                updatePageComponents(target);

                //listeners
                for (IPagingNavigatorListener listener : listeners) {
                    listener.onChangePage();
                }
            }
        };
    }

    protected void appendScrollupJavascript(AjaxRequestTarget target) {
        String javascript = "scrollTo(0, {axis:'y'});";
        target.appendJavaScript(javascript);
    }

    @Override
    protected void onBeforeRender() {
        boolean visibility = dataView.getPageCount() > 1;
        pageBar.setVisible(visibility);
        newPageForm.setVisible(visibility);
        allPagesRegion.setVisible(visibility);

        super.onBeforeRender();
    }

    public void addListener(IPagingNavigatorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IPagingNavigatorListener listener) {
        listeners.remove(listener);
    }

    /**
     * Appends title attribute to navigation links
     *
     * @author igor.vaynberg
     */
    private final class TitleResourceAppender extends Behavior {

        private static final long serialVersionUID = 1L;
        private final String resourceKey;

        /**
         * Constructor
         *
         * @param resourceKey
         *            resource key of the message
         */
        TitleResourceAppender(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        /** {@inheritDoc} */
        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
            tag.put("title", PagingNavigator.this.getString(resourceKey));
        }
    }

    private final class TitlePageNumberAppender extends Behavior {

        private static final long serialVersionUID = 1L;
        /** page number */
        private final long page;

        /**
         * Constructor
         *
         * @param page
         *            page number to use as the ${page} var
         */
        TitlePageNumberAppender(long page) {
            this.page = page;
        }

        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
            tag.put("title", page + 1 + "");
        }
    }
}
