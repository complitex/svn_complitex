package org.complitex.template.web.template;

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
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.PreferenceKey;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.BookmarkablePageLinkPanel;
import org.complitex.resources.WebCommonResourceInitializer;
import org.complitex.template.web.component.toolbar.HelpButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.odlabs.wiquery.core.commons.CoreJavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 16:09:45
 *
 * Суперкласс шаблон для отображения содержания страниц.
 * Для инициализации шаблона наследники должны вызывать метод super().
 */
public abstract class TemplatePage extends WebPage {

    private static final Logger log = LoggerFactory.getLogger(TemplatePage.class);
    
    @EJB
    private SessionBean sessionBean;
    
    private String page = getClass().getName();
    private Set<String> resourceBundle = new HashSet<String>();
    private boolean isPostBack;
    private final WebMarkupContainer toolbar;

    protected TemplatePage() {
        add(JavascriptPackageResource.getHeaderContribution(CoreJavaScriptResourceReference.get()));
        add(JavascriptPackageResource.getHeaderContribution(WebCommonResourceInitializer.COMMON_JS));
        add(JavascriptPackageResource.getHeaderContribution(TemplatePage.class, TemplatePage.class.getSimpleName() + ".js"));
        add(CSSPackageResource.getHeaderContribution(WebCommonResourceInitializer.STYLE_CSS));

        add(new Link<Void>("home") {

            @Override
            public void onClick() {
                setResponsePage(getApplication().getHomePage());
            }
        });

        //toolbar
        toolbar = new WebMarkupContainer("toolbar");
        add(toolbar);
        WebMarkupContainer commonPart = new WebMarkupContainer("commonPart");
        toolbar.add(commonPart);

        //add common buttons.
        HelpButton help = new HelpButton("help");
        commonPart.add(help);

        //menu
        add(new ListView<ITemplateMenu>("sidebar", newTemplateMenus()) {

            @Override
            protected void populateItem(ListItem<ITemplateMenu> item) {
                item.add(new TemplateMenu("menu_placeholder", "menu", this, item.getModelObject()));
            }
        });

        if (isUserAuthorized()) {
            String fullName = sessionBean.getCurrentUserFullName(getLocale());
            String depName = sessionBean.getMainUserOrganizationName(getLocale());

            add(new Label("current_user_fullname", fullName != null ? fullName : ""));
            add(new Label("current_user_department", depName != null ? depName : ""));

            try {
                //noinspection unchecked
                add(new BookmarkablePageLinkPanel("profile", getString("profile"),
                        getClass().getClassLoader().loadClass("org.complitex.admin.web.ProfilePage"), null));
            } catch (ClassNotFoundException e) {
                add(new EmptyPanel("profile"));
            }
        } else {
            add(new EmptyPanel("current_user_fullname"));
            add(new EmptyPanel("current_user_department"));
            add(new EmptyPanel("profile"));
        }

        add(new Form<Void>("exit") {

            @Override
            public void onSubmit() {
                getTemplateWebApplication().logout();
            }
        }.setVisible(isUserAuthorized()));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        if (!isPostBack) {
            isPostBack = true;

            //add page custom buttons.
            List<? extends ToolbarButton> buttons = getToolbarButtons("pageToolbarButton");
            if (buttons == null) {
                buttons = Collections.emptyList();
            }

            Component pagePart = new ListView<ToolbarButton>("pagePart", buttons) {

                @Override
                protected void populateItem(ListItem<ToolbarButton> item) {
                    item.add(item.getModelObject());
                }
            };
            toolbar.add(pagePart);
        }
    }

    /**
     * Боковая панель с меню, которое устанавливается в конфигурационном файле.
     */
    private class TemplateMenu extends Fragment {

        private String tagId;

        private TemplateMenu(String id, String markupId, MarkupContainer markupProvider, ITemplateMenu menu) {
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
        if (key == null) {
            return "";
        }

        try {
            return getString(key);
        } catch (MissingResourceException e) {
            //resource is not found
        }

        for (String bundle : resourceBundle) {
            try {
                return ResourceUtil.getString(bundle, key, getLocale());
            } catch (MissingResourceException e) {
                //resource is not found
            }
        }

        return key;
    }

    protected String getStringOrKey(Enum<?> key) {
        return key != null ? getStringOrKey(key.name()) : "";
    }

    protected String getStringFormat(String key, Object... args) {
        try {
            return MessageFormat.format(getString(key), args);
        } catch (Exception e) {
            log.error("Ошибка форматирования файла свойств", e);
            return key;
        }
    }

    protected void addResourceBundle(String bundle) {
        resourceBundle.add(bundle);
    }

    protected void addAllResourceBundle(Collection<String> bundle) {
        resourceBundle.addAll(bundle);
    }

    /* Template Session Preferences*/
    public String getPreferencesPage() {
        return page;
    }

    public void setPreferencesPage(String page) {
        this.page = page;
    }

    public void setSortProperty(String sortProperty) {
        getTemplateSession().putPreference(page, PreferenceKey.SORT_PROPERTY, sortProperty, true);
    }

    public String getSortProperty(String _default) {
        return getTemplateSession().getPreferenceString(page, PreferenceKey.SORT_PROPERTY, _default);
    }

    public void setSortOrder(Boolean sortOrder) {
        getTemplateSession().putPreference(page, PreferenceKey.SORT_ORDER, sortOrder, true);
    }

    public Boolean getSortOrder(Boolean _default) {
        return getTemplateSession().getPreferenceBoolean(page, PreferenceKey.SORT_ORDER, _default);
    }

    public void setFilterObject(Object filterObject) {
        getTemplateSession().putPreferenceObject(page, PreferenceKey.FILTER_OBJECT, filterObject);
    }

    public Object getFilterObject(Object _default) {
        return getTemplateSession().getPreferenceObject(page, PreferenceKey.FILTER_OBJECT, _default);
    }

    public final boolean isUserAuthorized() {
        return getTemplateWebApplication().hasAnyRole(SecurityRole.AUTHORIZED);
    }
}