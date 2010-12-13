package org.complitex.osznconnection.file.web;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.dictionaryfw.web.component.YearDropDownChoice;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.12.10 16:52
 */
public class RequestFileLoadPanel extends Panel {
    @EJB(name = "ProcessManagerBean")
    private ProcessManagerBean processManagerBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    private final Dialog dialog;

    public static interface ILoader extends Serializable{
        void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year);
    }

    public RequestFileLoadPanel(String id, String title, final ILoader loader) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(480);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setOutputMarkupId(true);
        dialog.setOutputMarkupPlaceholderTag(true);
        dialog.setTitle(title);
        add(dialog);

        dialog.add(new FeedbackPanel("messages"));

        //Форма
        Form form = new Form("form");
        dialog.add(form);

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
                    loader.load(oszn.getId(), organizationStrategy.getDistrictCode(oszn), f, t, year.getModelObject());
                    getSession().info(getString("info.start_loading"));
                } else {
                    getSession().error(getString("error.loading_in_progress"));
                }

                dialog.setAutoOpen(false);
                dialog.close();
            }
        };
        form.add(load);

        //Отмена
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
                dialog.setAutoOpen(false);
                dialog.close();
            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
    }

    public void open(){
        dialog.setAutoOpen(true);
    }
}

