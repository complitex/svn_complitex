package org.complitex.address.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.model.AttributeExampleModel;

import javax.ejb.EJB;
import java.util.List;

import static org.complitex.address.strategy.district.DistrictStrategy.CODE;
import static org.complitex.address.strategy.district.DistrictStrategy.NAME;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.01.14 1:53
 */
public class DistrictSelectPanel extends Panel {
    @EJB
    private DistrictStrategy districtStrategy;

    public DistrictSelectPanel(String id, IModel<List<Long>> districtModel) {
        super(id);

        final DomainObjectExample example = new DomainObjectExample(NAME, CODE);

        final Form form = new Form("form");
        form.setOutputMarkupId(true);
        add(form);

        form.add(new TextField<>("name", new AttributeExampleModel(example, NAME)));
        form.add(new TextField<>("code", new AttributeExampleModel(example, CODE)));

        CheckGroup checkGroup = new CheckGroup<>("check_group", districtModel);
        form.add(checkGroup);

        DataProvider<DomainObject> dataProvider = new DataProvider<DomainObject>() {
            @Override
            protected Iterable<? extends DomainObject> getData(int first, int count) {
                return districtStrategy.find(example, first, count);
            }

            @Override
            protected int getSize() {
                return districtStrategy.count(example);
            }
        };

        DataView<DomainObject> dataView = new DataView<DomainObject>("data_view", dataProvider) {
            @Override
            protected void populateItem(Item<DomainObject> item) {
                final DomainObject domainObject = item.getModelObject();

                item.add(new Check<>("check", new PropertyModel<>(domainObject, "id")));
                item.add(new Label("name", AttributeUtil.getStringCultureValue(domainObject, NAME, getLocale())));
                item.add(new Label("code", AttributeUtil.getStringValue(domainObject, CODE)));
            }
        };
        checkGroup.add(dataView);

        form.add(new PagingNavigator("navigator", dataView, form));

        form.add(new AjaxButton("filter") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        });
    }
}
