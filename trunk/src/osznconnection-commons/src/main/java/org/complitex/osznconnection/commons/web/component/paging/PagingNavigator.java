package org.complitex.osznconnection.commons.web.component.paging;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Artem
 */
public class PagingNavigator extends Panel {

    private static final int LEFT_OFFSET = 3;
    private static final int RIGHT_OFFSET = 3;
    private static final List<Integer> SUPPORTED_PAGE_SIZES = Arrays.asList(
            10, 20, 30, 50, 100);
    private static final String PAGE_SIZE_PREFERENCE_KEY = "PageSize";
    private IPageable pageable;
    private WebMarkupContainer pageBar;
    private Form newPageForm;
    private WebMarkupContainer allPagesRegion;
    private PropertyModel<Integer> rowsPerPagePropertyModel;
//    private UIPreferences preferences;
    private String pageNumberPreferenceKey;

    public PagingNavigator(String id, final IPageable pageable, final String rowsPerPagePropertyExpression, String pageNumberPreferenceKey) {
        super(id);

        this.pageable = pageable;
        this.pageNumberPreferenceKey = pageNumberPreferenceKey;
        rowsPerPagePropertyModel = new PropertyModel<Integer>(pageable, rowsPerPagePropertyExpression);

        //retrieve table page size from preferences.
//        Integer rowsPerPage = preferences.getPreference(UIPreferences.PreferenceType.PAGE_SIZE, PAGE_SIZE_PREFERENCE_KEY, Integer.class);
//        if (rowsPerPage == null) {
//            rowsPerPage = SUPPORTED_PAGE_SIZES.get(0);
//        }

//        rowsPerPagePropertyModel.setObject(rowsPerPage);

        //retrieve table page index from preferences.
//        Integer pageIndex = preferences.getPreference(UIPreferences.PreferenceType.PAGE_NUMBER, pageNumberPreferenceKey, Integer.class);
//        if (pageIndex != null && pageIndex < pageable.getPageCount()) {
//            pageable.setCurrentPage(pageIndex);
//        }

        WebMarkupContainer pageNavigator = new WebMarkupContainer("pageNavigator");
        pageBar = new WebMarkupContainer("pageBar");
        pageNavigator.add(pageBar);

        // Add additional page links
        pageBar.add(newPagingNavigationLink("first", pageable, 0).
                add(new Image("firstImage", new ResourceReference("images/pageNavStart.gif"))).
                add(new TitleResourceAppender("PagingNavigator.first")));
        pageBar.add(newPagingNavigationIncrementLink("prev", pageable, -1).
                add(new Image("prevImage", new ResourceReference("images/pageNavPrev.gif"))).
                add(new TitleResourceAppender("PagingNavigator.previous")));
        pageBar.add(newPagingNavigationIncrementLink("next", pageable, 1).
                add(new Image("nextImage", new ResourceReference("images/pageNavNext.gif"))).
                add(new TitleResourceAppender("PagingNavigator.next")));
        pageBar.add(newPagingNavigationLink("last", pageable, -1).
                add(new Image("lastImage", new ResourceReference("images/pageNavEnd.gif"))).
                add(new TitleResourceAppender("PagingNavigator.last")));

        //navigation before
        IModel navigationBeforeModel = new AbstractReadOnlyModel<List<? extends Integer>>() {

            @Override
            public List<? extends Integer> getObject() {
                List result = new ArrayList();

                int currentPage = pageable.getCurrentPage();
                for (int i = LEFT_OFFSET; i > 0; i--) {
                    if ((currentPage - i) >= 0) {
                        result.add(currentPage - i);
                    }
                }
                return result;
            }
        };
        pageBar.add(newNavigation("navigationBefore", "pageLinkBefore", "pageNumberBefore", pageable, navigationBeforeModel));

        //navigation after
        IModel navigationAfterModel = new AbstractReadOnlyModel<List<? extends Integer>>() {

            @Override
            public List<? extends Integer> getObject() {
                List result = new ArrayList();

                int currentPage = pageable.getCurrentPage();
                for (int i = 1; i <= RIGHT_OFFSET; i++) {
                    if ((currentPage + i) < pageable.getPageCount()) {
                        result.add(currentPage + i);
                    }
                }
                return result;
            }
        };
        pageBar.add(newNavigation("navigationAfter", "pageLinkAfter", "pageNumberAfter", pageable, navigationAfterModel));

        //navigation current
        IModel navigationCurrentModel = new AbstractReadOnlyModel<List<? extends Integer>>() {

            @Override
            public List<? extends Integer> getObject() {
                return Arrays.asList(pageable.getCurrentPage());
            }
        };
        pageBar.add(newNavigation("navigationCurrent", "pageLinkCurrent", "pageNumberCurrent", pageable, navigationCurrentModel));

        //new page form
        newPageForm = new Form("newPageForm");
        TextField newPageNumber = new TextField("newPageNumber", new Model<String>() {

            @Override
            public void setObject(String input) {
                if (!Strings.isEmpty(input)) {
                    Integer newPageNumber = null;
                    try {
                        newPageNumber = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                    }

                    if (newPageNumber != null) {
                        if (newPageNumber <= 0) {
                            pageable.setCurrentPage(0);
                        } else if (newPageNumber > pageable.getPageCount()) {
                            pageable.setCurrentPage(pageable.getPageCount() - 1);
                        } else {
                            pageable.setCurrentPage(newPageNumber - 1);
                        }
                    }
                }
            }
//            @Override
//            public String getObject() {
//                return String.valueOf(pageable.getCurrentPage() + 1);
//            }
        });
        Button goToPage = new Button("goToPage");

        newPageForm.add(newPageNumber);
        newPageForm.add(goToPage);
        pageNavigator.add(newPageForm);

        //page size form
        Form pageSizeForm = new Form("pageSizeForm");
        IModel<Integer> pageSizeModel = new Model<Integer>() {

            @Override
            public Integer getObject() {
                return rowsPerPagePropertyModel.getObject();
            }

            @Override
            public void setObject(Integer rowsPerPage) {
//                preferences.putPreference(UIPreferences.PreferenceType.PAGE_SIZE, PAGE_SIZE_PREFERENCE_KEY, rowsPerPage);
//                rowsPerPagePropertyModel.setObject(rowsPerPage);
            }
        };
        DropDownChoice<Integer> pageSize = new DropDownChoice<Integer>("pageSize", pageSizeModel, SUPPORTED_PAGE_SIZES) {

            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        };
        pageSizeForm.add(pageSize);
        pageNavigator.add(pageSizeForm);

        //all pages region
        allPagesRegion = new WebMarkupContainer("allPagesRegion");
        Label allPages = new Label("allPages", new AbstractReadOnlyModel<Integer>() {

            @Override
            public Integer getObject() {
                return pageable.getPageCount();
            }
        });
        allPagesRegion.add(allPages);
        pageNavigator.add(allPagesRegion);

        add(pageNavigator);
    }

    protected ListView<Integer> newNavigation(String navigationId, final String pageLinkId, final String pageNumberId, final IPageable pageable,
            IModel<List<? extends Integer>> navigationModel) {
        return new ListView<Integer>(navigationId, navigationModel) {

            @Override
            protected void populateItem(ListItem<Integer> item) {
                Integer pageIndex = item.getModelObject();
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
    protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable,
            int increment) {
        return new PagingNavigationIncrementLink<Void>(id, pageable, increment);
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
    protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
        return new PagingNavigationLink<Void>(id, pageable, pageNumber);
    }

    @Override
    protected void onBeforeRender() {
        boolean visisble = pageable.getPageCount() <= 1 ? false : true;
        pageBar.setVisible(visisble);
        newPageForm.setVisible(visisble);
        allPagesRegion.setVisible(visisble);

        super.onBeforeRender();
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

//        preferences.putPreference(UIPreferences.PreferenceType.PAGE_NUMBER, pageNumberPreferenceKey, pageable.getCurrentPage());
    }

    /**
     * Appends title attribute to navigation links
     *
     * @author igor.vaynberg
     */
    private final class TitleResourceAppender extends AbstractBehavior {

        private static final long serialVersionUID = 1L;
        private final String resourceKey;

        /**
         * Constructor
         *
         * @param resourceKey
         *            resource key of the message
         */
        public TitleResourceAppender(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        /** {@inheritDoc} */
        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
            tag.put("title", PagingNavigator.this.getString(resourceKey));
        }
    }

    /**
     * Appends title attribute to navigation links
     *
     * @author igor.vaynberg
     */
    private final class TitlePageNumberAppender extends AbstractBehavior {

        private static final long serialVersionUID = 1L;
        /** resource key for the message */
        private static final String RES = "PagingNavigation.page";
        /** page number */
        private final int page;

        /**
         * Constructor
         *
         * @param page
         *            page number to use as the ${page} var
         */
        public TitlePageNumberAppender(int page) {
            this.page = page;
        }

        /** {@inheritDoc} */
        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
//            Map<String, String> vars = new MicroMap<String, String>("page",
//                    String.valueOf(page + 1));
//            tag.put("title", PagingNavigator.this.getString(RES, Model.ofMap(vars)));
            tag.put("title", page + 1);
        }
    }
}
