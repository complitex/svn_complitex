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
import java.util.Arrays;
import java.util.Calendar;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 15:43:27
 */
public class RequestFileLoad extends FormTemplatePage{
    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    public RequestFileLoad() {
        super();

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        //Форма
        Form form = new Form("form");
        add(form);

        //Организация
        DropDownChoice organization = new DropDownChoice<String>("organization", new Model<String>(), Arrays.asList("XXXX"));
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

                requestFileBean.load(from.getModelObject(), to.getModelObject());
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
