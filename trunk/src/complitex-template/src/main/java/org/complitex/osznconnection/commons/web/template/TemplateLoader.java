package org.complitex.osznconnection.commons.web.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 18:38:32
 *
 * Загружает список меню из файла конфигурации.
 */
public class TemplateLoader {
    private List<String> menuClassNames;

        public TemplateLoader(InputStream inputStream) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);

                menuClassNames = getFragmentsClassName(document);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private List<String> getFragmentsClassName(Document document) {
            NodeList sidebar = document.getElementsByTagName("sidebar");
            if (sidebar.getLength() == 0) {
                return null;
            }

            List<String> menuClassNames = new ArrayList<String>();

            NodeList fragments = sidebar.item(0).getChildNodes();

            for (int i = 0; i < fragments.getLength(); i++) {
                if (fragments.item(i) instanceof Element) {
                    Element menu = (Element) fragments.item(i);
                    if ("menu".equals(menu.getTagName())) {
                        String className = menu.getAttribute("class");
                        if (className.length() > 0) {
                            menuClassNames.add(className);
                        }
                    }
                }
            }

            return menuClassNames;
        }

        public List<String> getMenuClassNames() {
            return menuClassNames;
        }
    }
