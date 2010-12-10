package org.complitex.osznconnection.file.web;

import java.util.List;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.dictionaryfw.web.component.YearDropDownChoice;
import org.complitex.osznconnection.commons.web.pages.welcome.WelcomePage;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.DomainObjectDisableAwareRenderer;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 15:43:27
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class RequestFileLoad extends FormTemplatePage {

    @EJB(name = "ProcessManagerBean")
    private ProcessManagerBean processManagerBean;

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
        IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        final IModel<DomainObject> organizationModel = new Model<DomainObject>();
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };

        DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<DomainObject>("organization", organizationModel,
                osznsModel, renderer);
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

                if (!processManagerBean.isProcessing()) {
                    DomainObject oszn = organizationModel.getObject();
                    processManagerBean.loadGroup(oszn.getId(), organizationStrategy.getDistrictCode(oszn), f, t,
                            year.getModelObject());
                    getSession().info(getString("info.start_loading"));
                } else {
                    getSession().error(getString("error.loading_in_progress"));
                }

                setResponsePage(GroupList.class);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //eh...
                }
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

                if (!processManagerBean.isProcessing()) {
                    DomainObject oszn = organizationModel.getObject();
                    processManagerBean.loadTarif(oszn.getId(), organizationStrategy.getDistrictCode(oszn), f, t,
                            year.getModelObject());
                    getSession().info(getString("info.tarif_start_loading"));
                } else {
                    getSession().error(getString("error.loading_in_progress"));
                }

                setResponsePage(TarifFileList.class);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //eh...
                }
            }
        };
        form.add(loadTarif);

        //Отмена
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
                setResponsePage(WelcomePage.class);
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
    }
}
