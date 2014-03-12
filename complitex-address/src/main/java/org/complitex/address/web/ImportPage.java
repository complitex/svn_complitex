package org.complitex.address.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.ImportService;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.organization.entity.OrganizationImportFile;
import org.complitex.template.web.component.LocalePicker;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.03.11 16:20
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class ImportPage extends TemplatePage {

    @EJB
    private ImportService importService;
    @EJB
    private LocaleBean localeBean;
    private int stopTimer = 0;
    private final IModel<List<IImportFile>> dictionaryModel;
    private final IModel<Locale> localeModel;

    public ImportPage() {
        add(new Label("title", new ResourceModel("title")));

        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        dictionaryModel = new ListModel<>();

        container.add(new FeedbackPanel("messages"));

        Form<Void> form = new Form<Void>("form");
        container.add(form);

        //Справочники
        List<IImportFile> dictionaryList = new ArrayList<IImportFile>();
        Collections.addAll(dictionaryList, OrganizationImportFile.values());
        Collections.addAll(dictionaryList, AddressImportFile.values());

        form.add(new CheckBoxMultipleChoice<>("address", dictionaryModel, dictionaryList,
                new IChoiceRenderer<IImportFile>() {

                    @Override
                    public Object getDisplayValue(IImportFile object) {
                        return object.getFileName() + getStatus(importService.getMessage(object));
                    }

                    @Override
                    public String getIdValue(IImportFile object, int index) {
                        return object.name();
                    }
                }));

        localeModel = new Model<>(localeBean.getSystemLocale());
        form.add(new LocalePicker("localePicker", localeModel, false));

        //Кнопка импортировать
        Button process = new Button("process") {

            @Override
            public void onSubmit() {
                if (!importService.isProcessing()) {
                    importService.process(dictionaryModel.getObject(), localeBean.convert(localeModel.getObject()).getId());
                    container.add(newTimer());
                }
            }

            @Override
            public boolean isVisible() {
                return !importService.isProcessing();
            }
        };
        form.add(process);

        //Ошибки
        container.add(new Label("error", new LoadableDetachableModel<Object>() {

            @Override
            protected Object load() {
                return importService.getErrorMessage();
            }
        }) {

            @Override
            public boolean isVisible() {
                return importService.isError();
            }
        });
    }

    private AjaxSelfUpdatingTimerBehavior newTimer() {
        stopTimer = 0;

        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                if (!importService.isProcessing()) {

                    dictionaryModel.setObject(null);

                    stopTimer++;
                }

                if (stopTimer > 2) {
                    if (importService.isSuccess()) {
                        info(getString("success"));
                    }

                    stop(target);
                }
            }
        };
    }

    private String getStatus(ImportMessage im) {
        if (im != null) {
            if (im.getIndex() < 1 && !importService.isProcessing()) {
                return " - " + getStringOrKey("error");
            } else if (im.getIndex() == im.getCount()) {
                return " - " + getStringFormat("complete", im.getIndex());
            } else {
                return " - " + getStringFormat("processing", im.getIndex(), im.getCount());
            }
        }

        return "";
    }
}
