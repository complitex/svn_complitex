package org.complitex.osznconnection.file.web;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.dictionaryfw.web.component.YearDropDownChoice;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.service.LoadRequestBean;
import org.complitex.osznconnection.file.service.process.ProcessBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 15:43:27
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class RequestFileLoad extends FormTemplatePage {

    @EJB(name = "LoadRequestBean")
    private LoadRequestBean loadRequestBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "ProcessManagerBean")
    private ProcessBean processManagerBean;

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
        Button load = new Button("load") {

            @Override
            public void onSubmit() {
                int f = from.getModelObject();
                int t = to.getModelObject();

                if (t < f) {
                    error(getString("error.to_less_then_from"));
                    return;
                }

                if (!loadRequestBean.isProcessing()) {
                    DomainObject oszn = organizationModel.getObject();
                    loadRequestBean.load(oszn.getId(), organizationStrategy.getDistrictCode(oszn), f, t,
                            year.getModelObject());
                    getSession().info(getString("info.start_loading"));
                } else {
                    getSession().error(getString("error.loading_in_progress"));
                }

                setResponsePage(RequestFileGroupList.class);
            }
        };
        form.add(load);

        //Загрузить
        Button loadTarif = new Button("load_tarif") {

            @Override
            public void onSubmit() {
                int f = from.getModelObject();
                int t = to.getModelObject();

                if (t < f) {
                    error(getString("error.to_less_then_from"));
                    return;
                }

                if (!loadRequestBean.isProcessing()) {
                    DomainObject oszn = organizationModel.getObject();
                    loadRequestBean.loadTarif(oszn.getId(), organizationStrategy.getDistrictCode(oszn), f, t,
                            year.getModelObject());
                    getSession().info(getString("info.start_loading"));
                } else {
                    getSession().error(getString("error.loading_in_progress"));
                }

                setResponsePage(RequestFileList.class);
            }
        };
        form.add(loadTarif);

        //Отмена
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
//                setResponsePage(WelcomePage.class);
//                processManagerBean.test();
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
    }
}
