package org.complitex.template.web.template;

import org.apache.wicket.Request;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
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
import java.util.Iterator;
import java.util.List;
import org.complitex.dictionary.mybatis.inject.JavaEE6ModuleNamingStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 18:36:29
 */
public abstract class TemplateWebApplication extends ServletAuthWebApplication implements IThemableApplication {

    private static final Logger log = LoggerFactory.getLogger(TemplateWebApplication.class);
    private static final String TEMPLATE_CONFIG_FILE_NAME = "template-config.xml";
    private List<Class<ITemplateMenu>> menuClasses;
    private final static ThemeResourceReference theme = new ThemeResourceReference();

    @Override
    protected void init() {
        super.init();

        initializeJEEInjector();
        initializeTemplateConfig();

        getApplicationSettings().setPageExpiredErrorPage(SessionExpiredPage.class);
    }

    @Override
    public ResourceReference getTheme(Session session) {
        return theme;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
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
            List<String> menuClassNames = templateLoader.getMenuClassNames();

            menuClasses = new ArrayList<Class<ITemplateMenu>>();
            for (String menuClassName : menuClassNames) {
                try {
                    Class menuClass = getApplicationSettings().getClassResolver().resolveClass(menuClassName);
                    menuClasses.add(menuClass);
                } catch (Exception e) {
                    log.warn("Меню не найдено : {}", menuClassName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeJEEInjector() {
        addComponentInstantiationListener(new JavaEEComponentInjector(this, new JavaEE6ModuleNamingStrategy()));
    }

    public List<Class<ITemplateMenu>> getMenuClasses() {
        return menuClasses;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new TemplateSession(request, newSessionStorage());
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
