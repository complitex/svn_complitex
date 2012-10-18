package org.complitex.template.web.template;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.complitex.dictionary.entity.Preference;
import org.complitex.dictionary.service.PreferenceBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.web.ISessionStorage;
import org.complitex.resources.theme.ThemeResourceReference;
import org.complitex.template.web.pages.expired.SessionExpiredPage;
import org.complitex.template.web.security.ServletAuthWebApplication;
import org.odlabs.wiquery.ui.themes.IThemableApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.IApplicationListener;
import org.apache.wicket.Page;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.inject.JavaEE6ModuleNamingStrategy;
import org.complitex.dictionary.web.component.image.markup.WicketStaticImageResolver;
import org.complitex.template.web.component.IMainUserOrganizationPicker;
import org.complitex.template.web.component.MainUserOrganizationPicker;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.access.AccessDeniedPage;
import org.complitex.template.web.pages.welcome.WelcomePage;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 18:36:29
 */
public abstract class TemplateWebApplication extends ServletAuthWebApplication implements IThemableApplication {

    private static final Logger log = LoggerFactory.getLogger(TemplateWebApplication.class);
    private static final String TEMPLATE_CONFIG_FILE_NAME = "template-config.xml";
    private static final ThemeResourceReference theme = new ThemeResourceReference();
    private volatile Collection<Class<ITemplateMenu>> menuClasses;
    private volatile static Class<? extends Page> homePageClass;
    private static volatile Class<? extends Component> mainUserOrganizationPickerComponentClass;

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
                application.setHeaderResponseDecorator(new IHeaderResponseDecorator() {

                    public IHeaderResponse decorate(IHeaderResponse response) {
                        return new TemplateApplicationDecoratingHeaderResponse(response);
                    }
                });

                application.getPageSettings().addComponentResolver(new WicketStaticImageResolver());
            }

            @Override
            public void onBeforeDestroyed(Application application) {
            }
        });
    }

    @Override
    public ResourceReference getTheme(Session session) {
        return theme;
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
            final IClassResolver wicketClassResolver = getApplicationSettings().getClassResolver();

            //template menus
            Collection<String> menuClassNames = templateLoader.getMenuClassNames();
            final Collection<Class<ITemplateMenu>> templateMenuClasses = new ArrayList<Class<ITemplateMenu>>();
            for (String menuClassName : menuClassNames) {
                try {
                    Class<ITemplateMenu> menuClass = (Class<ITemplateMenu>) wicketClassResolver.resolveClass(menuClassName);
                    templateMenuClasses.add(menuClass);
                } catch (ClassNotFoundException e) {
                    log.warn("Меню не найдено: {}", menuClassName);
                }
            }
            this.menuClasses = Collections.unmodifiableCollection(templateMenuClasses);

            //home page
            final String homePageClassName = templateLoader.getHomePageClassName();
            if (!Strings.isEmpty(homePageClassName)) {
                try {
                    homePageClass = (Class) wicketClassResolver.resolveClass(homePageClassName);
                } catch (ClassNotFoundException e) {
                    log.warn("Домашняя страница не найдена: {}, будет использована страница {}", homePageClassName,
                            WelcomePage.class);
                }
            }
            if (homePageClass == null) {
                homePageClass = WelcomePage.class;
            }

            //main user organization picker component class
            final String mainUserOrganizationPickerComponentClassName = 
                    templateLoader.getMainUserOrganizationPickerClassName();
            
            if (!Strings.isEmpty(mainUserOrganizationPickerComponentClassName)) {
                try {
                    mainUserOrganizationPickerComponentClass = 
                            (Class) wicketClassResolver.resolveClass(mainUserOrganizationPickerComponentClassName);
                    if (!IMainUserOrganizationPicker.class.isAssignableFrom(mainUserOrganizationPickerComponentClass)) {
                        log.warn("Компонент для выбора основной пользовательской организации не наследует нитерфейс {}."
                                + " Будет использован компонент по умолчанию {}",
                                IMainUserOrganizationPicker.class, MainUserOrganizationPicker.class);
                        mainUserOrganizationPickerComponentClass = null;
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Компонент для выбора основной пользовательской организации не найден: {}, "
                            + "будет использован компонент по умолчанию {}", mainUserOrganizationPickerComponentClassName,
                            MainUserOrganizationPicker.class);
                }
            }
            if (mainUserOrganizationPickerComponentClass == null) {
                mainUserOrganizationPickerComponentClass = MainUserOrganizationPicker.class;
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

    public static Class<? extends Page> getHomePageClass() {
        return homePageClass;
    }

    public static Class<? extends Component> getMainUserOrganizationPickerComponentClass() {
        return mainUserOrganizationPickerComponentClass;
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
