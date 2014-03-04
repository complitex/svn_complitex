package org.complitex.organization.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.01.14 20:28
 */
public class OrganizationMultiselectPanel extends Panel{
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    public OrganizationMultiselectPanel(String id, final IModel<List<DomainObject>> model, List<Long> organizationTypeIds){
        this(id, model, organizationTypeIds, false);
    }

    public OrganizationMultiselectPanel(String id, final IModel<List<DomainObject>> model, List<Long> organizationTypeIds,
                                        boolean balanceHolder) {
        super(id);

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        container.setOutputMarkupPlaceholderTag(true);
        add(container);

        final CheckGroup<DomainObject> checkGroup = new CheckGroup<>("check_group", new ListModel<>(new ArrayList<DomainObject>()));
        container.add(checkGroup);

        ListView<DomainObject> listView = new ListView<DomainObject>("list_view", model){
            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                String name = organizationStrategy.displayShortNameAndCode(item.getModelObject(), getLocale());

                item.add(new Check<>("check", Model.of(item.getModelObject())));
                item.add(new Label("name", Model.of(name)));
            }
        };
        checkGroup.add(listView);

        final OrganizationSelectPanel organizationSelectPanel = new OrganizationSelectPanel("organization_select",
                organizationTypeIds, balanceHolder){
            @Override
            protected void onSelect(AjaxRequestTarget target, DomainObject domainObject) {
                model.getObject().add(domainObject);

                target.add(container.setVisible(true));
                target.add(setVisible(false));

                OrganizationMultiselectPanel.this.onSelect(target, domainObject);
            }

            @Override
            protected void onCancel(AjaxRequestTarget target) {
                target.add(container.setVisible(true));
                target.add(setVisible(false));

                OrganizationMultiselectPanel.this.onCancel(target);
            }
        };
        organizationSelectPanel.setOutputMarkupId(true);
        organizationSelectPanel.setOutputMarkupPlaceholderTag(true);
        organizationSelectPanel.setVisible(false);
        add(organizationSelectPanel);

        container.add(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(container.setVisible(false));
                target.add(organizationSelectPanel.setVisible(true));

                onAdd(target);
            }
        });

        container.add(new AjaxSubmitLink("delete") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                model.getObject().removeAll(checkGroup.getModelObject());

                target.add(container);
            }
        });
    }

    protected void onAdd(AjaxRequestTarget target){
    }

    protected void onSelect(AjaxRequestTarget target, DomainObject domainObject){
    }

    protected void onCancel(AjaxRequestTarget target){
    }

}
