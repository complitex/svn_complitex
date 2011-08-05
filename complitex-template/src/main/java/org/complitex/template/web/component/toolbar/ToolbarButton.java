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

    public ToolbarButton(String id, ResourceReference imageSrc, IModel<String> titleModel, boolean useAjax) {
        this(id, imageSrc, titleModel, useAjax, null);
    }

    public ToolbarButton(String id, ResourceReference imageSrc, IModel<String> titleModel) {
        this(id, imageSrc, titleModel, false);
    }

    /**
     * This constructor is not initializing component tree (init() method). Sometimes component tree initializing requires some
     * data that is not available at constructor call time. In such cases it would be better to do initialization
     * just before rendering when required data is already available rather at constructor time.
     * That should be done in extending class' onBeforeRenderer() method.
     * @param id
     * @param useAjax
     * @param tagId
     */
    protected ToolbarButton(String id, boolean useAjax, String tagId) {
        super(id);
        this.useAjax = useAjax;
        this.tagId = tagId;
    }

    protected ToolbarButton(String id, ResourceReference imageSrc, IModel<String> titleModel, boolean useAjax, String tagId) {
        this(id, useAjax, tagId);
        init(imageSrc, titleModel);
    }

    /**
     * Initializing method.
     * @param imageSrc
     * @param titleModel
     */
    protected void init(ResourceReference imageSrc, IModel<String> titleModel) {
        if (titleModel instanceof IComponentAssignedModel) {
            titleModel = ((IComponentAssignedModel) titleModel).wrapOnAssignment(this);
        }

        AbstractLink link = newLink(LINK_MARKUP_ID);
        Image image = newImage("image", imageSrc, titleModel);
        link.add(image);
        add(link);
    }

    protected void onClick() {
    }

    protected void onClick(AjaxRequestTarget target) {
    }

    protected AbstractLink newLink(String linkId) {
        if (useAjax) {
            return new AjaxLink(linkId) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ToolbarButton.this.onClick(target);
                }
            };
        } else {
            return new Link<Void>(linkId) {

                @Override
                public void onClick() {
                    ToolbarButton.this.onClick();
                }
            };
        }
    }

    protected AbstractLink getLink() {
        return (AbstractLink) get("link");
    }

    protected Image newImage(String imageId, ResourceReference imageSrc, final IModel<String> titleModel) {
        return new Image(imageId, imageSrc) {

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
