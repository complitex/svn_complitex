/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.pages.util.BuildingFormatter;

import javax.ejb.EJB;
import java.util.List;

/**
 * Список коррекций домов.
 * @author Artem
 */
public class BuildingCorrectionList extends AddressCorrectionList {

    @EJB(name = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    public BuildingCorrectionList(PageParameters params) {
        super(params);
    }

    @Override
    protected List<BuildingCorrection> find(CorrectionExample example) {
        return addressCorrectionBean.findBuildings(example);
    }

    @Override
    protected int count(CorrectionExample example) {
        return addressCorrectionBean.countBuildings(example);
    }

    @Override
    protected String displayCorrection(Correction correction) {
        String parentAddress = "";
        if (correction.getParent() != null && correction.getParent() != null) {
            parentAddress = correction.getParent().getParent().getCorrection()
                    + ", " + correction.getParent().getCorrection()
                    + ", ";
        }

        BuildingCorrection bc = (BuildingCorrection) correction;
        return parentAddress + BuildingFormatter.formatBuilding(bc.getCorrection(), bc.getCorrectionCorp(), getLocale());
    }
}
