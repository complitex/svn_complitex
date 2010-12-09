package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.StatusDetail;
import org.complitex.osznconnection.file.entity.example.PaymentExample;
import org.complitex.osznconnection.file.service.StatusDetailBean;

import javax.ejb.EJB;
import java.util.List;
import org.complitex.osznconnection.file.service.StatusRenderService;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.11.10 15:49
 */
public class StatusDetailPanel extends Panel {
    
    @EJB(name = "StatusDetailBean")
    private StatusDetailBean statusDetailBean;

    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;

    public StatusDetailPanel(String id, final RequestFile requestFile, final IModel<PaymentExample> exampleModel,
                             final Component... update) {
        super(id);

        WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        IModel<List<StatusDetail>> model = new LoadableDetachableModel<List<StatusDetail>>() {
            @Override
            protected List<StatusDetail> load() {
                return statusDetailBean.getPaymentStatusDetails(requestFile);
            }
        };

        ListView<StatusDetail> rootStatusDetails = new ListView<StatusDetail>("root_status_details", model){

            @Override
            protected void populateItem(ListItem<StatusDetail> rootItem) {
                final StatusDetail root = rootItem.getModelObject();

                //Контейнер для ajax обновления вложенного списка
                final WebMarkupContainer statusDetailContainer = new WebMarkupContainer("status_detail_container");
                statusDetailContainer.setOutputMarkupId(true);
                statusDetailContainer.setOutputMarkupPlaceholderTag(true);
                rootItem.add(statusDetailContainer);

                AjaxLink expandRoot = new AjaxLink("expand_root"){

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        for (Component component : update){
                            target.addComponent(component);
                        }

                        filter(requestFile, exampleModel, root);

                        if (root.getStatusDetails() != null && !root.getStatusDetails().isEmpty()) {
                            statusDetailContainer.setVisible(!statusDetailContainer.isVisible());
                            target.addComponent(statusDetailContainer);
                        }
                    }
                };
                rootItem.add(expandRoot);

                String rootName = statusRenderService.displayStatus(root.getRequestStatus(), getLocale()) +
                        " (" + root.getCount() + ")";
                expandRoot.add(new Label("root_name", rootName));

                ListView<StatusDetail> statusDetails = new ListView<StatusDetail>("status_details",
                        root.getStatusDetails()){

                    @Override
                    protected void populateItem(ListItem<StatusDetail> item) {
                        final StatusDetail statusDetail = item.getModelObject();

                        AjaxLink filterLink = new AjaxLink("filter_link"){

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                filter(requestFile, exampleModel, statusDetail);

                                for (Component component : update){
                                    target.addComponent(component);
                                }
                            }
                        };
                        item.add(filterLink);

                        filterLink.add(new Label("name", statusDetail.getDisplayName()));
                    }
                };
                statusDetailContainer.setVisible(false);
                statusDetailContainer.add(statusDetails);
            }
        };

        container.add(rootStatusDetails);
    }

    private void filter(RequestFile requestFile, IModel<PaymentExample> exampleModel, StatusDetail statusDetail){
        if (exampleModel == null){
            return;
        }

        PaymentExample paymentExample = new PaymentExample();
        paymentExample.setRequestFileId(requestFile.getId());
        paymentExample.setStatus(statusDetail.getRequestStatus());
        exampleModel.setObject(paymentExample);

        switch (statusDetail.getRequestStatus()){
            case ACCOUNT_NUMBER_NOT_FOUND:
            case MORE_ONE_ACCOUNTS:
            case WRONG_ACCOUNT_NUMBER:
                paymentExample.setAccount(statusDetail.getAccount());
                break;
            case CITY_UNRESOLVED_LOCALLY:
            case CITY_UNRESOLVED:
                paymentExample.setCity(statusDetail.getCity());
                break;
            case STREET_UNRESOLVED_LOCALLY:
            case STREET_UNRESOLVED:
                paymentExample.setCity(statusDetail.getCity());
                paymentExample.setStreet(statusDetail.getStreet());
                break;
            case BUILDING_UNRESOLVED_LOCALLY:
            case BUILDING_UNRESOLVED:
                paymentExample.setCity(statusDetail.getCity());
                paymentExample.setStreet(statusDetail.getStreet());
                paymentExample.setBuilding(statusDetail.getBuilding());
                break;
            case BUILDING_CORP_UNRESOLVED:
                paymentExample.setCity(statusDetail.getCity());
                paymentExample.setStreet(statusDetail.getStreet());
                paymentExample.setBuilding(statusDetail.getBuilding());
                paymentExample.setCorp(statusDetail.getCorp());
                break;
        }
    }
}
