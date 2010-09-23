/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.entity.example.ObjectCorrectionExample;
import org.complitex.osznconnection.file.service.CorrectionBean;

/**
 *
 * @author Artem
 */
public class EntityTypesCorrectionList extends AbstractCorrectionList {

    @EJB(name = "CorrectionBean")
    private CorrectionBean correctionBean;

    public EntityTypesCorrectionList(PageParameters params) {
        super(params);
    }

    @Override
    protected int count(ObjectCorrectionExample example) {
        return correctionBean.countEntityTypes(example);
    }

    @Override
    protected List<? extends ObjectCorrection> find(ObjectCorrectionExample example) {
        return correctionBean.findEntityTypes(example);
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return EntityTypeCorrectionEdit.class;
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        if (objectCorrectionId != null) {
            parameters.put(EntityTypeCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }

    @Override
    protected String getInternalObjectOrderByExpression() {
        return "internalObject";
    }
}
