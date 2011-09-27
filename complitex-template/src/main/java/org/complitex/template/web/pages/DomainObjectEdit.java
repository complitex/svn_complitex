package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.PageParameters;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.template.web.component.toolbar.DisableItemButton;
import org.complitex.template.web.component.toolbar.EnableItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.template.FormTemplatePage;

import java.util.List;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.security.SecurityRole;

import static org.complitex.template.strategy.TemplateStrategy.*;

/**
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class DomainObjectEdit extends FormTemplatePage {

    private DomainObjectEditPanel editPanel;
    private String entity;
    private String strategy;

    public DomainObjectEdit(PageParameters parameters) {
        init(parameters.getString(ENTITY), parameters.getString(STRATEGY), parameters.getAsLong(OBJECT_ID),
                parameters.getAsLong(PARENT_ID), parameters.getString(PARENT_ENTITY));
    }

    protected void init(String entity, String strategy, Long objectId, Long parentId, String parentEntity) {
        this.entity = entity;
        this.strategy = strategy;

        add(editPanel = newEditPanel("editPanel", entity, strategy, objectId, parentId, parentEntity, DomainObjectList.SCROLL_PARAMETER));
    }

    protected DomainObjectEditPanel newEditPanel(String id, String entity, String strategy, Long objectId, Long parentId,
            String parentEntity, String scrollListPageParameterName) {
        return new DomainObjectEditPanel(id, entity, strategy, objectId, parentId, parentEntity,
                scrollListPageParameterName);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new DisableItemButton(id) {

            @Override
            protected void onClick() {
                editPanel.disable();
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canDisable(strategy, entity, editPanel.getNewObject())) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        }, new EnableItemButton(id) {

            @Override
            protected void onClick() {
                editPanel.enable();
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canEnable(strategy, entity, editPanel.getNewObject())) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        }, new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                editPanel.delete();
            }

            @Override
            protected void onBeforeRender() {
                if (!DomainObjectAccessUtil.canDelete(strategy, entity, editPanel.getNewObject())) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        });
    }
}

