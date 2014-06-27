package org.complitex.dictionary;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableList;
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

    public static synchronized Context getContextInstance() {

        if (ctx == null) {
            ctx = createEJBContainer().getContext();
        }

        return ctx;
    }

    public static EJBContainer createEJBContainer() {
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

            return EJBContainer.createEJBContainer(properties);
        } catch (Exception ex) {
            log.error("Failed init ejb context", ex);
        }
        return null;
    }

    public static <T> T getBean(Context context, String beanName) {

        for (Boolean usingEjbApp : ImmutableList.of(Boolean.FALSE, Boolean.TRUE)) {
            for (String classesPath : ImmutableList.of("test-classes", "test-classesejb", "classes", "classesejb")) {
                T result = getBean(context, beanName, classesPath, usingEjbApp);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static <T> T getBean(String beanName) {

        return getBean(getContextInstance(), beanName);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getBean(Context context, String beanName, String classPath, boolean usingEjbApp) {
        StringBuilder builder = new StringBuilder("java:global/");
        if (usingEjbApp) {
            builder.append("ejb-app/");
        }
        builder.append(classPath).
                append("/").
                append(beanName);

        try {
            return (T) context.lookup(builder.toString());
        } catch (NamingException e) {
            //
        }
        return null;
    }
}
