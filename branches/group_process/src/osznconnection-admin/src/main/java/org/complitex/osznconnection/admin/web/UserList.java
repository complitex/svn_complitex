package org.complitex.osznconnection.admin.web;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.User;
import org.complitex.dictionaryfw.entity.UserGroup;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.web.component.AttributeColumnsPanel;
import org.complitex.dictionaryfw.web.component.AttributeFiltersPanel;
import org.complitex.dictionaryfw.web.component.AttributeHeadersPanel;
import org.complitex.dictionaryfw.web.component.BookmarkablePageLinkPanel;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.admin.service.UserBean;
import org.complitex.osznconnection.admin.service.UserFilter;
import org.complitex.osznconnection.commons.web.component.toolbar.AddUserButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 15:03:45
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class UserList extends TemplatePage {
    @EJB(name = "UserBean")
    private UserBean userBean;

    public UserList() {
        super();

        add(new Label("title", new ResourceModel("title")));
        add(new FeedbackPanel("messages"));

        //Фильтр
        UserFilter filterObject = userBean.newUserFilter();
        final IModel<UserFilter> filterModel = new Model<UserFilter>(filterObject);

        final Form filterForm = new Form("filter_form");
        filterForm.setOutputMarkupId(true);
        add(filterForm);

        Link filterReset = new Link("reset"){
            @Override
            public void onClick() {
                filterForm.clearInput();

                UserFilter filterObject = filterModel.getObject();
                filterObject.setLogin(null);
                filterObject.setGroupName(null);
                for (AttributeExample attributeExample : filterObject.getAttributeExamples()){
                    attributeExample.setValue(null);
                }                                                   
            }
        };
        filterForm.add(filterReset);

        filterForm.add(new TextField<String>("login", new PropertyModel<String>(filterModel, "login")));
        filterForm.add(new AttributeFiltersPanel("user_info", filterObject.getAttributeExamples()));

        //Модель
        final SortableDataProvider<User> dataProvider = new SortableDataProvider<User>(){
            @Override
            public Iterator<? extends User> iterator(int first, int count) {
                UserFilter filter = filterModel.getObject();

                filter.setFirst(first);
                filter.setCount(count);
                try {
                    filter.setSortProperty(null);
                    filter.setSortAttributeTypeId(Long.valueOf(getSort().getProperty()));
                } catch (NumberFormatException e) {
                    filter.setSortProperty(getSort().getProperty());
                    filter.setSortAttributeTypeId(null);
                }                
                filter.setAscending(getSort().isAscending());

                return userBean.getUsers(filterModel.getObject()).iterator();
            }

            @Override
            public int size() {
                return userBean.getUsersCount(filterModel.getObject());
            }

            @Override
            public IModel<User> model(User object) {
                return new Model<User>(object);
            }
        };
        dataProvider.setSort("login", true);

        //Таблица
        DataView<User> dataView = new DataView<User>("users", dataProvider, 10){
            @Override
            protected void populateItem(Item<User> item) {
                User user = item.getModelObject();

                item.add(new Label("login", user.getLogin()));

                List<Attribute> attributeColumns = userBean.getUserInfoStrategy().getAttributeColumns(user.getUserInfo());
                item.add(new AttributeColumnsPanel("user_info", attributeColumns));

                item.add(new Label("usergroup", getDisplayGroupNames(user)));

                item.add(new BookmarkablePageLinkPanel<User>("action_edit", getString("action_edit"), UserEdit.class,
                                new PageParameters("user_id=" + user.getId())));
            }
        };
        filterForm.add(dataView);

        //Названия колонок и сортировка
        filterForm.add(new ArrowOrderByBorder("header.login", "login", dataProvider, dataView, filterForm));
        filterForm.add(new AttributeHeadersPanel("header.user_info", userBean.getUserInfoStrategy().getListColumns(),
                dataProvider, dataView, filterForm));

        DropDownChoice<UserGroup.GROUP_NAME> usergroups = new DropDownChoice<UserGroup.GROUP_NAME>("usergroups",
                new PropertyModel<UserGroup.GROUP_NAME>(filterModel, "groupName"),
                new ListModel<UserGroup.GROUP_NAME>(Arrays.asList(UserGroup.GROUP_NAME.values())),
                new IChoiceRenderer<UserGroup.GROUP_NAME>(){

                    @Override
                    public Object getDisplayValue(UserGroup.GROUP_NAME object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(UserGroup.GROUP_NAME object, int index) {
                        return object.name();
                    }
                });
        filterForm.add(usergroups);

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, getClass().getName(), filterForm));
    }

    /**
     * Генерирует строку списка групп пользователей для отображения
     * @param user Пользователь
     * @return Список групп
     */
    private String getDisplayGroupNames(User user) {
        if (user.getUserGroups() == null || user.getUserGroups().isEmpty()) {
            return getString("blocked");
        }

         StringBuilder sb = new StringBuilder();

        for (Iterator<UserGroup> it = user.getUserGroups().iterator();;) {
            sb.append(getString(it.next().getGroupName().name()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    @Override
    protected List<ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList((ToolbarButton) new AddUserButton(id) {

            @Override
            protected void onClick() {
                setResponsePage(UserEdit.class);
            }
        });
    }
}
