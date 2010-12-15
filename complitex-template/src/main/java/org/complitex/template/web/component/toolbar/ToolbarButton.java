package org.complitex.template.web.component.toolbar;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.model.ResourceModel;

/**
 *
 * @author Artem
 */
public abstract class ToolbarButton extends Panel {
    private String tagId;

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

    protected Link addLink() {
        return new Link("link") {

            @Override
            public void onClick() {
                ToolbarButton.this.onClick();
            }
        };
    }

    protected Image addImage(ResourceReference imageSrc, final IWrapModel<String> title) {
        return new Image("image", imageSrc) {

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("title", title.getObject());

                if (tagId != null){
                    tag.put("id", tagId);
                }
            }
        };
    }
}
