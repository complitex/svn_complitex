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
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileGroupFilter;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
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
public class GroupList extends TemplatePage {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";

    @EJB(name = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    @EJB(name = "ProcessManagerBean")
    private ProcessManagerBean processManagerBean;

    @EJB(name = "LogBean")
    private LogBean logBean;

    private int waitForStopTimer;
    private int timerIndex = 0;
    private boolean completedDisplayed;

    private final static String ITEM_GROUP_ID_PREFIX = "item";

    private RequestFileLoadPanel requestFileLoadPanel;

    public GroupList(PageParameters parameters){
        super();
        init(parameters.getAsLong("group_id"));
    }

    public GroupList() {
        super();
        init(null);
    }

    private void init(Long groupId) {
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.HIGHLIGHT_JS));

        add(new Label("title", getString("title")));

        final AjaxFeedbackPanel messages = new AjaxFeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Фильтр модель
        RequestFileGroupFilter groupFilterObject = (RequestFileGroupFilter) getFilterObject(null);
        if (groupFilterObject == null){
            groupFilterObject = new RequestFileGroupFilter();
            setFilterObject(groupFilterObject);
        }

        groupFilterObject.setId(groupId);

        final IModel<RequestFileGroupFilter> filterModel = new CompoundPropertyModel<RequestFileGroupFilter>(groupFilterObject);

        //Фильтр форма
        final Form<RequestFileGroupFilter> filterForm = new Form<RequestFileGroupFilter>("filter_form", filterModel);
        add(filterForm);

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();

                RequestFileGroupFilter groupFilterObject = new RequestFileGroupFilter();

                setFilterObject(groupFilterObject);
                filterModel.setObject(groupFilterObject);
            }
        };
        filterForm.add(filter_reset);

        //Id
        filterForm.add(new TextField<String>("id"));

        //Дата загрузки
        filterForm.add(new DatePicker<Date>("loaded"));

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

        //Номер реестра
        filterForm.add(new TextField<String>("registry"));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new YearDropDownChoice("year"));

        //Директория
        filterForm.add(new TextField<String>("directory"));

        //Имя файла начислений
        filterForm.add(new TextField<String>("paymentName"));

        //Имя файла льгот
        filterForm.add(new TextField<String>("benefitName"));

        //Загружено записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", Integer.class));

        //Связано записей
        filterForm.add(new TextField<Integer>("bindedRecordCount", Integer.class));

        //Обработано записей
        filterForm.add(new TextField<Integer>("filledRecordCount", Integer.class));

        //Статус
        filterForm.add(new DropDownChoice<RequestFileGroup.STATUS>("status",
                Arrays.asList(RequestFileGroup.STATUS.values()),
                new IChoiceRenderer<RequestFileGroup.STATUS>() {

                    @Override
                    public Object getDisplayValue(RequestFileGroup.STATUS object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(RequestFileGroup.STATUS object, int index) {
                        return object.name();
                    }
                }));

        //Модель выбранных элементов списка
        final Map<RequestFileGroup, IModel<Boolean>> selectModels = new HashMap<RequestFileGroup, IModel<Boolean>>();

        //Модель данных списка
        final SortableDataProvider<RequestFileGroup> dataProvider = new SortableDataProvider<RequestFileGroup>() {

            @Override
            public Iterator<? extends RequestFileGroup> iterator(int first, int count) {
                RequestFileGroupFilter groupFilter = filterModel.getObject();

                //save preferences to session
                setFilterObject(groupFilter);
                setSortOrder(getSort().isAscending());
                setSortProperty(getSort().getProperty());

                //prepare groupFilter object
                groupFilter.setFirst(first);
                groupFilter.setCount(count);
                groupFilter.setSortProperty(getSort().getProperty());
                groupFilter.setAscending(getSort().isAscending());

                List<RequestFileGroup> requestFileGroups = requestFileGroupBean.getRequestFileGroups(groupFilter);

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
                RequestFileGroup group = item.getModelObject();

                item.setOutputMarkupId(true);
                item.setMarkupId(ITEM_GROUP_ID_PREFIX + group.getId());

                //checkbox
                CheckBox checkBox = new CheckBox("selected", selectModels.get(group));
                checkBox.setVisible(!group.isProcessing() || !isProcessing());
                checkBox.setEnabled(!isProcessing());
                item.add(checkBox);

                //processing image
                Image processing = new Image("processing", new ResourceReference(IMAGE_AJAX_LOADER));
                processing.setVisible(group.isProcessing());
                item.add(processing);

                //id
                item.add(new Label("id", StringUtil.valueOf(group.getId())));

                //loaded date
                item.add(DateLabel.forDatePattern("loaded", new Model<Date>(group.getLoaded()),
                        DateUtil.isCurrentDay(group.getLoaded()) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss"));

                //organization
                DomainObject domainObject = organizationStrategy.findById(group.getOrganizationId());
                String organization = domainObject != null
                        ? organizationStrategy.displayDomainObject(domainObject, getLocale())
                        : "—";
                item.add(new Label("organization", organization));

                //registry, month, year
                item.add(new Label("registry", StringUtil.valueOf(group.getRegistry())));
                item.add(new Label("month", DateUtil.displayMonth(group.getMonth(), getLocale())));
                item.add(new Label("year", StringUtil.valueOf(group.getYear())));

                //Директория
                item.add(new Label("directory", group.getDirectory()));

                //payment name link
                if (group.getPaymentFile() != null){
                    item.add(new BookmarkablePageLinkPanel<RequestFile>("paymentName", group.getPaymentFile().getName(),
                            PaymentList.class, new PageParameters("request_file_id=" + group.getPaymentFile().getId())));
                }else{
                    item.add(new Label("paymentName", "—"));
                }

                //benefit name link
                if (group.getBenefitFile() != null){
                    item.add(new BookmarkablePageLinkPanel<RequestFile>("benefitName", group.getBenefitFile().getName(),
                            BenefitList.class, new PageParameters("request_file_id=" + group.getBenefitFile().getId())));
                }else{
                    item.add(new Label("benefitName", "—"));
                }

                //loaded, binding filled count
                item.add(new Label("loaded_record_count", StringUtil.valueOf(group.getLoadedRecordCount())));
                item.add(new Label("binded_record_count", StringUtil.valueOf(group.getBindedRecordCount())));
                item.add(new Label("filled_record_count", StringUtil.valueOf(group.getFilledRecordCount())));

                String dots = "";
                if (group.isProcessing()){
                    if (processManagerBean.isProcessing()){
                        dots += StringUtil.getDots(timerIndex%7);
                    }
                }

                item.add(new Label("status", getStringOrKey(group.getStatus()) + dots));

            }
        };
        dataViewContainer.add(dataView);

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.id", "id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.registry", "registry", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.directory", "directory", dataProvider, dataView, filterForm));
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
                for (RequestFileGroup group : selectModels.keySet()) {
                    if (selectModels.get(group).getObject()) {
                        try {
                            requestFileGroupBean.delete(group);

                            info(getStringFormat("group.deleted", group.getFullName()));

                            logBean.info(Module.NAME, GroupList.class, RequestFileGroup.class, null, group.getId(),
                                    Log.EVENT.REMOVE, group.getLogChangeList(), "Файлы удалены успешно. Имя объекта: {0}",
                                    group.getLogObjectName());
                        } catch (Exception e) {
                            error(getStringFormat("group.delete_error", group.getFullName()));

                            logBean.error(Module.NAME, GroupList.class, RequestFileGroup.class, null, group.getId(),
                                    Log.EVENT.REMOVE, group.getLogChangeList(), "Ошибка удаления. Имя объекта: {0}",
                                    group.getLogObjectName());
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

        //Связать
        Button bind = new Button("bind") {

            @Override
            public void onSubmit() {
                List<RequestFileGroup> groups = new ArrayList<RequestFileGroup>();

                for (RequestFileGroup g : selectModels.keySet()) {
                    if (selectModels.get(g).getObject()) {
                        groups.add(g);
                        //todo add has been bound
                    }
                }

                completedDisplayed = false;

                processManagerBean.bind(groups);

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
                List<RequestFileGroup> groups = new ArrayList<RequestFileGroup>();


                for (RequestFileGroup g : selectModels.keySet()) {
                    if (selectModels.get(g).getObject()) {
                        groups.add(g);
                        //todo add has been filled
                    }
                }

                completedDisplayed = false;

                processManagerBean.fill(groups);

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
                List<RequestFileGroup> groups = new ArrayList<RequestFileGroup>();

                for (RequestFileGroup g : selectModels.keySet()) {
                    if (selectModels.get(g).getObject()) {
                        groups.add(g);
                    }
                }

                completedDisplayed = false;

                processManagerBean.save(groups);

                selectModels.clear();
                addTimer(dataViewContainer, filterForm, messages);
            }

            @Override
            public boolean isVisible() {
                return !isProcessing();
            }
        };
        filterForm.add(save);

        //Отменить
        Button cancel = new Button("cancel") {

            @Override
            public void onSubmit() {
                processManagerBean.cancel();

                info(getStringOrKey("process.cancel"));
            }

            @Override
            public boolean isVisible() {
                return isProcessing() && !processManagerBean.isStop();
            }
        };
        filterForm.add(cancel);

        //Отобразить сообщения
        showMessages();

        //Запуск таймера
        if (isProcessing()) {
            completedDisplayed = false;
            dataViewContainer.add(newTimer(filterForm, messages));
        }

        //Диалог загрузки
        requestFileLoadPanel = new RequestFileLoadPanel("load_panel",
                new RequestFileLoadPanel.ILoader(){

                    @Override
                    public void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year) {
                        completedDisplayed = false;
                        processManagerBean.loadGroup(organizationId, districtCode, monthFrom, monthTo, year);
                        addTimer(dataViewContainer, filterForm, messages);
                    }
                });

        add(requestFileLoadPanel);
    }

    private boolean isProcessing() {
        return processManagerBean.isProcessing();
    }

    private void showMessages() {
        showMessages(null);
    }

    private void showMessages(AjaxRequestTarget target) {
        for (RequestFileGroup group : processManagerBean.getProcessedGroups(GroupList.class)){
            switch (group.getStatus()){
                case SKIPPED:
                case LOADED:
                case BOUND:
                case FILLED:
                case SAVED:
                    highlightProcessed(target, group);
                    info(getStringFormat("group.processed", group.getFullName(), processManagerBean.getProcess().ordinal()));
                    break;
                case LOAD_ERROR:
                case BIND_ERROR:
                case FILL_ERROR:
                case SAVE_ERROR:
                    highlightError(target, group);
                    info(getStringFormat("group.process_error", group.getFullName(), processManagerBean.getProcess().ordinal()));
                    break;
            }
        }

        for (RequestFile rf : processManagerBean.getLinkError(true)){
            error(getStringFormat("request_file.link_error", rf.getFullName()));
        }

        //Process completed
        if (processManagerBean.isCompleted() && !completedDisplayed) {
            info(getStringFormat("process.done", processManagerBean.getSuccessCount(),
                    processManagerBean.getSkippedCount(), processManagerBean.getErrorCount(),
                    processManagerBean.getProcess().ordinal()));

            completedDisplayed = true;
        }

        //Process canceled
        if (processManagerBean.isCanceled() && !completedDisplayed) {
            info(getStringFormat("process.canceled", processManagerBean.getSuccessCount(),
                    processManagerBean.getSkippedCount(), processManagerBean.getErrorCount(),
                    processManagerBean.getProcess().ordinal()));

            completedDisplayed = true;
        }

        //Process error
        if (processManagerBean.isCriticalError() && !completedDisplayed) {
            error(getStringFormat("process.critical_error", processManagerBean.getSuccessCount(),
                    processManagerBean.getSkippedCount(), processManagerBean.getErrorCount(),
                    processManagerBean.getProcess().ordinal()));

            completedDisplayed = true;
        }
    }

    private void highlightProcessed(AjaxRequestTarget target, RequestFileGroup group){
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_GROUP_ID_PREFIX + group.getId() + "')"
                    + ".animate({ backgroundColor: 'lightgreen' }, 300)"
                    + ".animate({ backgroundColor: '#E0E4E9' }, 700)");
        }
    }

    private void highlightError(AjaxRequestTarget target, RequestFileGroup group){
        if (target != null) {
            target.appendJavascript("$('#" + ITEM_GROUP_ID_PREFIX + group.getId() + "')"
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
