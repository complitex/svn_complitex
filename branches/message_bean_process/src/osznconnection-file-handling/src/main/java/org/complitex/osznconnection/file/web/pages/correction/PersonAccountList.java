/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.PersonAccount;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;
import org.complitex.osznconnection.file.service.PersonAccountLocalBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;
import java.util.Iterator;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class PersonAccountList extends TemplatePage {

    @EJB(name = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    private IModel<PersonAccountExample> example;

    public PersonAccountList() {
        init();
    }

    private void clearExample() {
        example.setObject(newExample());
    }

    private PersonAccountExample newExample() {
        return new PersonAccountExample();
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);
        final Form filterForm = new Form("filterForm");
        content.add(filterForm);

        example = new Model<PersonAccountExample>(newExample());

        final SortableDataProvider<PersonAccount> dataProvider = new SortableDataProvider<PersonAccount>() {

            @Override
            public Iterator<? extends PersonAccount> iterator(int first, int count) {
                example.getObject().setAsc(getSort().isAscending());
                if (!Strings.isEmpty(getSort().getProperty())) {
                    example.getObject().setOrderByClause(getSort().getProperty());
                }
                example.getObject().setStart(first);
                example.getObject().setSize(count);
                example.getObject().setLocale(getLocale().getLanguage());
                return personAccountLocalBean.find(example.getObject()).iterator();
            }

            @Override
            public int size() {
                example.getObject().setAsc(getSort().isAscending());
                return personAccountLocalBean.count(example.getObject());
            }

            @Override
            public IModel<PersonAccount> model(PersonAccount object) {
                return new Model<PersonAccount>(object);
            }
        };
        dataProvider.setSort("", true);

        filterForm.add(new TextField<String>("firstNameFilter", new PropertyModel<String>(example, "firstName")));
        filterForm.add(new TextField<String>("middleNameFilter", new PropertyModel<String>(example, "middleName")));
        filterForm.add(new TextField<String>("lastNameFilter", new PropertyModel<String>(example, "lastName")));
        filterForm.add(new TextField<String>("cityFilter", new PropertyModel<String>(example, "city")));
        filterForm.add(new TextField<String>("streetFilter", new PropertyModel<String>(example, "street")));
        filterForm.add(new TextField<String>("buildingNumberFilter", new PropertyModel<String>(example, "buildingNumber")));
        filterForm.add(new TextField<String>("buildingCorpFilter", new PropertyModel<String>(example, "buildingCorp")));
        filterForm.add(new TextField<String>("apartmentFilter", new PropertyModel<String>(example, "apartment")));
        filterForm.add(new TextField<String>("accountNumberFilter", new PropertyModel<String>(example, "accountNumber")));
        filterForm.add(new TextField<String>("ownNumSrFilter", new PropertyModel<String>(example, "ownNumSr")));
        filterForm.add(new TextField<String>("osznFilter", new PropertyModel<String>(example, "oszn")));
        filterForm.add(new TextField<String>("calculationCenterFilter", new PropertyModel<String>(example, "calculationCenter")));

        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                clearExample();
                target.addComponent(content);
            }
        };
        filterForm.add(reset);
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(content);
            }
        };
        filterForm.add(submit);

        DataView<PersonAccount> data = new DataView<PersonAccount>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<PersonAccount> item) {
                PersonAccount personAccount = item.getModelObject();
                item.add(new Label("firstName", personAccount.getFirstName()));
                item.add(new Label("middleName", personAccount.getMiddleName()));
                item.add(new Label("lastName", personAccount.getLastName()));
                item.add(new Label("city", personAccount.getCity()));
                item.add(new Label("street", personAccount.getStreet()));
                item.add(new Label("buildingNumber", personAccount.getBuildingNumber()));
                item.add(new Label("buildingCorp", !Strings.isEmpty(personAccount.getBuildingCorp()) ? personAccount.getBuildingCorp()
                        : ""));
                item.add(new Label("apartment", personAccount.getApartment()));
                item.add(new Label("accountNumber", personAccount.getAccountNumber()));
                item.add(new Label("ownNumSr", personAccount.getOwnNumSr()));
                item.add(new Label("oszn", personAccount.getOszn()));
                item.add(new Label("calculationCenter", personAccount.getCalculationCenter()));
                item.add(new BookmarkablePageLink<PersonAccountEdit>("edit", PersonAccountEdit.class,
                        new PageParameters(ImmutableMap.of(PersonAccountEdit.CORRECTION_ID, personAccount.getId()))));
            }
        };
        filterForm.add(data);

        filterForm.add(new ArrowOrderByBorder("lastNameHeader", PersonAccountLocalBean.OrderBy.LAST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("firstNameHeader", PersonAccountLocalBean.OrderBy.FIRST_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("middleNameHeader", PersonAccountLocalBean.OrderBy.MIDDLE_NAME.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("cityHeader", PersonAccountLocalBean.OrderBy.CITY.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("streetHeader", PersonAccountLocalBean.OrderBy.STREET.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("buildingNumberHeader", PersonAccountLocalBean.OrderBy.BUILDING_NUMBER.getOrderBy(), dataProvider,
                data, content));
        filterForm.add(new ArrowOrderByBorder("buildingCorpHeader", PersonAccountLocalBean.OrderBy.BUILDING_CORP.getOrderBy(), dataProvider,
                data, content));
        filterForm.add(new ArrowOrderByBorder("apartmentHeader", PersonAccountLocalBean.OrderBy.APARTMENT.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("accountNumberHeader", PersonAccountLocalBean.OrderBy.ACCOUNT_NUMBER.getOrderBy(), dataProvider,
                data, content));
        filterForm.add(new ArrowOrderByBorder("ownNumSrHeader", PersonAccountLocalBean.OrderBy.OWN_NUM_SR.getOrderBy(), dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("osznHeader", organizationStrategy.getOrderByExpression("pa.`oszn_id`", getLocale().getLanguage(), null),
                dataProvider, data, content));
        filterForm.add(new ArrowOrderByBorder("calculationCenterHeader",
                organizationStrategy.getOrderByExpression("pa.`calc_center_id`", getLocale().getLanguage(), null), dataProvider, data, content));

        content.add(new PagingNavigator("navigator", data, getClass().getName(), content));
    }
}

