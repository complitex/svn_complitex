/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountCorrectionDetail;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class DefaultCalculationCenterAdapter extends AbstractCalculationCenterAdapter {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCalculationCenterAdapter.class);

    protected static final String MAPPING_NAMESPACE = DefaultCalculationCenterAdapter.class.getName();

    @Override
    public void prepareCity(Payment payment, String city, Long cityId) {
        payment.setOutgoingCity(city);
    }

    @Override
    public void prepareDistrict(Payment payment, String district, Long districtId) {
        payment.setOutgoingDistrict(district);
    }

    @Override
    public void prepareStreet(Payment payment, String street, Long streetId) {
        payment.setOutgoingStreet(street);
    }

    @Override
    public void prepareStreetType(Payment payment, String streetType, Long streetTypeId) {
        payment.setOutgoingStreetType(streetType);
    }

    @Override
    public void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, Long buildingId) {
        payment.setOutgoingBuildingNumber(buildingNumber);
        payment.setOutgoingBuildingCorp(buildingCorp);
    }

    @Override
    public void prepareApartment(Payment payment, String apartment, Long apartmentId) {
        payment.setOutgoingApartment((String)payment.getField(PaymentDBF.FLAT));
    }

    @Override
    public void acquirePersonAccount(Payment payment) {
        SqlSession session = null;
        try {
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            params.put("pDistrName", payment.getOutgoingDistrict());
            params.put("pStSortName", payment.getOutgoingStreetType());
            params.put("pStreetName", payment.getOutgoingStreet());
            params.put("pHouseNum", payment.getOutgoingBuildingNumber());
            params.put("pHousePart", payment.getOutgoingBuildingCorp());
            params.put("pFlatNum", payment.getOutgoingApartment());
            params.put("dat1", (Date) payment.getField(PaymentDBF.DAT1));

            String result = (String) session.selectOne(MAPPING_NAMESPACE + ".acquirePersonAccount", params);
            processPersonAccountResult(payment, result);

            session.commit();
        } catch (Exception e) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception exc) {
                log.error("", exc);
            }
            log.error("", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    protected void processPersonAccountResult(Payment payment, String result) {
        if (result.equals("0")) {
            payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
        } else if (result.equals("-1")) {
            payment.setStatus(Status.MORE_ONE_ACCOUNTS);
        } else if (result.equals("-2")) {
            payment.setStatus(Status.APARTMENT_UNRESOLVED);
        } else if (result.equals("-3")) {
            payment.setStatus(Status.BUILDING_CORP_UNRESOLVED);
        } else if (result.equals("-4")) {
            payment.setStatus(Status.BUILDING_UNRESOLVED);
        } else if (result.equals("-5")) {
            payment.setStatus(Status.STREET_UNRESOLVED);
        } else if (result.equals("-6")) {
            payment.setStatus(Status.STREET_TYPE_UNRESOLVED);
        } else if (result.equals("-7")) {
            payment.setStatus(Status.DISTRICT_UNRESOLVED);
        } else {
            if (Strings.isEmpty(result)) {
                payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
            } else {
                payment.setAccountNumber(result);
                payment.setStatus(Status.ACCOUNT_NUMBER_RESOLVED);
            }
        }
    }

//    protected boolean processAccountCorrectionDetailsResult(Payment payment, String result) {
//        boolean error = true;
//        if (result.equals("0")) {
//            payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
//        } else if (result.equals("-2")) {
//            payment.setStatus(Status.APARTMENT_UNRESOLVED);
//        } else if (result.equals("-3")) {
//            payment.setStatus(Status.BUILDING_CORP_UNRESOLVED);
//        } else if (result.equals("-4")) {
//            payment.setStatus(Status.BUILDING_UNRESOLVED);
//        } else if (result.equals("-5")) {
//            payment.setStatus(Status.STREET_UNRESOLVED);
//        } else if (result.equals("-6")) {
//            payment.setStatus(Status.STREET_TYPE_UNRESOLVED);
//        } else if (result.equals("-7")) {
//            payment.setStatus(Status.DISTRICT_NOT_FOUND);
//        } else {
//            error = false;
//        }
//        return error;
//    }
    @Override
    public List<AccountCorrectionDetail> acquireAccountCorrectionDetails(Payment payment) {
        List<AccountCorrectionDetail> accountCorrectionDetails = null;
        SqlSession session = null;
        try {
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            params.put("pDistrName", payment.getOutgoingDistrict());
            params.put("pStSortName", payment.getOutgoingStreetType());
            params.put("pStreetName", payment.getOutgoingStreet());
            params.put("pHouseNum", payment.getOutgoingBuildingNumber());
            params.put("pHousePart", payment.getOutgoingBuildingCorp());
            params.put("pFlatNum", payment.getOutgoingApartment());
            params.put("dat1", (Date) payment.getField(PaymentDBF.DAT1));

            try {
                session.selectOne(MAPPING_NAMESPACE + ".acquireAccountCorrectionDetails", params);
//                if (processAccountCorrectionDetailsResult(payment, String.valueOf(resultCode))) {
                accountCorrectionDetails = (List<AccountCorrectionDetail>) params.get("details");
                if (accountCorrectionDetails != null) {
                    boolean isIncorrectResult = false;
                    for (AccountCorrectionDetail detail : accountCorrectionDetails) {
                        if (Strings.isEmpty(detail.getAccountNumber())) {
                            isIncorrectResult = true;
                            break;
                        }
                    }
                    if (isIncorrectResult) {
                        accountCorrectionDetails = null;
                    }
                }
//                }
            } catch (Exception e) {
                payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
            }

            session.commit();
        } catch (Exception e) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception exc) {
                log.error("", exc);
            }
            log.error("", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return accountCorrectionDetails;
    }

    @Override
    public void processPaymentAndBenefit(Payment payment, Benefit benefit) {
        SqlSession session = null;
        try {
            payment.setField(PaymentDBF.OPP, "00000001");
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            params.put("accountNumber", payment.getAccountNumber());
            params.put("dat1", (Date) payment.getField(PaymentDBF.DAT1));

            try {
                session.selectOne(MAPPING_NAMESPACE + ".processPaymentAndBenefit", params);
                List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("data");
                if (data != null && (data.size() == 1)) {
                    setPaymentData(payment, benefit, data.get(0));
                    payment.setStatus(Status.PROCESSED);
                    benefit.setStatus(Status.PROCESSED);
                } else {
                    payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("", e);
                payment.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
            }

            session.commit();
        } catch (Exception e) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception exc) {
                log.error("", exc);
            }
            log.error("", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    protected void setPaymentData(Payment payment, Benefit benefit, Map<String, Object> fromDb) {
        payment.setField(PaymentDBF.FROG, fromDb.get("FROG"));
        payment.setField(PaymentDBF.FL_PAY, fromDb.get("FL_PAY"));
        payment.setField(PaymentDBF.NM_PAY, fromDb.get("NM_PAY"));
        payment.setField(PaymentDBF.DEBT, fromDb.get("DEBT"));
        payment.setField(PaymentDBF.NORM_F_1, fromDb.get("NORM_F_1"));
        payment.setField(PaymentDBF.NUMB, fromDb.get("NUMB"));
        payment.setField(PaymentDBF.MARK, fromDb.get("MARK"));
        benefit.setField(BenefitDBF.CM_AREA, payment.getField(PaymentDBF.NORM_F_1));
    }
}
