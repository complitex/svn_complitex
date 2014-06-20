package org.complitex.dictionary;

import com.beust.jcommander.internal.Maps;
import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.apache.ibatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.Map;

/**
 * @author Pavel Sknar
 */
public final class EjbTestBeanLocator {

    private static final Logger log = LoggerFactory.getLogger(EjbTestBeanLocator.class);

    private static final String CONFIGURATION_FILE = "config.xml";

    private static Context ctx;

    public static synchronized Context getContext() {

        if (ctx == null) {
            try {
                Map<String, Object> properties = Maps.newHashMap();
                try (Reader reader = Resources.getResourceAsReader(CONFIGURATION_FILE)) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(reader));

                    NodeList propertyNodes = document.getElementsByTagName("property");
                    for (int i = 0; i < propertyNodes.getLength(); i++) {
                        Node propertyNode = propertyNodes.item(i);
                        NamedNodeMap propertyAttributes = propertyNode.getAttributes();
                        String propertyName = propertyAttributes.getNamedItem("name").getTextContent();
                        Node valueAttribute = propertyAttributes.getNamedItem("value");
                        Object propertyValue;
                        if (valueAttribute != null) {
                            propertyValue = valueAttribute.getNodeValue();
                        } else if (propertyNode.hasChildNodes() && ((DeferredElementImpl) propertyNode).getElementsByTagName("collection").getLength() == 1) {
                            Node collectionNode = ((DeferredElementImpl) propertyNode).getElementsByTagName("collection").item(0);
                            Node itemsTypeAttribute = collectionNode.getAttributes().getNamedItem("itemsType");
                            NodeList itemNodes = ((DeferredElementImpl) collectionNode).getElementsByTagName("item");
                            if (itemsTypeAttribute != null) {
                                Class<?> itemClass = Class.forName(itemsTypeAttribute.getNodeValue());
                                propertyValue = Array.newInstance(itemClass, itemNodes.getLength());
                                for (int j = 0; j < itemNodes.getLength(); j++) {
                                    ((Object[]) propertyValue)[j] = itemClass.getConstructor(String.class).newInstance(itemNodes.item(j).getTextContent());
                                }
                            } else {
                                propertyValue = new String[itemNodes.getLength()];
                                for (int j = 0; j < itemNodes.getLength(); j++) {
                                    ((String[]) propertyValue)[j] = itemNodes.item(j).getNodeValue();
                                }
                            }
                        } else {
                            throw new ParserConfigurationException("Failed property node: not found value");
                        }
                        properties.put(propertyName, propertyValue);
                    }
                }

                EJBContainer container = EJBContainer.createEJBContainer(properties);
                ctx = container.getContext();
            } catch (Exception ex) {
                log.error("Failed init ejb context", ex);
            }
        }

        return ctx;
    }

    public static <T> T getBean(String beanName) {
        T result = getBean(beanName, "test-classes");
        if (result == null) {
            return getBean(beanName, "classes");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getBean(String beanName, String subPath) {
        try {
            return (T) getContext().lookup("java:global/ejb-app/" + subPath + "/" + beanName);
        } catch (NamingException e) {
            //
        }
        return null;
    }
}
