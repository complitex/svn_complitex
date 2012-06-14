package org.complitex.address.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.time.Duration;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.service.AddressImportService;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.ResourceModel;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.03.11 16:20
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class ImportPage extends TemplatePage {

    @EJB
    private AddressImportService addressImportService;
    private int stopTimer = 0;
    private final IModel<List<IImportFile>> dictionaryModel;

    public ImportPage() {
        add(new Label("title", new ResourceModel("title")));

        final WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        dictionaryModel = new ListModel<IImportFile>();

        container.add(new FeedbackPanel("messages"));

        Form<Void> form = new Form<Void>("form");
        container.add(form);

        //Справочники
        List<IImportFile> dictionaryList = new ArrayList<IImportFile>();
        Collections.addAll(dictionaryList, AddressImportFile.values());

        form.add(new CheckBoxMultipleChoice<IImportFile>("address", dictionaryModel, dictionaryList,
                new IChoiceRenderer<IImportFile>() {

                    @Override
                    public Object getDisplayValue(IImportFile object) {
                        return object.getFileName() + getStatus(addressImportService.getMessage(object));
                    }

                    @Override
                    public String getIdValue(IImportFile object, int index) {
                        return object.name();
                    }
                }));


        //Кнопка импортировать
        Button process = new Button("process") {

            @Override
            public void onSubmit() {
                if (!addressImportService.isProcessing()) {
                    addressImportService.process(dictionaryModel.getObject());
                    container.add(newTimer());
                }
            }

            @Override
            public boolean isVisible() {
                return !addressImportService.isProcessing();
            }
        };
        form.add(process);

        //Ошибки
        container.add(new Label("error", new LoadableDetachableModel<Object>() {

            @Override
            protected Object load() {
                return addressImportService.getErrorMessage();
            }
        }) {

            @Override
            public boolean isVisible() {
                return addressImportService.isError();
            }
        });
    }

    private AjaxSelfUpdatingTimerBehavior newTimer() {
        stopTimer = 0;

        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                if (!addressImportService.isProcessing()) {

                    dictionaryModel.setObject(null);

                    stopTimer++;
                }

                if (stopTimer > 2) {
                    if (addressImportService.isSuccess()) {
                        info(getString("success"));
                    }
                    stop();
                }
            }
        };
    }

    private String getStatus(ImportMessage im) {
        if (im != null) {
            if (im.getIndex() < 1 && !addressImportService.isProcessing()) {
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
