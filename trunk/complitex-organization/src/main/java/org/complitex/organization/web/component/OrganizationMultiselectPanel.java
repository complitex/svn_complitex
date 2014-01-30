package org.complitex.organization.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
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
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.01.14 20:28
 */
public class OrganizationMultiselectPanel extends Panel{
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    public OrganizationMultiselectPanel(String id, final IModel<List<DomainObject>> model) {
        super(id);

        final CheckGroup<DomainObject> checkGroup = new CheckGroup<>("check_group", new ListModel<DomainObject>());
        checkGroup.setOutputMarkupId(true);
        add(checkGroup);

        ListView<DomainObject> listView = new ListView<DomainObject>("list_view", model){
            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                String name = organizationStrategy.displayShortNameAndCode(item.getModelObject(), getLocale());

                item.add(new Check<>("check"));
                item.add(new Label("name", Model.of(name)));
            }
        };
        checkGroup.add(listView);


        add(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {

            }
        });

        add(new AjaxSubmitLink("delete") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                model.getObject().removeAll(checkGroup.getModelObject());

                target.add(checkGroup);
            }
        });












    }
}
