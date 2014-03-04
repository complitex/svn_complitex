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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

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
    private final Collection<String> menuClassNames;
    private final String homePageClassName;
    private final String mainUserOrganizationPickerComponentClassName;
    private final String domainObjectPermissionPanelClassName;
    private final String organizationPermissionPanelClassName;
    private final String userOrganizationPickerClassName;

    public TemplateLoader(InputStream inputStream) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            this.menuClassNames = Collections.unmodifiableCollection(getMenuClassNames(document));

            XPath xpath = XPathFactory.newInstance().newXPath();
            this.homePageClassName = getHomePageClassName(xpath, document);
            this.mainUserOrganizationPickerComponentClassName =
                    getMainUserOrganizationPickerComponentClassName(xpath, document);
            this.domainObjectPermissionPanelClassName = getDomainObjectPermissionPanelClassName(xpath, document);
            this.organizationPermissionPanelClassName = getOrganizationPermissionPanelClassName(xpath, document);
            this.userOrganizationPickerClassName = getUserOrganizationPickerClassName(xpath, document);
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

    private String getHomePageClassName(XPath xpath, Document doc) {
        return getPathValue("//homepage-class/text()", xpath, doc);
    }

    private String getMainUserOrganizationPickerComponentClassName(XPath xpath, Document doc) {
        return getPathValue("//web-components/main-user-organization-picker-component/text()", xpath, doc);
    }

    private String getDomainObjectPermissionPanelClassName(XPath xpath, Document doc) {
        return getPathValue("//web-components/domain-object-permission-panel/text()", xpath, doc);
    }

    private String getOrganizationPermissionPanelClassName(XPath xpath, Document doc) {
        return getPathValue("//web-components/organization-permission-panel/text()", xpath, doc);
    }

    private String getUserOrganizationPickerClassName(XPath xpath, Document doc) {
        return getPathValue("//web-components/user-organization-picker/text()", xpath, doc);
    }

    private String getPathValue(String expression, XPath xpath, Document doc) {
        try {
            Node text = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
            if (text != null) {
                return text.getNodeValue().trim();
            }
            return null;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getMenuClassNames() {
        return menuClassNames;
    }

    public String getHomePageClassName() {
        return homePageClassName;
    }

    public String getMainUserOrganizationPickerClassName() {
        return mainUserOrganizationPickerComponentClassName;
    }

    public String getDomainObjectPermissionPanelClassName() {
        return domainObjectPermissionPanelClassName;
    }

    public String getOrganizationPermissionPanelClassName() {
        return organizationPermissionPanelClassName;
    }

    public String getUserOrganizationPickerClassName() {
        return userOrganizationPickerClassName;
    }
}
