package org.complitex.osznconnection.file.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.osznconnection.commons.web.pages.welcome.WelcomePage;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.service.RequestFileBean;

import javax.ejb.EJB;
import java.util.Calendar;
import org.apache.wicket.model.IModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 15:43:27
 */
public class RequestFileLoad extends FormTemplatePage{
    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    public RequestFileLoad() {
        super();

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        //Форма
        Form form = new Form("form");
        add(form);

        //Организация
        final IModel<DomainObject> organizationModel = new Model<DomainObject>();
        IChoiceRenderer<DomainObject> renderer = new IChoiceRenderer<DomainObject>() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }

            @Override
            public String getIdValue(DomainObject object, int index) {
                return String.valueOf(object.getId());
            }
        };

        DropDownChoice<DomainObject> organization = new DropDownChoice<DomainObject>("organization", organizationModel,
                organizationStrategy.getAllOSZNs(), renderer);
        organization.setRequired(true);
        form.add(organization);

        //Период
        final DropDownChoice<Integer> from = new MonthDropDownChoice("from");
        from.setRequired(true);
        form.add(from);

        final DropDownChoice<Integer> to = new MonthDropDownChoice("to");
        to.setRequired(true);
        form.add(to);

        final TextField<Integer> year = new TextField<Integer>("year", new Model<Integer>(), Integer.class);
        year.add(new RangeValidator<Integer>(1991, Calendar.getInstance().get(Calendar.YEAR)));
        year.setRequired(true);
        form.add(year);

        //Загрузить
        Button load = new Button("load"){
            @Override
            public void onSubmit() {
                super.onSubmit();

                //validation to > from
                if (from.getModelObject() > to.getModelObject()){

                    error("to > from");
                    return;
                }

                DomainObject oszn = organizationModel.getObject();
                requestFileBean.load(oszn.getId(), organizationStrategy.getDistrictCode(oszn), organizationStrategy.getUniqueCode(oszn),
                        from.getModelObject(), to.getModelObject());
            }
        };
        form.add(load);

        //Отмена
        Button cancel = new Button("cancel"){
            @Override
            public void onSubmit() {
                setResponsePage(WelcomePage.class);
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
    }
}
