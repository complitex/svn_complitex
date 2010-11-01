package org.complitex.osznconnection.file.web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.component.*;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.FileExecutorService;
import org.complitex.osznconnection.file.service.LoadRequestBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.complitex.osznconnection.file.service.SaveRequestBean;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.web.resource.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.io.File;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class RequestFileGroupList extends TemplatePage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";

    @EJB(name = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "LoadRequestBean")
    private LoadRequestBean loadRequestBean;

    @EJB(name = "SaveRequestBean")
    private SaveRequestBean saveRequestBean;

    private int waitForStopTimer;
    private int timerIndex = 0;

    private final static String ITEM_GROUP_ID_PREFIX = "item";

    public RequestFileGroupList(PageParameters parameters){
        super();
        init(parameters.getAsLong("request_file_id"));
    }

    public RequestFileGroupList() {
        super();
        init(null);
    }

    private void init(Long requestFileId) {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        RequestFileFilter filterObject = (RequestFileFilter) getFilterObject(null);
        if (filterObject == null){
            filterObject = new RequestFileFilter();
            setFilterObject(filterObject);
        }

        filterObject.setId(requestFileId);

        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filterObject);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();

                RequestFileFilter filterObject = new RequestFileFilter();

                setFilterObject(filterObject);
                filterModel.setObject(filterObject);
            }
        };
        filterForm.add(filter_reset);

        //Id
        filterForm.add(new TextField<String>("id"));

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

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

        //Номер реестра
        filterForm.add(new TextField<String>("registry"));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

        //Имя файла начислений
        filterForm.add(new TextField<String>("paymentName"));

        //Имя файла льгот
        filterForm.add(new TextField<String>("benefitName"));

        //Загружено записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", new Model<Integer>(), Integer.class));

        //Связано записей
        filterForm.add(new TextField<Integer>("bindedRecordCount", new Model<Integer>(), Integer.class));

        //Обработано записей
        filterForm.add(new TextField<Integer>("filledRecordCount", new Model<Integer>(), Integer.class));

        //Статус
        filterForm.add(new DropDownChoice<RequestFile.STATUS>("status",
                Arrays.asList(RequestFile.STATUS.values()),
                new IChoiceRenderer<RequestFile.STATUS>() {

                    @Override
                    public Object getDisplayValue(RequestFile.STATUS object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(RequestFile.STATUS object, int index) {
                        return object.name();
                    }
                }));

        //Модель выбранных элементов списка
        final Map<RequestFileGroup, IModel<Boolean>> selectModels = new HashMap<RequestFileGroup, IModel<Boolean>>();

        //Модель данных списка
        final SortableDataProvider<RequestFileGroup> dataProvider = new SortableDataProvider<RequestFileGroup>() {

            @Override
            public Iterator<? extends RequestFileGroup> iterator(int first, int count) {
                RequestFileFilter filter = filterModel.getObject();

                //save preferences to session
                setFilterObject(filter);
                setSortOrder(getSort().isAscending());
                setSortProperty(getSort().getProperty());

                //prepare filter object
                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<RequestFileGroup> requestFileGroups = requestFileGroupBean.getRequestFileGroups(filter);

                selectModels.clear();
                for (RequestFileGroup rfg : requestFileGroups) {
                    selectModels.put(rfg, new Model<Boolean>(false));
                }

                return requestFileGroups.iterator();
            }

            @Override
            public int size() {
                return requestFileGroupBean.getRequestFileGroupsCount(filterModel.getObject());
            }

            @Override
            public IModel<RequestFileGroup> model(RequestFileGroup object) {
                return new Model<RequestFileGroup>(object);
            }
        };
        dataProvider.setSort(getSortProperty("loaded"), getSortOrder(false));

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_groups_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFileGroup> dataView = new DataView<RequestFileGroup>("request_files_groups", dataProvider, 1) {

            @Override
            protected void populateItem(Item<RequestFileGroup> item) {
                RequestFileGroup rfg = item.getModelObject();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_GROUP_ID_PREFIX + rfg.getId());

                //checkbox
                CheckBox checkBox = new CheckBox("selected", selectModels.get(rfg));
                checkBox.setVisible(!rfg.isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                //processing image
                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(rfg.isProcessing());
                item.add(processing);

                //id
                item.add(new Label("id", StringUtil.valueOf(rfg.getId())));

                //loaded date
                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(rfg.getLoaded()),
                        DateUtil.isCurrentDay(rfg.getLoaded()) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss"));

                //organization
                DomainObject domainObject = organizationStrategy.findById(rfg.getOrganizationId());
                String organization = domainObject != null
                        ? organizationStrategy.displayDomainObject(domainObject, getLocale())
                        : "—";
                item.add(new Label("organization", organization));

                //registry, month, year
                item.add(new Label("registry", StringUtil.valueOf(rfg.getRegistry())));
                item.add(new Label("month", DateUtil.displayMonth(rfg.getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(rfg.getYear())));

                //payment name link
                if (rfg.getPaymentFile() != null){
                    item.add(new BookmarkablePageLinkPanel<RequestFile>("paymentName", rfg.getPaymentFile().getName(),
                            PaymentList.class, new PageParameters("request_file_id=" + rfg.getPaymentFile().getId())));
                }else{
                    item.add(new Label("paymentName", "—"));
                }

                //benefit name link
                if (rfg.getBenefitFile() != null){
                    item.add(new BookmarkablePageLinkPanel<RequestFile>("benefitName", rfg.getBenefitFile().getName(),
                            BenefitList.class, new PageParameters("request_file_id=" + rfg.getBenefitFile().getId())));
                }else{
                    item.add(new Label("benefitName", "—"));
                }

                //loaded, binding filled count
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rfg.getLoadedRecordCount())));
                item.add(new Label("binded_record_count", StringUtil.valueOf(rfg.getBindedRecordCount())));
                item.add(new Label("filled_record_count", StringUtil.valueOf(rfg.getFilledRecordCount())));

                //status
                String detail = "";
//                if (rfg.getStatusDetail() != null){
//                    detail = ": " + getStringOrKey(rfg.getStatusDetail());
//                }

                if (rfg.isProcessing()){
                    if (FileExecutorService.get().isBinding()){
                        detail += StringUtil.getDots(timerIndex%7);
                    }

                    if (loadRequestBean.isProcessing()){
                        detail += StringUtil.getDots(timerIndex%7);
                    }
                }

                item.add(new Label("status", getStringOrKey(rfg.getStatus()) + detail));

            }
        };
        dataViewContainer.add(dataView);

        showMessages();

        if (isProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.registry", "registry", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.paymentName", "paymentName", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.benefitName", "benefitName", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.binded_record_count", "binded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.filled_record_count", "filled_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, getClass().getName(), filterForm));

        //Удалить
        Button delete = new Button("delete") {

            @Override
            public void onSubmit() {
                for (RequestFileGroup requestFileGroup : selectModels.keySet()) {
                    if (selectModels.get(requestFileGroup).getObject()) {
                        requestFileGroupBean.delete(requestFileGroup);
                        if (requestFileGroup.getBenefitFile() != null){
                            info(getStringFormat("info.deleted", RequestFile.TYPE.BENEFIT.ordinal(),
                                    requestFileGroup.getBenefitFile().getName(), requestFileGroup.getBenefitFile().getDirectory(), File.separator));
                        }
                        if (requestFileGroup.getPaymentFile() != null){
                            info(getStringFormat("info.deleted", RequestFile.TYPE.PAYMENT.ordinal(),
                                    requestFileGroup.getPaymentFile().getName(), requestFileGroup.getPaymentFile().getDirectory(), File.separator));
                        }
                    }
                }
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(delete);

       //Связать
        Button bind = new Button("bind") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFileGroup requestFileGroup : selectModels.keySet()) {
                    if (selectModels.get(requestFileGroup).getObject()) {
                        for (RequestFile requestFile : requestFileGroup.getRequestFiles()) {
                            requestFiles.add(requestFile);
                            if (requestFile.getStatus() == RequestFile.STATUS.BINDED || requestFile.getStatus() == RequestFile.STATUS.PROCESSED
                                    || requestFile.getStatus() == RequestFile.STATUS.PROCESSED_WITH_ERRORS
                                    || requestFile.getStatus() == RequestFile.STATUS.SAVED
                                    || requestFile.getStatus() == RequestFile.STATUS.SAVE_ERROR) {
                                warn(getStringFormat("has_been_bound", requestFile.getName(), requestFile.getDirectory(), File.separator));
                            }
                        }
                    }
                }

                FileExecutorService.get().bind(requestFiles);
                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(bind);

        //Process
        Button process = new Button("process") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFileGroup requestFileGroup : selectModels.keySet()) {
                    if (selectModels.get(requestFileGroup).getObject()) {
                        for (RequestFile requestFile : requestFileGroup.getRequestFiles()) {
                            requestFiles.add(requestFile);
                            if (requestFile.getStatus() == RequestFile.STATUS.PROCESSED || requestFile.getStatus() == RequestFile.STATUS.SAVED
                                    || requestFile.getStatus() == RequestFile.STATUS.SAVE_ERROR) {
                                warn(getStringFormat("has_been_processed", requestFile.getName(), requestFile.getDirectory(), File.separator));
                            }
                        }
                    }
                }

                FileExecutorService.get().process(requestFiles);
                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(process);

        //Выгрузить
        Button save = new Button("save") {

            @Override
            public void onSubmit() {
                List<RequestFile> requestFiles = new ArrayList<RequestFile>();

                for (RequestFileGroup requestFileGroup : selectModels.keySet()) {
                    if (selectModels.get(requestFileGroup).getObject()) {
                        requestFiles.addAll(requestFileGroup.getRequestFiles());
                    }
                }

                info(getString("info.start_saving"));

                saveRequestBean.save(requestFiles);

                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(save);
    }

    private boolean isProcessing() {
        return loadRequestBean.isProcessing()
                || FileExecutorService.get().isBinding()
                || FileExecutorService.get().isProcessing()
                || saveRequestBean.isProcessing();
    }

    private void showMessages() {
        if (loadRequestBean.isError(true)){
            error(getString("error.load.process"));
        }

        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        //Load
        for (RequestFile rf : loadRequestBean.getProcessed(true)) {
            switch (rf.getStatus()) {
                case LOADED:
                    highlightProcessed(target, rf);
                    info(getStringFormat("info.loaded", rf.getType().ordinal(), rf.getName(), rf.getDirectory(), File.separator));
                    break;
                case SKIPPED:                    
                    info(getStringFormat("info.already_loaded", rf.getType().ordinal(), rf.getName(), rf.getDirectory(), File.separator, rf.getYear()));
                    break;
                case LOAD_ERROR:
                    highlightError(target, rf);
                    error(getStringOrKey(rf.getStatus())
                            + " " + getStringFormat("error.load.common", rf.getType().ordinal(), rf.getName(), rf.getDirectory(), File.separator)
                            + ". " + getStringOrKey(rf.getStatusDetail()));
                    break;
            }
        }

        //Load Error
        if (loadRequestBean.isError(true)){
            error(getString("error.load.process"));
        }

        //Load completed
        if (loadRequestBean.isCompleted(true)) {
            info(getStringFormat("info.load_completed", loadRequestBean.getProcessedCount(),
                    loadRequestBean.getSkippedCount(), loadRequestBean.getErrorCount()));
        }

        //Save
        for (RequestFile rf : saveRequestBean.getProcessed(true)){
            switch (rf.getStatus()){
                case SAVED:
                    highlightProcessed(target, rf);
                    info(getStringFormat("info.saved", rf.getType().ordinal(), rf.getName(), rf.getDirectory(), File.separator));
                    break;
                case SAVE_ERROR:
                    highlightError(target, rf);
                    error(getStringFormat("error.save.common", rf.getType().ordinal(), rf.getName(), rf.getDirectory(), File.separator));
                    break;
            }
        }

        //Save Error
        if (saveRequestBean.isError(true)){
            error(getString("error.save.process"));
        }

        //Save completed
        if (saveRequestBean.isCompleted(true)) {
            info(getStringFormat("info.save_completed", saveRequestBean.getProcessedCount(), saveRequestBean.getErrorCount()));
        }

        //show messages for binding operation
        for (RequestFile bindingFile : FileExecutorService.get().getInBinding(true)) {
            switch (bindingFile.getStatus()) {
                case BINDED: {
                    highlightProcessed(target, bindingFile);
                    info(getStringFormat("bound.success", bindingFile.getName(), bindingFile.getDirectory(), File.separator));
                    break;
                }
                case BOUND_WITH_ERRORS: {
                    highlightError(target, bindingFile);
                    error(getStringFormat("bound.error", bindingFile.getName(), bindingFile.getDirectory(), File.separator));
                    break;
                }
            }
        }

        //show messages for process operation
        for (RequestFile processingFile : FileExecutorService.get().getInProcessing(true)) {
            switch (processingFile.getStatus()) {
                case PROCESSED: {
                    highlightProcessed(target, processingFile);
                    info(getStringFormat("processed.success", processingFile.getName(), processingFile.getDirectory(), File.separator));
                    break;
                }
                case PROCESSED_WITH_ERRORS: {
                    highlightError(target, processingFile);
                    error(getStringFormat("processed.error", processingFile.getName(), processingFile.getDirectory(), File.separator));
                    break;
                }
            }
        }
    }

    private void highlightProcessed(AjaxRequestTarget target, RequestFile requestFile){
        if (target != null) {            
            target.appendJavascript("$('#" + ITEM_GROUP_ID_PREFIX + requestFile.getGroupId() + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, RequestFile requestFile){
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_GROUP_ID_PREFIX + requestFile.getGroupId() + "')"
                    + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void addTimer(WebMarkupContainer dataViewContainer, Form<?> filterForm, AjaxFeedbackPanel messages) {
        boolean needCreateNewTimer = true;

        List<AjaxSelfUpdatingTimerBehavior> timers = null;
        timers = Lists.newArrayList(Iterables.filter(dataViewContainer.getBehaviors(), AjaxSelfUpdatingTimerBehavior.class));
        if (timers != null && !timers.isEmpty()) {
            for (AjaxSelfUpdatingTimerBehavior timer : timers) {
                if (!timer.isStopped()) {
                    needCreateNewTimer = false;
                    break;
                }
            }
        }
        if (needCreateNewTimer) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }
    }

    private AjaxSelfUpdatingTimerBehavior newTimer(final Form<?> filterForm, final AjaxFeedbackPanel messages) {
        waitForStopTimer = 0;
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                showMessages(target);

                if (!isProcessing() && ++waitForStopTimer > 2) {
                    this.stop();
                    target.addComponent(filterForm);
                } else {
                    //update feedback messages panel
                    target.addComponent(messages);
                }

                timerIndex++;
            }
        };
    }
}
