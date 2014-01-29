package org.complitex.organization.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.model.AttributeExampleModel;

import javax.ejb.EJB;
import java.util.List;

import static org.complitex.dictionary.strategy.organization.IOrganizationStrategy.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.01.14 18:49
 */
public class OrganizationPickerPanel extends Panel {
    @EJB
    private LocaleBean localeBean;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    public OrganizationPickerPanel(String id, final IModel<Long> organizationModel, List<Long> organizationTypeIds) {
        super(id);

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);
        add(content);

        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);

        //Example
        final DomainObjectExample example = new DomainObjectExample(NAME, CODE);

        if (organizationTypeIds != null && !organizationTypeIds.isEmpty()) {
            example.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, organizationTypeIds);
        }

        if (isBalanceHolder()){
            example.addAdditionalParam(IOrganizationStrategy.BALANCE_HOLDER_PARAMETER, true);
        }

        filterForm.add(new TextField<>("nameFilter", new AttributeExampleModel(example, NAME)));
        filterForm.add(new TextField<>("codeFilter", new AttributeExampleModel(example, CODE)));

        final RadioGroup<Long> radioGroup = new RadioGroup<>("radioGroup", organizationModel);
        radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                //update model
            }
        });
        filterForm.add(radioGroup);

        DataView<DomainObject> data = new DataView<DomainObject>("data", getDataProvider(example)) {

            @Override
            protected void populateItem(Item<DomainObject> item) {
                final DomainObject organization = item.getModelObject();

                item.add(new Radio<>("radio", Model.of(organization.getId()), radioGroup));
                item.add(new Label("name", AttributeUtil.getStringCultureValue(organization, NAME, getLocale())));
                item.add(new Label("code", organizationStrategy.getUniqueCode(organization)));
            }
        };
        radioGroup.add(data);

        PagingNavigator pagingNavigator = new PagingNavigator("navigator", data, content);
        content.add(pagingNavigator);

        IndicatingAjaxButton find = new IndicatingAjaxButton("find", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(content);

                onFind(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(find);
    }

    protected DataProvider<DomainObject> getDataProvider(final DomainObjectExample example) {
        return new DataProvider<DomainObject>() {

                @Override
                protected Iterable<? extends DomainObject> getData(int first, int count) {
                    example.setLocaleId(localeBean.convert(getLocale()).getId());
                    example.setStart(first);
                    example.setSize(count);

                    return organizationStrategy.find(example);
                }

                @Override
                protected int getSize() {
                    example.setLocaleId(localeBean.convert(getLocale()).getId());
                    return organizationStrategy.count(example);
                }
            };
    }

    protected boolean isBalanceHolder(){
        return false;
    }

    protected void onFind(AjaxRequestTarget target){
//        target.appendJavaScript(CENTER_DIALOG_JS.asString(
//                ImmutableMap.of("dialogId", lookupDialog.getMarkupId())));

    }
}
