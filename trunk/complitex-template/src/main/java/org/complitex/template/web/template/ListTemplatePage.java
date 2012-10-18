package org.complitex.template.web.template;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.ILongId;
import org.complitex.dictionary.service.IListBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.component.AjaxFeedbackPanel;
import org.complitex.dictionary.web.component.TextLabel;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.template.web.component.InputPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.complitex.dictionary.util.StringUtil.lowerCamelToUnderscore;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.10.12 15:39
 */
public abstract class ListTemplatePage<T extends ILongId> extends TemplatePage{
    private final Class<? extends IListBean<T>> beanClass;

    protected abstract T newFilterObject(PageParameters pageParameters);

    protected void onPopulateFilter(ListItem<String> item){
    }

    protected void onPopulateData(ListItem<String> item){
    }

    private transient IListBean<T> listBean;

    protected IListBean<T> getListBean(){
        if (listBean == null){
            listBean = EjbBeanLocator.getBean(beanClass);
        }

        return listBean;
    }

    protected List<? extends Component> getActionComponents(String id, T object){
        return new ArrayList<>();
    }

    public ListTemplatePage(final PageParameters pageParameters, Class<? extends IListBean<T>> beanClass,
                            final String... properties) {
        this.beanClass = beanClass;

        //Title
        add(new Label("title", new ResourceModel("title")));

        //Feedback Panel
        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        add(messages);

        //Filter Model
        String pageKey = pageParameters.toString();

        final IModel<FilterWrapper<T>> filterModel = new CompoundPropertyModel<>(
                getTemplateSession().getPreferenceFilter(getClass().getName() + pageKey,
                        FilterWrapper.of(newFilterObject(pageParameters))));

        //Filter Form
        final Form filterForm = new Form<>("filter_form", filterModel);
        filterForm.setOutputMarkupId(true);
        add(filterForm);

        //Filter Reset Button
        AjaxLink filterReset = new AjaxLink("filter_reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                filterModel.setObject(FilterWrapper.of(newFilterObject(pageParameters)));
                target.add(filterForm);
            }
        };
        filterForm.add(filterReset);

        //Filter Find
        AjaxButton filterFind = new AjaxButton("filter_find") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(filterForm);
                target.add(messages);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        };
        filterForm.add(filterFind);

        //Filter Fields
        filterForm.add(new ListView<String>("filter_list", Arrays.asList(properties)){
            @Override
            protected void populateItem(ListItem<String> item) {
                String p = "object." + item.getModelObject();

                item.add(new InputPanel<>("filter_field", new PropertyModel<>(filterModel, p)));

                onPopulateFilter(item);
            }
        });

        //Data Provider
        final DataProvider<T> dataProvider = new DataProvider<T>() {
            @Override
            protected Iterable<T> getData(int first, int count) {
                FilterWrapper<T> filterWrapper = filterModel.getObject();

                filterWrapper.setFirst(first);
                filterWrapper.setCount(count);
                filterWrapper.setSortProperty(getSort().getProperty());
                filterWrapper.setAscending(getSort().isAscending());

                return getListBean().getList(filterWrapper);
            }

            @Override
            protected int getSize() {
                return getListBean().getCount(filterModel.getObject());
            }

            @Override
            public IModel<T> model(T object) {
                return new CompoundPropertyModel<>(object);
            }
        };
        dataProvider.setSort("id", SortOrder.DESCENDING);

        //Data Container
        final WebMarkupContainer dataContainer = new WebMarkupContainer("data_container");
        dataContainer.setOutputMarkupId(true);
        filterForm.add(dataContainer);

        //Data View
        final DataView dataView = new DataView<T>("data_view", dataProvider) {
            @Override
            protected void populateItem(final Item<T> item) {
                item.add(new ListView<String>("data_list", Arrays.asList(properties)) {

                    @Override
                    protected void populateItem(ListItem<String> column) {
                        column.add(new TextLabel("data", new PropertyModel<>(item.getModel(), column.getModelObject())));

                        onPopulateData(column);
                    }
                });

                item.add(new ListView<Component>("data_action_list",
                        getActionComponents("data_action", item.getModelObject())) {
                    @Override
                    protected void populateItem(ListItem<Component> item) {
                        item.add(item.getModelObject());
                    }
                });
            }
        };
        dataContainer.add(dataView);

        //Paging Navigator
        final PagingNavigator paging = new PagingNavigator("paging", dataView, getClass().getName(), filterForm);
        filterForm.add(paging);

        //Headers
        filterForm.add(new ListView<String>("header_list", Arrays.asList(properties)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String property = item.getModelObject();

                ArrowOrderByBorder border = new ArrowOrderByBorder("header_border", lowerCamelToUnderscore(property),
                        dataProvider, dataView, filterForm);
                border.add(new Label("header_label", getString(property)));

                item.add(border);
            }
        });
    }
}
