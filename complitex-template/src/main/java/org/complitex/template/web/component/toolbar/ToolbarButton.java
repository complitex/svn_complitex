package org.complitex.template.web.component.toolbar;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 *
 * @author Artem
 */
public abstract class ToolbarButton extends Panel {

    private String tagId;
    private static final String LINK_MARKUP_ID = "link";
    private boolean useAjax;

    public ToolbarButton(String id, ResourceReference imageSrc, String titleKey) {
        this(id, imageSrc, titleKey, false);
    }

    public ToolbarButton(String id, ResourceReference imageSrc, String titleKey, boolean useAjax) {
        this(id, imageSrc, new ResourceModel(titleKey), useAjax, null);
    }

    public ToolbarButton(String id, ResourceReference imageSrc, String titleKey, String tagId) {
        this(id, imageSrc, new ResourceModel(titleKey), false, tagId);
    }

    protected ToolbarButton(String id, ResourceReference imageSrc, IModel<String> titleModel, boolean useAjax, String tagId) {
        super(id);

        this.useAjax = useAjax;
        this.tagId = tagId;

        if (titleModel instanceof IComponentAssignedModel) {
            titleModel = ((IComponentAssignedModel) titleModel).wrapOnAssignment(this);
        }

        AbstractLink link = addLink();
        Image image = newImage(imageSrc, titleModel);
        link.add(image);
        add(link);
    }

    protected void onClick() {
    }

    protected void onClick(AjaxRequestTarget target) {
    }

    protected class ToolbarButtonLink extends Link<Void> {

        public ToolbarButtonLink() {
            super(LINK_MARKUP_ID);
        }

        @Override
        public void onClick() {
            ToolbarButton.this.onClick();
        }
    }

    protected AbstractLink addLink() {
        if (useAjax) {
            return new AjaxLink(LINK_MARKUP_ID) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ToolbarButton.this.onClick(target);
                }
            };
        } else {
            return new ToolbarButtonLink();
        }
    }

    protected Image newImage(ResourceReference imageSrc, final IModel<String> titleModel) {
        return new Image("image", imageSrc) {

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("title", titleModel.getObject());

                if (tagId != null) {
                    tag.put("id", tagId);
                }
            }
        };
    }
}
