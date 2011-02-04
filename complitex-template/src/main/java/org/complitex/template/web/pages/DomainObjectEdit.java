/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.pages;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.PageParameters;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.template.web.component.toolbar.DisableItemButton;
import org.complitex.template.web.component.toolbar.EnableItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.template.web.security.SecurityRole;

/**
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class DomainObjectEdit extends FormTemplatePage {

    private static final Logger log = LoggerFactory.getLogger(DomainObjectEdit.class);
    public static final String ENTITY = "entity";
    public static final String OBJECT_ID = "object_id";
    public static final String PARENT_ID = "parent_id";
    public static final String PARENT_ENTITY = "parent_entity";
    private DomainObjectEditPanel editPanel;
    private String entity;

    public DomainObjectEdit(PageParameters parameters) {
        init(parameters.getString(ENTITY), parameters.getAsLong(OBJECT_ID), parameters.getAsLong(PARENT_ID), parameters.getString(PARENT_ENTITY));
    }

    private void init(String entity, Long object_id, Long parentId, String parentEntity) {
        this.entity = entity;
        add(editPanel = new DomainObjectEditPanel("editPanel", entity, object_id, parentId, parentEntity, DomainObjectList.SCROLL_PARAMETER));
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
                if (!DomainObjectAccessUtil.canEdit(entity, editPanel.getObject())) {
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
                if (!DomainObjectAccessUtil.canEditDisabled(entity, editPanel.getObject())) {
                    setVisible(false);
                }
                super.onBeforeRender();
            }
        });
    }
}

