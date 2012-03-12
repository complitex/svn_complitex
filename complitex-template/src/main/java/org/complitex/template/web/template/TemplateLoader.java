package org.complitex.template.web.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 18:38:32
 *
 * Загружает список меню из файла конфигурации.
 */
public class TemplateLoader {

    private static final String SIDEBAR_ELEMENT_NAME = "sidebar";
    private static final String MENU_ELEMENT_NAME = "menu";
    private static final String CLASS_ATTRIBUTE_NAME = "class";
    private static final String HOME_PAGE_CLASS_ELEMENT_NAME = "homepage-class";
    private final Collection<String> menuClassNames;
    private final String homePageClassName;

    public TemplateLoader(InputStream inputStream) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            this.menuClassNames = Collections.unmodifiableCollection(getMenuClassNames(document));
            this.homePageClassName = getHomePageClassName(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getMenuClassNames(Document document) {
        NodeList sidebar = document.getElementsByTagName(SIDEBAR_ELEMENT_NAME);

        if (sidebar.getLength() == 0) {
            return null;
        }

        List<String> menuClassNamesList = new ArrayList<String>();

        NodeList fragments = sidebar.item(0).getChildNodes();
        for (int i = 0; i < fragments.getLength(); i++) {
            if (fragments.item(i) instanceof Element) {
                Element menu = (Element) fragments.item(i);
                if (MENU_ELEMENT_NAME.equals(menu.getTagName())) {
                    String className = menu.getAttribute(CLASS_ATTRIBUTE_NAME);
                    if (className.length() > 0) {
                        menuClassNamesList.add(className);
                    }
                }
            }
        }

        return menuClassNamesList;
    }

    private String getHomePageClassName(Document document) {
        NodeList homePageClassElements = document.getElementsByTagName(HOME_PAGE_CLASS_ELEMENT_NAME);

        if (homePageClassElements.getLength() > 1) {
            throw new IllegalStateException("There are more one " + HOME_PAGE_CLASS_ELEMENT_NAME + " elements.");
        }

        if (homePageClassElements.getLength() == 1) {
            Element homePageClassElement = (Element) homePageClassElements.item(0);
            String result = homePageClassElement.getTextContent();
            return result != null ? result.trim() : null;
        }
        return null;
    }

    public Collection<String> getMenuClassNames() {
        return menuClassNames;
    }

    public String getHomePageClassName() {
        return homePageClassName;
    }
}
