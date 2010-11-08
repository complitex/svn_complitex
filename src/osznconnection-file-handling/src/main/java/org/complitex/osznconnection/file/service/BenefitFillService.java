package org.complitex.osznconnection.file.service;

import org.complitex.osznconnection.file.calculation.entity.BenefitData;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.RequestStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

import static org.complitex.osznconnection.file.entity.RequestFileGroup.STATUS.FILLED;
import static org.complitex.osznconnection.file.entity.RequestFileGroup.STATUS.FILL_ERROR;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.11.10 17:57
 */
@Stateless(name = "BenefitFillBean")
public class BenefitFillService {
    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @EJB(beanName = "PrivilegeCorrectionBean")
    private PrivilegeCorrectionBean privilegeCorrectionBean;

    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    public List<BenefitData> getBenefitData(Benefit benefit){
        List<BenefitData> list = calculationCenterBean.getDefaultCalculationCenterAdapter()
                .getBenefitData(benefit.getAccountNumber(), benefitBean.findDat1(benefit));

        List<Benefit> benefits = benefitBean.findByAccountNumber(benefit.getAccountNumber(), benefit.getRequestFileId());

        for (Benefit b : benefits){
            for (int i=0; i < list.size(); ++i){
                if (b.getField(BenefitDBF.ORD_FAM) != null && b.getField(BenefitDBF.ORD_FAM).equals(list.get(i).getCode())){
                    list.remove(i);
                    i--;
                }
            }
        }

        return list;
    }

    public void connectBenefit(Benefit benefit, BenefitData benefitData){
        String osznBenefitCode = privilegeCorrectionBean.getOSZNPrivilegeCode(benefitData.getCode(),
                calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId(),
                benefit.getOrganizationId());

        if (osznBenefitCode == null) {
            benefit.setStatus(RequestStatus.BENEFIT_NOT_FOUND);
        } else {
            benefit.setField(BenefitDBF.PRIV_CAT, osznBenefitCode);
            benefit.setField(BenefitDBF.ORD_FAM, benefitData.getCode());
            benefit.setStatus(RequestStatus.PROCESSED);
        }

        benefitBean.update(benefit);

        boolean filled = benefitBean.isBenefitFileProcessed(benefit.getRequestFileId());

        requestFileGroupBean.updateStatus(benefit, filled ? FILLED : FILL_ERROR);
    }
}
