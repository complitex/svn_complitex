/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.entity.example.ObjectCorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.pages.util.BuildingFormatter;

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
    protected List<BuildingCorrection> find(ObjectCorrectionExample example) {
        return addressCorrectionBean.findBuildings(example);
    }

    @Override
    protected String displayCorrection(ObjectCorrection correction) {
        BuildingCorrection buildingCorrection = (BuildingCorrection) correction;
        return BuildingFormatter.formatBuilding(buildingCorrection.getCorrection(), buildingCorrection.getCorrectionCorp(), getLocale());
    }
}
