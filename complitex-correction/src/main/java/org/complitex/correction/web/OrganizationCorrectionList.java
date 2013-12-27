package org.complitex.correction.web;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.correction.service.OrganizationCorrectionBean;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.List;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.13 15:36
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OrganizationCorrectionList extends AbstractCorrectionList<OrganizationCorrection> {
    @EJB
    private OrganizationCorrectionBean organizationCorrectionBean;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    public OrganizationCorrectionList() {
        super("organization");
    }

    @Override
    protected OrganizationCorrection newCorrection() {
        return new OrganizationCorrection();
    }

    @Override
    protected List<OrganizationCorrection> getCorrections(FilterWrapper<OrganizationCorrection> filterWrapper) {
        return organizationCorrectionBean.getOrganizationCorrections(filterWrapper);
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<OrganizationCorrection> filterWrapper) {
        return organizationCorrectionBean.getOrganizationCorrectionsCount(filterWrapper);
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return OrganizationCorrectionEdit.class;
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();

        if (objectCorrectionId != null) {
            parameters.set(OrganizationCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }

        return parameters;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("title");
    }

    @Override
    protected String displayInternalObject(Correction correction) {
        return organizationStrategy.displayShortNameAndCode(organizationStrategy.findById(correction.getObjectId(), true),
                getLocale());
    }

    @Override
    protected void onDelete(OrganizationCorrection correction) {
        organizationCorrectionBean.delete(correction);
    }

    @Override
    protected boolean isDeleteVisible() {
        return true;
    }
}
