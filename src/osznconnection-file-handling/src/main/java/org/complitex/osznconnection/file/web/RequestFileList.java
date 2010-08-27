package org.complitex.osznconnection.file.web;

import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.dictionaryfw.util.StringUtil;
import org.complitex.dictionaryfw.web.component.DatePicker;
import org.complitex.dictionaryfw.web.component.MonthDropDownChoice;
import org.complitex.dictionaryfw.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionaryfw.web.component.paging.PagingNavigator;
import org.complitex.osznconnection.commons.web.template.TemplatePage;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileFilter;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 13:35:35
 */
public class RequestFileList extends TemplatePage{
    @EJB(name = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(name = "StrategyFactory")
    private StrategyFactory strategyFactory;

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
        filterForm.add(new DropDownChoice<DomainObject>("organization", new ListModel<DomainObject>()));

        //Месяц
        filterForm.add(new MonthDropDownChoice("month"));

        //Год
        filterForm.add(new TextField<Integer>("year", new Model<Integer>(), Integer.class));

        //Количество записей
        filterForm.add(new TextField<Integer>("dbfRecordCount", new Model<Integer>(), Integer.class));

        //Количество загруженных записей
        filterForm.add(new TextField<Integer>("loadedRecordCount", new Model<Integer>(), Integer.class));

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

        //Таблица файлов запросов
        DataView<RequestFile> dataView = new DataView<RequestFile>("request_files", dataProvider, 1){

            @Override
            protected void populateItem(Item<RequestFile> item) {
                RequestFile rf = item.getModelObject();

                item.add(DateLabel.forDatePattern("date", new Model<Date>(rf.getLoaded()), "dd.MM.yy HH:mm:ss"));
                item.add(new Label("name", rf.getName()));
                item.add(DateLabel.forDatePattern("month", new Model<Date>(rf.getDate()), "MMMM"));
                item.add(DateLabel.forDatePattern("year", new Model<Date>(rf.getDate()), "yyyy"));
                item.add(new Label("dbf_record_count", StringUtil.valueOf(rf.getDbfRecordCount())));
                item.add(new Label("loaded_record_count", StringUtil.valueOf(rf.getLoadedRecordCount())));
                item.add(new Label("status", getStringOrKey(rf.getStatus().name())));
            }
        };

        //Сортировка
        filterForm.add(new ArrowOrderByBorder("header.loaded", "loaded", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.name", "name", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.organization", "organization", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.month", "month", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.year", "year", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.dbf_record_count", "dbf_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.loaded_record_count", "loaded_record_count", dataProvider, dataView, filterForm));
        filterForm.add(new ArrowOrderByBorder("header.status", "status", dataProvider, dataView, filterForm));

        //Постраничная навигация
        filterForm.add(new PagingNavigator("paging", dataView, filterForm));
    }
}
