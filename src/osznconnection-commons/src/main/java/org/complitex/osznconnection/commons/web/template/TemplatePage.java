package org.complitex.osznconnection.commons.web.template;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.web.DictionaryFwSession;
import org.complitex.osznconnection.commons.web.component.LocalePicker;
import org.complitex.osznconnection.commons.web.component.toolbar.HelpButton;
import org.complitex.osznconnection.commons.web.component.toolbar.ToolbarButton;
import org.complitex.osznconnection.web.resource.WebCommonResourceInitializer;
import org.odlabs.wiquery.core.commons.CoreJavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.complitex.dictionaryfw.web.DictionaryFwSession.PREFERENCE.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:09:45
 *
 * Суперкласс шаблон для отображения содержания страниц.
 * Для инициализации шаблона наследники должны вызывать метод super().
 */
public abstract class TemplatePage extends WebPage {
    private static final Logger log = LoggerFactory.getLogger(TemplatePage.class);

    protected TemplatePage() {
        add(JavascriptPackageResource.getHeaderContribution(CoreJavaScriptResourceReference.get()));
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.COMMON_JS));
        add(JavascriptPackageResource.getHeaderContribution(TemplatePage.class, TemplatePage.class.getSimpleName() + ".js"));
        add(CSSPackageResource.getHeaderContribution(WebCommonResourceInitializer.STYLE_CSS));

        add(new Link("home") {

            @Override
            public void onClick() {
                setResponsePage(getApplication().getHomePage());
            }
        });

        //locale picker
        add(new LocalePicker("localePicker"));

        //toolbar
        WebMarkupContainer toolbar = new WebMarkupContainer("toolbar");
        add(toolbar);
        WebMarkupContainer commonPart = new WebMarkupContainer("commonPart");
        toolbar.add(commonPart);

        //add common buttons.
        HelpButton help = new HelpButton("help");
        commonPart.add(help);

        //add page custom buttons.
        List<? extends ToolbarButton> pageToolbarButtonsList = getToolbarButtons("pageToolbarButton");
        if (pageToolbarButtonsList == null) {
            pageToolbarButtonsList = Collections.emptyList();
        }
        Component pagePart = new ListView<ToolbarButton>("pagePart", pageToolbarButtonsList) {

            @Override
            protected void populateItem(ListItem<ToolbarButton> item) {
                item.add(item.getModelObject());
            }
        };
        toolbar.add(pagePart);


        //menu
        add(new ListView<ITemplateMenu>("sidebar", newTemplateMenus()) {

            @Override
            protected void populateItem(ListItem<ITemplateMenu> item) {
                item.add(new TemplateMenu("menu_placeholder", "menu", this, item.getModelObject()));
            }
        });

//        User user = userProfileBean.getCurrentUser();
//
//
//        add(new Label("current_user_fullname", user.getFullName()
//                + (user.getJob() != null ? ", " + user.getJob().getDisplayName(getLocale(), systemLocale) : "")));
//        add(new Label("current_user_department", user.getDepartment().getDisplayName(getLocale(), systemLocale)));

        add(new Form("exit") {

            @Override
            public void onSubmit() {
                getTemplateWebApplication().logout();
            }
        });
    }

    /**
     * Боковая панель с меню, которое устанавливается в конфигурационном файле.
     */
    private class TemplateMenu extends Fragment {

        private String tagId;

        public TemplateMenu(String id, String markupId, MarkupContainer markupProvider, ITemplateMenu menu) {
            super(id, markupId, markupProvider);
            this.tagId = menu.getTagId();

            add(new Label("menu_title", menu.getTitle(getLocale())));
            add(new ListView<ITemplateLink>("menu_items", menu.getTemplateLinks(getLocale())) {

                @Override
                protected void populateItem(ListItem<ITemplateLink> item) {
                    final ITemplateLink templateLink = item.getModelObject();
                    BookmarkablePageLink link = new BookmarkablePageLink<Class<? extends Page>>("link", templateLink.getPage(),
                            templateLink.getParameters()) {

                        @Override
                        protected void onComponentTag(ComponentTag tag) {
                            super.onComponentTag(tag);
                            if (!Strings.isEmpty(templateLink.getTagId())) {
                                tag.put("id", templateLink.getTagId());
                            }
                        }
                    };
                    link.add(new Label("label", templateLink.getLabel(getLocale())));
                    item.add(link);
                }
            });
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            if (!Strings.isEmpty(tagId)) {
                tag.put("id", tagId);
            }
        }
    }

    private List<ITemplateMenu> newTemplateMenus() {
        List<ITemplateMenu> templateMenus = new ArrayList<ITemplateMenu>();
        for (Class<ITemplateMenu> menuClass : getTemplateWebApplication().getMenuClasses()) {
            if (isTemplateMenuAuthorized(menuClass)) {
                try {
                    ITemplateMenu templateMenu = menuClass.newInstance();
                    templateMenus.add(templateMenu);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return templateMenus;
    }

    /**
     * Проверка роли пользователя для отображения меню модуля.
     * @param menuClass Класс меню.
     * @return Отображать ли меню пользователю в зависимости от его роли.
     */
    private boolean isTemplateMenuAuthorized(Class<?> menuClass) {
        boolean authorized = true;

        final AuthorizeInstantiation classAnnotation = menuClass.getAnnotation(AuthorizeInstantiation.class);
        if (classAnnotation != null) {
            authorized = getTemplateWebApplication().hasAnyRole(classAnnotation.value());
        }

        return authorized;
    }

    /**
     * Subclass can override method in order to specify custom page toolbar buttons.
     * @param id Component id
     * @return List of ToolbarButton to add to Template
     */
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return null;
    }

    protected boolean hasAnyRole(String... roles) {
        return getTemplateWebApplication().hasAnyRole(roles);
    }

    protected TemplateWebApplication getTemplateWebApplication() {
        return (TemplateWebApplication) getApplication();
    }

    protected TemplateSession getTemplateSession() {
        return (TemplateSession) getSession();
    }

    protected String getStringOrKey(String key) {
        return key != null ? getString(key, null, key) : "";
    }

    protected String getStringOrKey(Enum key) {
        return getStringOrKey(key.name());
    }

    public Locale getSystemLocale() {
        return getLocale();
    }

    protected String getStringFormat(String key, Object... args){
        try {
            return MessageFormat.format(getString(key), args);
        } catch (Exception e) {
            log.error("Ошибка форматирования файла свойств", e);
            return key;
        }
    }

    /* Template Session Preferences*/

    private <T> T getNotNull(T object, T _default){
        return object != null ? object : _default;
    }

    public void setSortProperty(String sortProperty){
        getTemplateSession().putPreference(getClass(), SORT_PROPERTY, sortProperty);
    }

    public String getSortProperty(String _default){
        return getNotNull((String) getTemplateSession().getPreference(getClass(), SORT_PROPERTY), _default);
    }

    public void setSortOrder(Boolean sortOrder){
        getTemplateSession().putPreference(getClass(), SORT_ORDER, sortOrder);
    }

    public Boolean getSortOrder(Boolean _default){
        return getNotNull((Boolean) getTemplateSession().getPreference(getClass(), SORT_ORDER), _default);
    }

    public void setPageIndex(Integer pageNumber){
        getTemplateSession().putPreference(getClass(), PAGE_INDEX, pageNumber);
    }

    public Integer getPageIndex(Integer _default){
        return getNotNull((Integer) getTemplateSession().getPreference(getClass(), PAGE_INDEX), _default);
    }

    public void setFilterObject(Object filterObject){
        getTemplateSession().putPreference(getClass(), FILTER_OBJECT, filterObject);
    }

    public Object getFilterObject(Object _default){
        return getNotNull(getTemplateSession().getPreference(getClass(), FILTER_OBJECT), _default);
    }

    public void setRowsPerPage(Integer pageSize){
        getTemplateSession().putPreference(getClass(), ROWS_PER_PAGE, pageSize);
    }

    public Integer getRowsPerPage(Integer _default){
        return getNotNull((Integer) getTemplateSession().getPreference(getClass(), ROWS_PER_PAGE), _default);
    }
}