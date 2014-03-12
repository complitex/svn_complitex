package org.complitex.template.web.template;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IApplicationListener;
import org.apache.wicket.Session;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.mybatis.inject.JavaEE6ModuleNamingStrategy;
import org.complitex.dictionary.service.PreferenceBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.ISessionStorage;
import org.complitex.dictionary.web.IWebComponentResolvableApplication;
import org.complitex.dictionary.web.IWebComponentResolver;
import org.complitex.dictionary.web.component.UserOrganizationPicker;
import org.complitex.dictionary.web.component.image.markup.WicketStaticImageResolver;
import org.complitex.dictionary.web.component.organization.user.UserOrganizationPickerFactory;
import org.complitex.dictionary.web.component.organization.user.UserOrganizationPickerParameters;
import org.complitex.dictionary.web.component.permission.AbstractDomainObjectPermissionPanel;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionPanelFactory;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionParameters;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionsPanel;
import org.complitex.dictionary.web.component.permission.organization.OrganizationPermissionPanelFactory;
import org.complitex.dictionary.web.component.permission.organization.OrganizationPermissionParameters;
import org.complitex.dictionary.web.component.permission.organization.OrganizationPermissionsPanel;
import org.complitex.resources.theme.ThemeResourceReference;
import org.complitex.template.web.component.IMainUserOrganizationPicker;
import org.complitex.template.web.component.MainUserOrganizationPicker;
import org.complitex.template.web.component.MainUserOrganizationPickerFactory;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.HomePageFactory;
import org.complitex.template.web.pages.access.AccessDeniedPage;
import org.complitex.template.web.pages.expired.SessionExpiredPage;
import org.complitex.template.web.pages.welcome.WelcomePage;
import org.complitex.template.web.security.ServletAuthWebApplication;
import org.complitex.template.web.template.TemplateWebComponentResolver.TemplateWebComponentResolverBuilder;
import org.odlabs.wiquery.ui.themes.WiQueryCoreThemeResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 18:36:29
 */
public abstract class TemplateWebApplication extends ServletAuthWebApplication
        implements IWebComponentResolvableApplication {

    private final Logger log = LoggerFactory.getLogger(TemplateWebApplication.class);
    private static final String TEMPLATE_CONFIG_FILE_NAME = "template-config.xml";
    private static final ThemeResourceReference theme = new ThemeResourceReference();
    private volatile Collection<Class<ITemplateMenu>> menuClasses;
    private IWebComponentResolver webComponentResolver;

    @Override
    protected void init() {
        super.init();

        initializeJEEInjector();
        initializeTemplateConfig();

        getApplicationSettings().setPageExpiredErrorPage(SessionExpiredPage.class);
        getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);

        getApplicationListeners().add(new IApplicationListener() {

            @Override
            public void onAfterInitialized(Application application) {

                application.getPageSettings().addComponentResolver(new WicketStaticImageResolver());
            }

            @Override
            public void onBeforeDestroyed(Application application) {
            }
        });

        addResourceReplacement(WiQueryCoreThemeResourceReference.get(), theme);
    }

    private void initializeTemplateConfig() throws RuntimeException {
        try {
            Iterator<URL> resources = getApplicationSettings().getClassResolver().getResources(TEMPLATE_CONFIG_FILE_NAME);
            InputStream inputStream;
            if (resources.hasNext()) {
                inputStream = resources.next().openStream();
                if (resources.hasNext()) {
                    log.warn("There are more than one template config {} files. What file will be picked is unpredictable.",
                            TEMPLATE_CONFIG_FILE_NAME);
                }
            } else {
                throw new RuntimeException("Template config file " + TEMPLATE_CONFIG_FILE_NAME + " was not found.");
            }

            TemplateLoader templateLoader = new TemplateLoader(inputStream);
            final IClassResolver classResolver = getApplicationSettings().getClassResolver();

            //template menus
            Collection<String> menuClassNames = templateLoader.getMenuClassNames();
            final Collection<Class<ITemplateMenu>> templateMenuClasses = new ArrayList<Class<ITemplateMenu>>();
            for (String menuClassName : menuClassNames) {
                try {
                    Class<ITemplateMenu> menuClass = (Class<ITemplateMenu>) classResolver.resolveClass(menuClassName);
                    templateMenuClasses.add(menuClass);
                } catch (ClassNotFoundException e) {
                    log.warn("Меню не найдено: {}", menuClassName);
                }
            }
            this.menuClasses = Collections.unmodifiableCollection(templateMenuClasses);

            //custom web components
            {
                TemplateWebComponentResolverBuilder webComponentResolverBuilder =
                        new TemplateWebComponentResolverBuilder();

                //Home page
                {
                    final String homePageClassName = templateLoader.getHomePageClassName();

                    Class<? extends WebPage> homePageClass = null;
                    if (!Strings.isEmpty(homePageClassName)) {
                        try {
                            homePageClass = (Class) classResolver.resolveClass(homePageClassName);
                        } catch (ClassNotFoundException e) {
                            log.warn("Домашняя страница не найдена: {}. Будет использована страница {}", homePageClassName,
                                    WelcomePage.class);
                        }
                    }
                    if (homePageClass == null) {
                        homePageClass = WelcomePage.class;
                    }
                    webComponentResolverBuilder.addComponentMapping(HomePageFactory.WEB_COMPONENT_NAME, homePageClass);
                }

                //Main user organization picker component class
                {
                    final String mainUserOrganizationPickerComponentClassName =
                            templateLoader.getMainUserOrganizationPickerClassName();

                    Class<? extends Component> mainUserOrganizationPickerComponentClass = null;
                    if (!Strings.isEmpty(mainUserOrganizationPickerComponentClassName)) {
                        try {
                            mainUserOrganizationPickerComponentClass =
                                    (Class) classResolver.resolveClass(mainUserOrganizationPickerComponentClassName);
                            if (!IMainUserOrganizationPicker.class.isAssignableFrom(mainUserOrganizationPickerComponentClass)) {
                                log.warn("Компонент для выбора основной пользовательской организации не наследует нитерфейс {}."
                                        + " Будет использован компонент по умолчанию {}",
                                        IMainUserOrganizationPicker.class, MainUserOrganizationPicker.class);
                                mainUserOrganizationPickerComponentClass = null;
                            }
                        } catch (ClassNotFoundException e) {
                            log.warn("Компонент для выбора основной пользовательской организации не найден: {}. "
                                    + "Будет использован компонент по умолчанию {}", mainUserOrganizationPickerComponentClassName,
                                    MainUserOrganizationPicker.class);
                        }
                    }
                    if (mainUserOrganizationPickerComponentClass == null) {
                        mainUserOrganizationPickerComponentClass = MainUserOrganizationPicker.class;
                    }

                    webComponentResolverBuilder.addComponentMapping(MainUserOrganizationPickerFactory.WEB_COMPONENT_NAME,
                            mainUserOrganizationPickerComponentClass);
                }

                //Domain object permission panel class
                {
                    final String domainObjectPermissionPanelClassName =
                            templateLoader.getDomainObjectPermissionPanelClassName();

                    Class<? extends AbstractDomainObjectPermissionPanel> domainObjectPermissionPanelClass = null;
                    if (!Strings.isEmpty(domainObjectPermissionPanelClassName)) {
                        try {
                            domainObjectPermissionPanelClass =
                                    (Class) classResolver.resolveClass(domainObjectPermissionPanelClassName);
                            boolean hasConstructor = true;
                            try {
                                hasConstructor =
                                        domainObjectPermissionPanelClass.getConstructor(
                                        String.class, DomainObjectPermissionParameters.class) != null;
                            } catch (NoSuchMethodException | SecurityException e) {
                                hasConstructor = false;
                            }

                            if (!hasConstructor) {
                                String constructor = "(" + String.class.getName() + ", "
                                        + DomainObjectPermissionParameters.class.getName() + ")";
                                log.warn("Компонент для редактирования прав доступа к объекту не имеет обязательного "
                                        + "конструктора {}. Будет использован компонент по умолчанию {}",
                                        constructor, DomainObjectPermissionsPanel.class);
                                domainObjectPermissionPanelClass = null;
                            }
                        } catch (ClassNotFoundException e) {
                            log.warn("Компонент для редактирования прав доступа к объекту не найден: {}. "
                                    + "Будет использован компонент по умолчанию {}", domainObjectPermissionPanelClassName,
                                    DomainObjectPermissionsPanel.class);
                        }
                    }
                    if (domainObjectPermissionPanelClass == null) {
                        domainObjectPermissionPanelClass = DomainObjectPermissionsPanel.class;
                    }

                    webComponentResolverBuilder.addComponentMapping(
                            DomainObjectPermissionPanelFactory.WEB_COMPONENT_NAME, domainObjectPermissionPanelClass);
                }

                //Organization permission panel class
                {
                    final String organizationPermissionPanelClassName =
                            templateLoader.getOrganizationPermissionPanelClassName();

                    Class<? extends AbstractDomainObjectPermissionPanel> organizationPermissionPanelClass = null;
                    if (!Strings.isEmpty(organizationPermissionPanelClassName)) {
                        try {
                            organizationPermissionPanelClass =
                                    (Class) classResolver.resolveClass(organizationPermissionPanelClassName);
                            boolean hasConstructor = true;
                            try {
                                hasConstructor =
                                        organizationPermissionPanelClass.getConstructor(
                                        String.class, OrganizationPermissionParameters.class) != null;
                            } catch (NoSuchMethodException | SecurityException e) {
                                hasConstructor = false;
                            }

                            if (!hasConstructor) {
                                String constructor = "(" + String.class.getName() + ", "
                                        + OrganizationPermissionParameters.class.getName() + ")";
                                log.warn("Компонент для редактирования прав доступа к организации не имеет обязательного "
                                        + "конструктора {}. Будет использован компонент по умолчанию {}",
                                        constructor, OrganizationPermissionParameters.class);
                                organizationPermissionPanelClass = null;
                            }
                        } catch (ClassNotFoundException e) {
                            log.warn("Компонент для редактирования прав доступа к организации не найден: {}. "
                                    + "Будет использован компонент по умолчанию {}", organizationPermissionPanelClassName,
                                    OrganizationPermissionsPanel.class);
                        }
                    }
                    if (organizationPermissionPanelClass == null) {
                        organizationPermissionPanelClass = OrganizationPermissionsPanel.class;
                    }

                    webComponentResolverBuilder.addComponentMapping(
                            OrganizationPermissionPanelFactory.WEB_COMPONENT_NAME, organizationPermissionPanelClass);
                }

                //User organization picker class
                {
                    final String userOrganizationPickerClassName =
                            templateLoader.getUserOrganizationPickerClassName();

                    Class<? extends Panel> userOrganizationPickerClass = null;
                    if (!Strings.isEmpty(userOrganizationPickerClassName)) {
                        try {
                            userOrganizationPickerClass =
                                    (Class) classResolver.resolveClass(userOrganizationPickerClassName);
                            boolean hasConstructor = true;
                            try {
                                hasConstructor =
                                        userOrganizationPickerClass.getConstructor(
                                        String.class, IModel.class, UserOrganizationPickerParameters.class) != null;
                            } catch (NoSuchMethodException | SecurityException e) {
                                hasConstructor = false;
                            }

                            if (!hasConstructor) {
                                String constructor = "(" + String.class.getName() + ", " + IModel.class + ", "
                                        + OrganizationPermissionParameters.class.getName() + ")";
                                log.warn("Компонент для выбора пользовательской организации не имеет обязательного "
                                        + "конструктора {}. Будет использован компонент по умолчанию {}",
                                        constructor, UserOrganizationPicker.class);
                                userOrganizationPickerClass = null;
                            }
                        } catch (ClassNotFoundException e) {
                            log.warn("Компонент для выбора пользовательской организации не найден: {}. "
                                    + "Будет использован компонент по умолчанию {}", userOrganizationPickerClassName,
                                    UserOrganizationPicker.class);
                        }
                    }
                    if (userOrganizationPickerClass == null) {
                        userOrganizationPickerClass = UserOrganizationPicker.class;
                    }

                    webComponentResolverBuilder.addComponentMapping(
                            UserOrganizationPickerFactory.WEB_COMPONENT_NAME, userOrganizationPickerClass);
                }

                //build template web component resolver
                webComponentResolver = webComponentResolverBuilder.build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeJEEInjector() {
        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, new JavaEE6ModuleNamingStrategy()));
    }

    public Collection<Class<ITemplateMenu>> getMenuClasses() {
        return menuClasses;
    }

    @Override
    public IWebComponentResolver getWebComponentResolver() {
        return webComponentResolver;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new TemplateSession(request, newSessionStorage());
    }

    public List<? extends ToolbarButton> getApplicationToolbarButtons(String id) {
        return null;
    }

    private static class DefaultSessionStorage implements ISessionStorage, Serializable {

        private PreferenceBean getPreferenceBean() {
            return EjbBeanLocator.getBean(PreferenceBean.class);
        }

        private SessionBean getSessionBean() {
            return EjbBeanLocator.getBean(SessionBean.class);
        }

        @Override
        public List<Preference> load() {
            return getPreferenceBean().getPreferences(getSessionBean().getCurrentUserId());
        }

        @Override
        public void save(Preference preference) {
            getPreferenceBean().save(preference);
        }

        @Override
        public Long getUserId() {
            return getSessionBean().getCurrentUserId();
        }
    }

    private ISessionStorage newSessionStorage() {
        return new DefaultSessionStorage();
    }
}
