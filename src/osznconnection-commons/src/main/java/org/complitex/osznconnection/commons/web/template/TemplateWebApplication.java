package org.complitex.osznconnection.commons.web.template;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.complitex.dictionaryfw.entity.Preference;
import org.complitex.dictionaryfw.service.PreferenceBean;
import org.complitex.dictionaryfw.service.SessionBean;
import org.complitex.dictionaryfw.util.EjbBeanLocator;
import org.complitex.dictionaryfw.web.ISessionStorage;
import org.complitex.osznconnection.commons.web.pages.expired.SessionExpiredPage;
import org.complitex.osznconnection.commons.web.security.ServletAuthWebApplication;
import org.odlabs.wiquery.core.commons.WiQueryInstantiationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 18:36:29
 */
public abstract class TemplateWebApplication extends ServletAuthWebApplication {

    private static final Logger log = LoggerFactory.getLogger(TemplateWebApplication.class);

    private static final String TEMPLATE_CONFIG_FILE_NAME = "template-config.xml";

    private List<Class<ITemplateMenu>> menuClasses;

    @Override
    protected void init() {
        super.init();

        initializeWiQuery();
        initializeJEEInjector();
        initializeTemplateConfig();

        getApplicationSettings().setPageExpiredErrorPage(SessionExpiredPage.class);
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
        addComponentInstantiationListener(new JavaEEComponentInjector(this));
    }

    private void initializeWiQuery() {
        addComponentInstantiationListener(new WiQueryInstantiationListener());
    }

    public List<Class<ITemplateMenu>> getMenuClasses() {
        return menuClasses;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new TemplateSession(request, newSessionStorage());
    }

    private ISessionStorage newSessionStorage(){
        final SessionBean sessionBean = EjbBeanLocator.getBean(SessionBean.class);
        final PreferenceBean preferenceBean = EjbBeanLocator.getBean(PreferenceBean.class);

        return new ISessionStorage(){

            @Override
            public List<Preference> load() {
                return preferenceBean.getPreferences(sessionBean.getCurrentUserId());
            }

            @Override
            public void save(Preference preference) {
                preferenceBean.save(preference);
            }

            @Override
            public Long getUserId() {
                return sessionBean.getCurrentUserId();
            }
        };
    }
}
