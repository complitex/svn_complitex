package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import java.util.Collection;
import org.complitex.osznconnection.file.calculation.adapter.AccountNotFoundException;
import org.complitex.osznconnection.file.calculation.entity.BenefitData;
import org.complitex.osznconnection.file.calculation.service.CalculationCenterBean;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.RequestStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.complitex.osznconnection.file.entity.RequestFileGroup.STATUS.FILLED;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.11.10 17:57
 */
@Stateless(name = "BenefitFillBean")
public class BenefitFillService {

    private static final Logger log = LoggerFactory.getLogger(BenefitFillService.class);

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB(beanName = "CalculationCenterBean")
    private CalculationCenterBean calculationCenterBean;

    @EJB(beanName = "PrivilegeCorrectionBean")
    private PrivilegeCorrectionBean privilegeCorrectionBean;
    
    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    public Collection<BenefitData> getBenefitData(Benefit benefit) throws AccountNotFoundException {
        long osznId = benefit.getOrganizationId();
        long calculationCenterId = calculationCenterBean.getCurrentCalculationCenterInfo().getCalculationCenterId();
        ICalculationCenterAdapter adapter = calculationCenterBean.getDefaultCalculationCenterAdapter();
        Collection<BenefitData> benefitData = adapter.getBenefitData(benefit.getAccountNumber(), 
                benefitBean.findDat1(benefit.getAccountNumber(), benefit.getRequestFileId()));
        Collection<BenefitData> notConnectedBenefitData = Lists.newArrayList();
        List<Benefit> benefits = benefitBean.findByAccountNumber(benefit.getAccountNumber(), benefit.getRequestFileId());

        for (BenefitData benefitDataItem : benefitData) {
            boolean suitable = true;
            String osznBenefitCode = privilegeCorrectionBean.getOSZNPrivilegeCode(benefitDataItem.getCode(), calculationCenterId, osznId);

            Integer benefitCodeAsInt = null;
            try {
                benefitCodeAsInt = Integer.valueOf(osznBenefitCode);
            } catch (NumberFormatException e) {
            }

            for (Benefit benefitItem : benefits) {
                Integer benefitItemCode = (Integer) benefitItem.getField(BenefitDBF.PRIV_CAT);
                if (benefitItemCode != null && benefitItemCode.equals(benefitCodeAsInt)) {
                    suitable = false;
                }
            }

            if (suitable) {
                benefitDataItem.setOsznBenefitCode(osznBenefitCode);
                notConnectedBenefitData.add(benefitDataItem);
            }
        }

        return notConnectedBenefitData;
    }

    public void connectBenefit(Benefit benefit, BenefitData benefitData) {
        String osznBenefitCode = benefitData.getOsznBenefitCode();
        if (osznBenefitCode == null) {
            benefit.setStatus(RequestStatus.BENEFIT_NOT_FOUND);
        } else {
            benefit.setField(BenefitDBF.PRIV_CAT, Integer.valueOf(osznBenefitCode));
            benefit.setField(BenefitDBF.ORD_FAM, Integer.valueOf(benefitData.getOrderFamily()));
            benefit.setStatus(RequestStatus.PROCESSED);
        }

        benefitBean.update(benefit);

        try {
            Collection<BenefitData> leftBenefitData = getBenefitData(benefit);
            if (leftBenefitData == null || leftBenefitData.isEmpty()) {
                benefitBean.updateStatusByAccountNumber(benefit.getRequestFileId(), benefit.getAccountNumber(), RequestStatus.PROCESSED);
            }
        } catch (AccountNotFoundException e) {
        }

        long benefitFileId = benefit.getRequestFileId();
        long paymentFileId = requestFileGroupBean.getPaymentFileId(benefit.getRequestFileId());
        if (benefitBean.isBenefitFileProcessed(benefitFileId) && paymentBean.isPaymentFileProcessed(paymentFileId)) {
            requestFileGroupBean.updateStatus(benefitFileId, FILLED);
        }
    }
}
