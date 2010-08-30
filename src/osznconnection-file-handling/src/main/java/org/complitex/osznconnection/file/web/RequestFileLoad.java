package org.complitex.osznconnection.file.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.dictionaryfw.web.component.YearDropDownChoice;
import org.complitex.osznconnection.commons.web.pages.welcome.WelcomePage;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.service.LoadRequestBean;
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
    @EJB(name = "LoadRequestBean")
    private LoadRequestBean loadRequestBean;

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
        final DropDownChoice<Integer> from = new MonthDropDownChoice("from", new Model<Integer>());
        from.setRequired(true);
        form.add(from);

        final DropDownChoice<Integer> to = new MonthDropDownChoice("to", new Model<Integer>());
        to.setRequired(true);
        form.add(to);

        final DropDownChoice<Integer> year = new YearDropDownChoice("year", new Model<Integer>());        
        year.setRequired(true);
        form.add(year);

        //Загрузить
        Button load = new Button("load"){
            @Override
            public void onSubmit() {
                DomainObject oszn = organizationModel.getObject();
                loadRequestBean.load(oszn.getId(), 
                        organizationStrategy.getDistrictCode(oszn), organizationStrategy.getUniqueCode(oszn),
                        from.getModelObject(), to.getModelObject(), year.getModelObject());
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
