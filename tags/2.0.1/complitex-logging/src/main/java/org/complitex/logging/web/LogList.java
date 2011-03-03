package org.complitex.logging.web;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogManager;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import org.complitex.logging.service.LogFilter;
import org.complitex.logging.service.LogListBean;
import org.complitex.logging.web.component.LogChangePanel;
import org.complitex.resources.WebCommonResourceInitializer;

import javax.ejb.EJB;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 19.08.2010 13:08:10
 *
 * todo добавить механизм регистрации внешних файлов свойств
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class LogList extends TemplatePage{
    private final static String IMAGE_ARROW_TOP = "images/arrow2top.gif";
    private final static String IMAGE_ARROW_BOTTOM = "images/arrow2bot.gif";

    @EJB(name = "LogListBean")
    private LogListBean logListBean;

    public LogList() {
        super();
        init();
    }

    private void init(){

        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.IE_SELECT_FIX_JS));

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        //Фильтр модель
        LogFilter filterObject = new LogFilter();

        final IModel<LogFilter> filterModel = new CompoundPropertyModel<LogFilter>(filterObject);

        //Фильтр форма
        final Form<LogFilter> filterForm = new Form<LogFilter>("filter_form", filterModel);
        filterForm.setOutputMarkupId(true);
        add(filterForm);

        final Set<Long> expandModel = new HashSet<Long>();

        Link filter_reset = new Link("filter_reset") {

            @Override
            public void onClick() {
                filterForm.clearInput();
                filterModel.setObject(new LogFilter());
            }
        };
        filterForm.add(filter_reset);

        //Date
        DatePicker<Date> date = new DatePicker<Date>("date");
        filterForm.add(date);

        //Login
        filterForm.add(new TextField<String>("login"));

        //Module
        filterForm.add(new DropDownChoice<String>("module", logListBean.getModules(),
                new IChoiceRenderer<String>() {

                    @Override
                    public Object getDisplayValue(String object) {
                        return getStringOrKey(object);
                    }

                    @Override
                    public String getIdValue(String object, int index) {
                        return object;
                    }
                }));


        //Controller Class
        filterForm.add(new DropDownChoice<String>("controller", logListBean.getControllers(),
                new IChoiceRenderer<String>() {

                    @Override
                    public Object getDisplayValue(String object) {
                        return getStringOrKey(object);
                    }

                    @Override
                    public String getIdValue(String object, int index) {
                        return object;
                    }
                }));

        //Model Class
        filterForm.add(new DropDownChoice<String>("model", logListBean.getModels(),
                new IChoiceRenderer<String>() {

                    @Override
                    public Object getDisplayValue(String object) {
                        return getStringOrKey(object);
                    }

                    @Override
                    public String getIdValue(String object, int index) {
                        return object;
                    }
                }));

        //Object Id
        filterForm.add(new TextField<String>("objectId"));

        //Event
        filterForm.add(new DropDownChoice<Log.EVENT>("event", Arrays.asList(Log.EVENT.values()),
                new IChoiceRenderer<Log.EVENT>() {

                    @Override
                    public Object getDisplayValue(Log.EVENT object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(Log.EVENT object, int index) {
                        return String.valueOf(object.ordinal());
                    }
                }));

        //Status
        filterForm.add(new DropDownChoice<Log.STATUS>("status", Arrays.asList(Log.STATUS.values()),
                new IChoiceRenderer<Log.STATUS>() {

                    @Override
                    public Object getDisplayValue(Log.STATUS object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(Log.STATUS object, int index) {
                        return String.valueOf(object.ordinal());
                    }
                }));

        //Description
        filterForm.add(new TextField<String>("description"));

        //Модель данных списка элементов журнала событий
        final SortableDataProvider<Log> dataProvider = new SortableDataProvider<Log>() {

            @Override
            public Iterator<? extends Log> iterator(int first, int count) {
                LogFilter filter = filterModel.getObject();

                filter.setFirst(first);
                filter.setCount(count);
                filter.setSortProperty(getSort().getProperty());
                filter.setAscending(getSort().isAscending());

                return logListBean.getLogs(filterModel.getObject()).iterator();
            }

            @Override
            public int size() {
                return logListBean.getLogsCount(filterModel.getObject());
            }

            @Override
            public IModel<Log> model(Log object) {
                return new Model<Log>(object);
            }
        };
        dataProvider.setSort("date", false);

        //Таблица журнала событий
        DataView<Log> dataView = new DataView<Log>("logs", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Log> item) {
                final Log log = item.getModelObject();

                item.add(DateLabel.forDatePattern("date", new Model<Date>(log.getDate()), "dd.MM.yy HH:mm:ss"));
                item.add(new Label("login", log.getLogin()));
                item.add(new Label("module", getStringOrKey(log.getModule())));
                item.add(new Label("controller", getStringOrKey(log.getController())));
                item.add(new Label("model", getStringOrKey(log.getModel())));
                item.add(LogManager.get().getLinkComponent("objectId", log));
                item.add(new Label("event", getStringOrKey(log.getEvent().name())));
                item.add(new Label("status", getStringOrKey(log.getStatus().name())));
                item.add(new Label("description", log.getDescription()));

                LogChangePanel logChangePanel = new LogChangePanel("log_changes", log.getLogChanges());
                logChangePanel.setVisible(!log.getLogChanges().isEmpty() && expandModel.contains(log.getId()));
                item.add(logChangePanel);

                Image expandImage = new Image("expand_image", new ResourceReference(
                        expandModel.contains(log.getId()) ? IMAGE_ARROW_TOP : IMAGE_ARROW_BOTTOM));

                AjaxSubmitLink expandLink = new AjaxSubmitLink("expand_link"){
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        if (expandModel.contains(log.getId())){
                            expandModel.remove(log.getId());
                        }else{
                            expandModel.add(log.getId());
                        }
                        target.addComponent(filterForm);
                    }
                };
                expandLink.setDefaultFormProcessing(false);
                expandLink.setVisible(!log.getLogChanges().isEmpty());
                expandLink.add(expandImage);
                item.add(expandLink);                                 
            }
        };
        filterForm.add(dataView);

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.date", "date", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.login", "login", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.module", "module", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.controller", "controller", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.model", "model", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.object_id", "object_id", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.event", "event", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.description", "description", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, getClass().getName(), filterForm));
    }


}
