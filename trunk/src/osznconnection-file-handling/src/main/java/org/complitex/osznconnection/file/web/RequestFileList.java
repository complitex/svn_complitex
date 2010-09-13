package org.complitex.osznconnection.file.web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.Page;
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
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.component.*;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.web.resource.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.util.*;

import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.LOAD_ERROR;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class RequestFileList extends TemplatePage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "LoadRequestBean")
    private LoadRequestBean loadRequestBean;

    @EJB(name = "SaveRequestBean")
    private SaveRequestBean saveRequestBean;

    private int waitForStopTimer;

    private final static String ITEM_ID_PREFIX = "item";

    public RequestFileList() {
        super();

        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        RequestFileFilter filterObject = new RequestFileFilter();
        final IModel<RequestFileFilter> filterModel = new CompoundPropertyModel<RequestFileFilter>(filterObject);

        //Фильтр форма
        final Form<RequestFileFilter> filterForm = new Form<RequestFileFilter>("filter_form", filterModel);
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
        final Map<RequestFile, IModel<Boolean>> selectModels = new HashMap<RequestFile, IModel<Boolean>>();

        //Модель данных списка
        final SortableDataProvider<RequestFile> dataProvider = new SortableDataProvider<RequestFile>() {

            @Override
            public Iterator<? extends RequestFile> iterator(int first, int count) {
                RequestFileFilter filter = filterModel.getObject();

                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                List<RequestFile> requestFiles = requestFileBean.getRequestFiles(filter);

                selectModels.clear();
                for (RequestFile rf : requestFiles) {
                    selectModels.put(rf, new Model<Boolean>(false));
                }

                return requestFiles.iterator();
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

        //Контейнер для ajax
        final WebMarkupContainer dataViewContainer = new WebMarkupContainer("request_files_container");
        dataViewContainer.setOutputMarkupId(true);
        filterForm.add(dataViewContainer);

        //Таблица файлов запросов
        final DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1) {

            @Override
            protected void populateItem(Item<RequestFile> item) {
                RequestFile rf = item.getModelObject();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_ID_PREFIX + rf.getId());

                CheckBox checkBox = new CheckBox("selected", selectModels.get(rf));
                checkBox.setVisible(!rf.isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(rf.isProcessing());
                item.add(processing);

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

                item.add(new Label("status", getStringOrKey(rf.getStatus() != LOAD_ERROR ? rf.getStatus() : rf.getStatusDetail())));

                Class<? extends Page> page = null;
                if (rf.isPayment()) {
                    page = PaymentList.class;
                } else if (rf.isBenefit()) {
                    page = BenefitList.class;
                }

                if (page != null) {
                    item.add(new BookmarkablePageLinkPanel<RequestFile>("action_list", getString("action_list"),
                            page, new PageParameters("request_file_id=" + rf.getId())));
                } else {
                    item.add(new EmptyPanel("action_list"));
                }
            }
        };
        dataViewContainer.add(dataView);

        showMessages();

        if (isProcessing()) {
            dataViewContainer.add(newTimer(filterForm, messages));
        }

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_object_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.dbf_record_count", "dbf_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.binded_record_count", "binded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, filterForm));

        //Удалить
        Button delete = new Button("delete") {

            @Override
            public void onSubmit() {
                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        requestFileBean.delete(requestFile);
                        info(getStringFormat("info.deleted", requestFile.getType().ordinal(), requestFile.getName()));
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

                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        requestFiles.add(requestFile);
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

                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        requestFiles.add(requestFile);
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

                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        requestFiles.add(requestFile);
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
                    info(getStringFormat("info.loaded", rf.getType().ordinal(), rf.getName()));
                    break;
                case LOAD_ERROR:
                    switch (rf.getStatusDetail()){
                        case ALREADY_LOADED:
                            Calendar year = Calendar.getInstance();
                            year.setTime(rf.getDate());

                            error(getStringFormat("error.already_loaded", rf.getType().ordinal(), rf.getName(), year.get(Calendar.YEAR)));
                            break;
                        default:
                            highlightError(target, rf);
                            error(getStringFormat("error.load.common", rf.getType().ordinal(), rf.getName()));
                            break;
                    }
                    break;
            }
        }

        //Load Error
        if (loadRequestBean.isError(true)){
            error(getString("error.load.process"));
        }

        //Load completed
        if (loadRequestBean.isCompleted(true)) {
            info(getStringFormat("info.load_completed", loadRequestBean.getProcessedCount(), loadRequestBean.getErrorCount()));
        }

        //Save
        for (RequestFile rf : saveRequestBean.getProcessed(true)){
            switch (rf.getStatus()){
                case SAVED:
                    highlightProcessed(target, rf);
                    info(getStringFormat("info.saved", rf.getType().ordinal(), rf.getName()));
                    break;
                case SAVE_ERROR:
                    highlightError(target, rf);
                    error(getStringFormat("error.save.common", rf.getType().ordinal(), rf.getName()));
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
                    info(getStringFormat("bound.success", bindingFile.getName()));
                    break;
                }
                case BOUND_WITH_ERRORS: {
                    highlightError(target, bindingFile);
                    error(getStringFormat("bound.error", bindingFile.getName()));
                    break;
                }
            }
        }

        //show messages for process operation
        for (RequestFile processingFile : FileExecutorService.get().getInProcessing(true)) {
            switch (processingFile.getStatus()) {
                case PROCESSED: {
                    highlightProcessed(target, processingFile);
                    info(getStringFormat("processed.success", processingFile.getName()));
                    break;
                }
                case PROCESSED_WITH_ERRORS: {
                   highlightError(target, processingFile);
                    error(getStringFormat("processed.error", processingFile.getName()));
                    break;
                }
            }
        }
    }

    private void highlightProcessed(AjaxRequestTarget target, RequestFile requestFile){
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + requestFile.getId() + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, RequestFile requestFile){
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + requestFile.getId() + "')"
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
            }
        };
    }
}
