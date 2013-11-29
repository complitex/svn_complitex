package org.complitex.correction.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.dictionary.entity.FilterWrapper;

import java.util.List;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.13 15:36
 */
public class OrganizationCorrectionList extends AbstractCorrectionList<OrganizationCorrection> {
    public OrganizationCorrectionList() {
        super("organization");
    }

    @Override
    protected OrganizationCorrection newCorrection() {
        return null;
    }

    @Override
    protected List<OrganizationCorrection> getCorrections(FilterWrapper<OrganizationCorrection> filterWrapper) {
        return null;
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<OrganizationCorrection> filterWrapper) {
        return null;
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return null;
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        return null;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return null;
    }
}
