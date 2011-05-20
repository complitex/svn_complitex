package org.complitex.template.web.component.toolbar;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 *
 * @author Artem
 */
public abstract class ToolbarButton extends Panel {

    private String tagId;
    private static final String LINK_MARKUP_ID = "link";

    public ToolbarButton(String id, ResourceReference imageSrc, String titleKey) {
        super(id);
        Link link = addLink();
        Image image = addImage(imageSrc, new ResourceModel(titleKey).wrapOnAssignment(this));
        link.add(image);
        add(link);
    }

    public ToolbarButton(String id, ResourceReference imageSrc, String titleKey, String tagId) {
        super(id);
        this.tagId = tagId;
        Link link = addLink();
        Image image = addImage(imageSrc, new ResourceModel(titleKey).wrapOnAssignment(this));
        link.add(image);

        add(link);
    }

    protected abstract void onClick();

    protected class ToolbarButtonLink extends Link<Void> {

        public ToolbarButtonLink() {
            super(LINK_MARKUP_ID);
        }

        @Override
        public void onClick() {
            ToolbarButton.this.onClick();
        }
    }

    protected Link addLink() {
        return new ToolbarButtonLink();
    }

    protected Image addImage(ResourceReference imageSrc, final IModel<String> title) {
        return new Image("image", imageSrc) {

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("title", title.getObject());

                if (tagId != null) {
                    tag.put("id", tagId);
                }
            }
        };
    }
}
