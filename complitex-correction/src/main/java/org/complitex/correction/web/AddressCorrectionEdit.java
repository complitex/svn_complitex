package org.complitex.correction.web;

import com.google.common.collect.Lists;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.web.component.*;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

import java.util.List;

/**
 * Страница для редактирования коррекций адресов.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class AddressCorrectionEdit extends FormTemplatePage {
    public static final String CORRECTED_ENTITY = "entity";
    public static final String CORRECTION_ID = "correction_id";

    private AbstractCorrectionEditPanel addressEditPanel;

    public AddressCorrectionEdit(PageParameters params) {
        String entity = params.get(CORRECTED_ENTITY).toString();
        Long correctionId = params.get(CORRECTION_ID).toOptionalLong();
        switch (entity) {
            case "city":
                addressEditPanel = new CityCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "district":
                addressEditPanel = new DistrictCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "street":
                addressEditPanel = new StreetCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "building":
                addressEditPanel = new BuildingCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "apartment":
                addressEditPanel = new ApartmentCorrectionEditPanel("addressEditPanel", correctionId);
                break;
            case "room":
                addressEditPanel = new RoomCorrectionEditPanel("addressEditPanel", correctionId);
                break;
        }
        add(addressEditPanel);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                addressEditPanel.executeDeletion();
            }

            @Override
            public boolean isVisible() {
                return !addressEditPanel.isNew();
            }
        });
        return toolbar;
    }
}
