package org.complitex.correction.web;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.correction.service.OrganizationCorrectionBean;
import org.complitex.correction.web.component.AbstractCorrectionEditPanel;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

import javax.ejb.EJB;
import java.util.Locale;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 29.11.13 18:44
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OrganizationCorrectionEdit extends FormTemplatePage {
    public static final String CORRECTION_ID = "correction_id";

    @EJB
    private OrganizationCorrectionBean organizationCorrectionBean;

    public OrganizationCorrectionEdit(PageParameters params) {
        add(new AbstractCorrectionEditPanel<OrganizationCorrection>("organization_edit_panel",
                params.get(CORRECTION_ID).toOptionalLong()) {

            @Override
            protected OrganizationCorrection getCorrection(Long correctionId) {
                return organizationCorrectionBean.geOrganizationCorrection(correctionId);
            }

            @Override
            protected OrganizationCorrection newCorrection() {
                return new OrganizationCorrection();
            }

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("internal_object");
            }

            @Override
            protected OrganizationPicker internalObjectPanel(String id) {
                return new OrganizationPicker(id, new PropertyModel<Long>(getCorrection(), "objectId"),
                        OrganizationTypeStrategy.SERVICING_ORGANIZATION_TYPE);
            }

            @Override
            protected String getNullObjectErrorMessage() {
                return getString("organization_required");
            }

            @Override
            protected boolean validateExistence() {
                return organizationCorrectionBean.getOrganizationCorrectionsCount(FilterWrapper.of(getCorrection())) > 0;
            }

            @Override
            protected Class<? extends Page> getBackPageClass() {
                return OrganizationCorrectionList.class;
            }

            @Override
            protected PageParameters getBackPageParameters() {
                return new PageParameters();
            }

            @Override
            protected void save() {
                organizationCorrectionBean.save(getCorrection());
            }

            @Override
            protected void delete() {
                organizationCorrectionBean.delete(getCorrection());
            }

            @Override
            protected IModel<String> getTitleModel() {
                return new ResourceModel("title");
            }
        });
    }
}
