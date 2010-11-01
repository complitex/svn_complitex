/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.CorrectionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * Список коррекций типов сущностей.
 * @author Artem
 */
public class EntityTypesCorrectionList extends AbstractCorrectionList {

    @EJB(name = "CorrectionBean")
    private CorrectionBean correctionBean;

    public EntityTypesCorrectionList(PageParameters params) {
        super(params);
    }

    @Override
    protected int count(CorrectionExample example) {
        return correctionBean.countEntityTypes(example);
    }

    @Override
    protected List<? extends Correction> find(CorrectionExample example) {
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
