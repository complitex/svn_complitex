package org.complitex.osznconnection.file.web.pages.benefit;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.complitex.osznconnection.file.calculation.entity.BenefitData;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.service.BenefitFillService;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.Collections;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.10.2010 14:46:58
 */
public class BenefitConnectPanel extends Panel {
    @EJB(name = "BenefitFillService")
    private BenefitFillService benefitFillService;

    private Dialog dialog;
    private Benefit benefit;
    private WebMarkupContainer container;

    public BenefitConnectPanel(String id, final Component update) {
        super(id);
       
        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(660);        
        dialog.setOutputMarkupId(true);
        add(dialog);

        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        dialog.add(container);

        container.add(new FeedbackPanel("messages"));

        Form form = new Form("form");
        container.add(form);

        container.add(new Label("name", new LoadableDetachableModel<String>(){

            @Override
            protected String load() {
                return benefit != null ? benefit.getDisplayName() : "";
            }
        }));

        container.add(new Label("address", new LoadableDetachableModel<String>(){

            @Override
            protected String load() {
                return benefit != null ? benefit.getDisplayAddress() : "";
            }
        }));

        final RadioGroup<BenefitData> radioGroup = new RadioGroup<BenefitData>("radioGroup", new Model<BenefitData>());
        form.add(radioGroup);

        IModel<List<BenefitData>> listViewModel = new LoadableDetachableModel<List<BenefitData>>(){

            @Override
            protected List<BenefitData> load() {
                if (benefit != null){
                    try {
                        return benefitFillService.getBenefitData(benefit);
                    } catch (Exception e) {
                        error("Ошибка соединения с удаленной базой данных");
                    }
                }

                return Collections.emptyList();
            }
        };

        ListView<BenefitData> listView = new ListView<BenefitData>("listView", listViewModel){

            @Override
            protected void populateItem(ListItem<BenefitData> item) {
                BenefitData benefitData = item.getModelObject();

                item.add(new Radio<BenefitData>("radio", item.getModel(), radioGroup));
                item.add(new Label("firstName", benefitData.getFirstName()));
                item.add(new Label("lastName", benefitData.getLastName()));
                item.add(new Label("middleName", benefitData.getMiddleName()));
                item.add(new Label("inn", benefitData.getInn()));
                item.add(new Label("passport", benefitData.getPassportSerial() + " " + benefitData.getPassportNumber()));
                item.add(new Label("orderFamily", benefitData.getOrderFamily()));
                item.add(new Label("code", benefitData.getCode()));
                item.add(new Label("userCount", benefitData.getUserCount()));                
            }
        };
        radioGroup.add(listView);

        AjaxButton connect = new AjaxButton("connect"){

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                dialog.close(target);
                if (benefit != null && radioGroup.getModelObject() != null){
                    benefitFillService.connectBenefit(benefit, radioGroup.getModelObject());
                }                                                                                              
            }
        };
        form.add(connect);

        AjaxButton cancel = new AjaxButton("cancel"){


            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                dialog.close(target);

                if (update != null){
                    target.addComponent(update);
                }

            }
        };
        cancel.setDefaultFormProcessing(false);
        form.add(cancel);
    }

    public void open(AjaxRequestTarget target, Benefit benefit){
        this.benefit = benefit;

        target.addComponent(container);

        dialog.open(target);
    }
}