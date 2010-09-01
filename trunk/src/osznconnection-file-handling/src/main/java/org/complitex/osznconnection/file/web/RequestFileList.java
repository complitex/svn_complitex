package org.complitex.osznconnection.file.web;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.component.BookmarkablePageLinkPanel;
import org.complitex.dictionaryfw.web.component.DatePicker;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.dictionaryfw.web.component.YearDropDownChoice;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.FileExecutorService;
import org.complitex.osznconnection.file.service.LoadRequestBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileFilter;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class RequestFileList extends TemplatePage{
    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader1.gif";

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "LoadRequestBean")
    private LoadRequestBean loadRequestBean;

    public RequestFileList() {
        super();

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        //Фильтр модель
        RequestFileFilter filterObject = new RequestFileFilter();
        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filterObject);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        filterForm.setOutputMarkupId(true);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();
                filterModel.setObject(new RequestFileFilter());
            }
        };
        filterForm.add(filter_reset);

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

        //Имя
        filterForm.add(new TextField<String>("name"));

        //Организация
        filterForm.add(new DropDownChoice<DomainObject>("organization",
                organizationStrategy.getAllOSZNs(), new IChoiceRenderer<DomainObject>() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }

            @Override
            public String getIdValue(DomainObject object, int index) {
                return String.valueOf(object.getId());
            }
        }));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

        //Всего записей
        filterForm.add(new TextField<Integer>("dbfRecordCount", new Model<Integer>(), Integer.class));

        //Загружено записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", new Model<Integer>(), Integer.class));

        //Связано записей
        filterForm.add(new TextField<Integer>("bindedRecordCount", new Model<Integer>(), Integer.class));

        //Статус
        filterForm.add(new DropDownChoice<RequestFile.STATUS>("status",
                Arrays.asList(RequestFile.STATUS.values()),
                new IChoiceRenderer<RequestFile.STATUS>(){

                    @Override
                    public Object getDisplayValue(RequestFile.STATUS object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(RequestFile.STATUS object, int index) {
                        return object.name();
                    }
                }));

        //Модель данных списка
        final SortableDataProvider<RequestFile> dataProvider = new SortableDataProvider<RequestFile>(){

            @Override
            public Iterator<? extends RequestFile> iterator(int first, int count) {
                RequestFileFilter filter = filterModel.getObject();

                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                return requestFileBean.getRequestFiles(filter).iterator();
            }

            @Override
            public int size() {
                return requestFileBean.size(filterModel.getObject());
            }

            @Override
            public IModel<RequestFile> model(RequestFile object) {
                return new Model<RequestFile>(object);
            }
        };
        dataProvider.setSort("loaded", false);

        //todo paging debug
        final Map<RequestFile, IModel<Boolean>> selectModels = new HashMap<RequestFile, IModel<Boolean>>();

        //Таблица файлов запросов
        final DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1){

            @Override
            protected void populateItem(Item<RequestFile> item) {
                RequestFile rf = item.getModelObject();

                IModel<Boolean> checkModel = new Model<Boolean>(false);
                selectModels.put(rf, checkModel);
                item.add(new CheckBox("selected", checkModel));

                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(rf.getLoaded()), "dd.MM.yy HH:mm:ss"));
                item.add(new Label("name", rf.getName()));
                
                DomainObject domainObject = organizationStrategy.findById(rf.getOrganizationObjectId());
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                item.add(DateLabel.forDatePattern("month", new Model<Date>(rf.getDate()), "MMMM"));
                item.add(DateLabel.forDatePattern("year", new Model<Date>(rf.getDate()), "yyyy"));
                item.add(new Label("dbf_record_count", StringUtil.valueOf(rf.getDbfRecordCount())));
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rf.getLoadedRecordCount(), rf.getDbfRecordCount())));
                item.add(new Label("binded_record_count", StringUtil.valueOf(rf.getBindedRecordCount(), rf.getDbfRecordCount())));
                item.add(new Label("status", getStringOrKey(rf.getStatus().name())));

                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(rf.isProcessing());
                item.add(processing);

                Class<? extends Page> page = null;
                if (rf.isPayment()){
                    page = PaymentList.class;
                }else if (rf.isBenefit()){
                    page = BenefitList.class;
                }

                item.add(new BookmarkablePageLinkPanel<RequestFile>("action_list", getString("action_list"),
                        page, new PageParameters("request_file_id=" + rf.getId())));
            }
        };
        filterForm.add(dataView);
                           
        //Таймер
        if (loadRequestBean.isLoading()){
            filterForm.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)){
                @Override
                protected void onPostProcessTarget(AjaxRequestTarget target) {
                    target.addComponent(filterForm);

                    if (!loadRequestBean.isLoading()){
                        this.stop();
                        setResponsePage(RequestFileList.class);
                        info(getString("info.loaded"));
                    }
                }
            });
        }
        
        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.dbf_record_count", "dbf_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.binded_record_count", "binded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, filterForm));

        //Связать
        Button process = new Button("bind"){
            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFile requestFile : selectModels.keySet()){
                    if (selectModels.get(requestFile).getObject()){
                        requestFiles.add(requestFile);
                    }
                }

                FileExecutorService.get().bind(requestFiles);
            }
        };
        filterForm.add(process);
    }
}
