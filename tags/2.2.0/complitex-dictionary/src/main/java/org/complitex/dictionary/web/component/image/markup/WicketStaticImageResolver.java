/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.image.markup;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author Artem
 */
public class WicketStaticImageResolver implements IComponentResolver {

    public static final String IMAGE_TAG = "image";
    public static final String IMAGE_SRC_ATTRIBUTE = "src";

    static {
        // register "wicket:image"
        WicketTagIdentifier.registerWellKnownTagName(IMAGE_TAG);
    }

    @Override
    public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag) {
        if (tag instanceof WicketTag) {
            WicketTag wtag = (WicketTag) tag;
            if (IMAGE_TAG.equalsIgnoreCase(wtag.getName())) {
                String imageSrc = wtag.getAttributes().getString(IMAGE_SRC_ATTRIBUTE);
                if (Strings.isEmpty(imageSrc)) {
                    throw new MarkupException(
                            "Wrong format of <wicket:image src='xxx'>: attribute 'src' is missing");
                }

                final String containerId = "_static_image_container_" + container.getPage().getAutoIndex();

                // collect all image attributes except 'src' and write through them to new static image wicket object.
                final Map<String, Object> writeThroughAttributes = new HashMap<>();
                for (Entry<String, Object> attribute : wtag.getAttributes().entrySet()) {
                    String name = attribute.getKey();
                    if (!IMAGE_SRC_ATTRIBUTE.equalsIgnoreCase(name)) {
                        writeThroughAttributes.put(name, attribute.getValue());
                    }
                }

                StaticImageContainer staticImageContainer = new StaticImageContainer(containerId, imageSrc,
                        writeThroughAttributes);
                return staticImageContainer;
            }
        }

        // We were not able to handle the tag
        return null;
    }

    private static class StaticImageContainer extends MarkupContainer {

        final Map<String, Object> attributes;
        final String imageSrc;

        StaticImageContainer(String id, String imageSrc, Map<String, Object> attributes) {
            super(id);
            this.attributes = attributes;
            this.imageSrc = imageSrc;
        }

        @Override
        protected void onComponentTag(final ComponentTag tag) {
            if (tag.isOpenClose()) {
                tag.setType(TagType.OPEN);
            }
            super.onComponentTag(tag);
        }

        @Override
        public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
            ResourceReference imageResourceReference = new SharedResourceReference(imageSrc);
            final CharSequence imageResourceUrl = getRequestCycle().urlFor(imageResourceReference, null);

            StringBuilder imgBuilder = new StringBuilder();
            imgBuilder.append("<img src = \"").append(imageResourceUrl).append("\"");
            if (!attributes.isEmpty()) {
                for (Entry<String, Object> attribute : attributes.entrySet()) {
                    imgBuilder.append(" ").append(attribute.getKey()).append(" = \"").append(attribute.getValue()).append("\"");
                }
            }
            imgBuilder.append("/>");

            getResponse().write(imgBuilder.toString());
        }
    }
}
