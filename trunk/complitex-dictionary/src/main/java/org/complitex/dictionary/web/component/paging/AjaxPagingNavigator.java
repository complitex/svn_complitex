package org.complitex.dictionary.web.component.paging;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.web.DictionaryFwSession;
import org.complitex.dictionary.web.component.image.StaticImage;

import java.util.Arrays;
import java.util.List;

import static org.complitex.dictionary.entity.PreferenceKey.CURRENT_PAGE;

/**
 * @author Pavel Sknar
 */
public class AjaxPagingNavigator extends org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator {

    private static final List<Long> SUPPORTED_PAGE_SIZES = Arrays.asList(10L, 20L, 30L, 50L, 100L);

    private List<IPagingNavigatorListener> listeners = Lists.newArrayList();

    public AjaxPagingNavigator(String id, IPageable pageable) {
        super(id, pageable);
    }

    public AjaxPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final String page = getPage().getPageClass().toString();

        final IPageable pageable = getPageable();

        long rowsPerPage = getSession().getPreferenceLong(page, PreferenceKey.ROWS_PER_PAGE, SUPPORTED_PAGE_SIZES.get(0));
        long currentPage = getSession().getPreferenceLong(page, CURRENT_PAGE, 0L);
        ((IPageableItems)getPageable()).setItemsPerPage(rowsPerPage);
        getPageable().setCurrentPage(currentPage);
        getPagingNavigation().setViewSize(7);

        //new page form
        Form<Void> newPageForm = new Form<>("newPageForm");
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
                            pageable.setCurrentPage(0);
                        } else if (newPageNumber > pageable.getPageCount()) {
                            pageable.setCurrentPage(pageable.getPageCount() - 1);
                        } else {
                            pageable.setCurrentPage(newPageNumber - 1);
                        }

                        getSession().putPreference(page, CURRENT_PAGE, pageable.getCurrentPage(), true);
                    }
                }
            }
        };
        TextField<String> newPageNumber = new TextField<>("newPageNumber", newPageNumberModel);
        AjaxButton goToPage = new AjaxButton("goToPage", newPageForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                updateComponents(target);

            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };

        newPageForm.add(newPageNumber);
        newPageForm.add(goToPage);
        add(newPageForm);

        //page size
        IModel<Long> pageSizeModel = new Model<Long>() {

            @Override
            public Long getObject() {
                return ((IPageableItems)getPageable()).getItemsPerPage();
            }

            @Override
            public void setObject(Long rowsPerPage) {
                getSession().putPreference(page, PreferenceKey.ROWS_PER_PAGE, rowsPerPage, true);
                ((IPageableItems)getPageable()).setItemsPerPage(rowsPerPage);
            }
        };
        DropDownChoice<Long> pageSize = new DropDownChoice<>("pageSize", pageSizeModel, SUPPORTED_PAGE_SIZES);
        pageSize.setNullValid(false);

        pageSize.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                //update model - pageSizeModel
                updateComponents(target);
            }
        });
        add(pageSize);

        //all pages region
        WebMarkupContainer allPagesRegion = new WebMarkupContainer("allPagesRegion");
        Label allPages = new Label("allPages", new AbstractReadOnlyModel<Long>() {

            @Override
            public Long getObject() {
                return pageable.getPageCount();
            }
        });
        allPagesRegion.add(allPages);
        add(allPagesRegion);

        //preference
        IPagingNavigatorListener listener = new IPagingNavigatorListener() {
            @Override
            public void onChangePage() {
                //preference
                if (page != null) {
                    getSession().putPreference(page, CURRENT_PAGE, getPageable().getCurrentPage(), true);
                }
            }
        };
        addListener(listener);
        ((AjaxPagingNavigation)getPagingNavigation()).addListener(listener);
    }

    private void updateComponents(AjaxRequestTarget target) {
        //update model - newPageNumberModel
        target.add(this);
        IPageable pageable = getPageable();
        if (pageable instanceof WebMarkupContainer) {
            target.add((WebMarkupContainer)pageable);
        }
        //listeners
        for (IPagingNavigatorListener listener : listeners) {
            listener.onChangePage();
        }
    }

    @Override
    protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
        return addImage(id, new AjaxPagingNavigationLink(id, pageable, pageNumber) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                super.onClick(target);

                //listeners
                for (IPagingNavigatorListener listener : listeners) {
                    listener.onChangePage();
                }
            }
        });
    }

    @Override
    protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment) {
        return addImage(id, new AjaxPagingNavigationIncrementLink(id, pageable, increment) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                super.onClick(target);

                //listeners
                for (IPagingNavigatorListener listener : listeners) {
                    listener.onChangePage();
                }
            }
        });
    }

    @Override
    protected AjaxPagingNavigation newNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        return new AjaxPagingNavigation(id, pageable, labelProvider);
    }

    private AbstractLink addImage(String id, AbstractLink link) {
        if (StringUtils.equals("first", id)) {
            link.add(new StaticImage("firstImage", new SharedResourceReference("images/pageNavStart.gif")));
        } else if (StringUtils.equals("prev", id)) {
            link.add(new StaticImage("prevImage", new SharedResourceReference("images/pageNavPrev.gif")));
        } else if (StringUtils.equals("next", id)) {
            link.add(new StaticImage("nextImage", new SharedResourceReference("images/pageNavNext.gif")));
        } else if (StringUtils.equals("last", id)) {
            link.add(new StaticImage("lastImage", new SharedResourceReference("images/pageNavEnd.gif")));
        }
        return link;
    }

    @Override
    public DictionaryFwSession getSession() {
        return (DictionaryFwSession) super.getSession();
    }

    public void addListener(IPagingNavigatorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IPagingNavigatorListener listener) {
        listeners.remove(listener);
    }
}
