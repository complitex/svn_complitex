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
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.component.*;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.web.component.LoadButton;
import org.complitex.osznconnection.file.web.pages.benefit.BenefitList;
import org.complitex.osznconnection.file.web.pages.payment.PaymentList;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;
import org.complitex.osznconnection.web.resource.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class TarifFileList extends TemplatePage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";

    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "ProcessManagerBean")
    private ProcessManagerBean processManagerBean;

    @EJB(name = "LogBean")
    private LogBean logBean;

    private int waitForStopTimer;

    private boolean completedDisplayed = false;

    private final static String ITEM_ID_PREFIX = "item";

    private RequestFileLoadPanel requestFileLoadPanel;

    public TarifFileList(PageParameters parameters) {
        super();
        init(parameters.getAsLong("request_file_id"));
    }

    public TarifFileList() {
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
        if (filterObject == null) {
            filterObject = new RequestFileFilter();
            filterObject.setType(RequestFile.TYPE.TARIF);

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

                RequestFileFilter groupFilterObject = new RequestFileFilter();
                groupFilterObject.setType(RequestFile.TYPE.TARIF);

                setFilterObject(groupFilterObject);
                filterModel.setObject(groupFilterObject);
            }
        };
        filterForm.add(filter_reset);

        //Id
        filterForm.add(new TextField<String>("id"));

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

        //Имя
        filterForm.add(new TextField<String>("name"));

        //Организация
        IModel<List<DomainObject>> osznsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return organizationStrategy.getAllOSZNs(getLocale());
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        filterForm.add(new DisableAwareDropDownChoice<DomainObject>("organization", osznsModel, renderer));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

        //Всего записей
        filterForm.add(new TextField<Integer>("dbfRecordCount", new Model<Integer>(), Integer.class));

        //Загружено записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", new Model<Integer>(), Integer.class));

        //Модель выбранных элементов списка
        final Map<RequestFile, IModel<Boolean>> selectModels = new HashMap<RequestFile, IModel<Boolean>>();

        //Модель данных списка
        final SortableDataProvider<RequestFile> dataProvider = new SortableDataProvider<RequestFile>() {

            @Override
            public Iterator<? extends RequestFile> iterator(int first, int count) {
                RequestFileFilter filter = filterModel.getObject();

                //save preferences to session
                setFilterObject(filter);
                setSortOrder(getSort().isAscending());
                setSortProperty(getSort().getProperty());

                //prepare groupFilter object
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
        dataProvider.setSort(getSortProperty("loaded"), getSortOrder(false));

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
                checkBox.setVisible(!isLoading(rf) || !isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(isLoading(rf));
                item.add(processing);

                item.add(new Label("id", StringUtil.valueOf(rf.getId())));

                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(rf.getLoaded()), "dd.MM.yy HH:mm:ss"));
                item.add(new Label("name", rf.getFullName()));

                DomainObject domainObject = organizationStrategy.findById(rf.getOrganizationId());
                String organization = organizationStrategy.displayDomainObject(domainObject, getLocale());
                item.add(new Label("organization", organization));

                item.add(new Label("month", DateUtil.displayMonth(rf.getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(rf.getYear())));
                item.add(new Label("dbf_record_count", StringUtil.valueOf(rf.getDbfRecordCount())));
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rf.getLoadedRecordCount(), rf.getDbfRecordCount())));

                String status = "";

                if (isLoaded(rf)){
                    status = getStringOrKey("status.loaded");
                }else if (isLoading(rf)){
                    status = getStringOrKey("status.loading");
                }else if (isLoadError(rf)){
                    status = getStringOrKey("status.load_error");
                }

                item.add(new Label("status", status));

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
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.dbf_record_count", "dbf_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, getClass().getName(), filterForm));

        //Удалить
        Button delete = new Button("delete") {

            @Override
            public void onSubmit() {
                for (RequestFile requestFile : selectModels.keySet()) {
                    if (selectModels.get(requestFile).getObject()) {
                        try {
                            requestFileBean.delete(requestFile);

                            info(getStringFormat("info.deleted", requestFile.getFullName()));

                            logBean.info(Module.NAME, TarifFileList.class, RequestFileGroup.class, null, requestFile.getId(),
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Файл удален успешно. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                        } catch (Exception e) {
                            error(getStringFormat("error.delete", requestFile.getFullName()));

                            logBean.error(Module.NAME, GroupList.class, RequestFileGroup.class, null, requestFile.getId(),
                                    Log.EVENT.REMOVE, requestFile.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    requestFile.getLogObjectName());
                            break;
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

         //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new RequestFileLoadPanel.ILoader(){

                    @Override
                    public void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year) {
                        processManagerBean.loadTarif(organizationId, districtCode, monthFrom, monthTo, year);
                        addTimer(dataViewContainer, filterForm, messages);
                    }
                });

        add(requestFileLoadPanel);
    }

    private boolean isProcessing() {
        return processManagerBean.isProcessing();
    }

    private boolean isLoading(RequestFile requestFile){
        return processManagerBean.isProcessing()
                && ProcessManagerBean.PROCESS.LOAD.equals(processManagerBean.getProcess())
                && requestFile.getLoadedRecordCount() < requestFile.getDbfRecordCount();
    }

    private boolean isLoaded(RequestFile requestFile){
        return requestFile.getLoadedRecordCount().equals(requestFile.getDbfRecordCount())
                && requestFile.getDbfRecordCount() != 0;
    }

    private boolean isLoadError(RequestFile requestFile){
        return !processManagerBean.isProcessing() && !isLoaded(requestFile);
    }

    private void showMessages() {

        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        for (RequestFile rf : processManagerBean.getProcessedTarifFiles(TarifFileList.class)){

            if (rf.getLoadedRecordCount().equals(rf.getDbfRecordCount()) && rf.getDbfRecordCount() != 0){
                info(getStringFormat("tarif.loaded", rf.getFullName()));
                highlightProcessed(target, rf);
            }else {
                error(getStringFormat("tarif.load_error", rf.getFullName()));
                highlightError(target, rf);
            }
        }

        //Process completed
        if (processManagerBean.isCompleted() && !completedDisplayed) {
            info(getStringFormat("process.done", processManagerBean.getSuccessCount(),
                    processManagerBean.getSkippedCount(), processManagerBean.getErrorCount()));

            completedDisplayed = true;
        }

        //Process error
        if (processManagerBean.isCriticalError() && !completedDisplayed) {
            error(getStringFormat("process.critical_error", processManagerBean.getSuccessCount(),
                    processManagerBean.getSkippedCount(), processManagerBean.getErrorCount()));

            completedDisplayed = true;
        }
    }

    private void highlightProcessed(AjaxRequestTarget target, RequestFile requestFile) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + requestFile.getId() + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, RequestFile requestFile) {
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_ID_PREFIX + requestFile.getId() + "')"
                    + ".animate({ backgroundColor: 'darksalmon' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
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

    @Override
    protected List<ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList((ToolbarButton) new LoadButton(id) {

            @Override
            protected void onClick() {
                requestFileLoadPanel.open();
            }
        });
    }
}
