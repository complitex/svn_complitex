/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Comparator;
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
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.OwnershipCorrectionBean;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;
import org.complitex.osznconnection.file.service.TarifBean;
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
    public void prepareCity(Payment payment, String city, String cityCode) {
        payment.setOutgoingCity(city);
    }

    @Override
    public void prepareDistrict(Payment payment, String district, String districtCode) {
        payment.setOutgoingDistrict(district);
    }

    @Override
    public void prepareStreet(Payment payment, String street, String streetCode) {
        payment.setOutgoingStreet(street);
    }

    @Override
    public void prepareStreetType(Payment payment, String streetType, String streetTypeCode) {
        payment.setOutgoingStreetType(streetType);
    }

    @Override
    public void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, String buildingCode) {
        payment.setOutgoingBuildingNumber(buildingNumber);
        payment.setOutgoingBuildingCorp(buildingCorp);
    }

    @Override
    public void prepareApartment(Payment payment, String apartment, String apartmentCode) {
        String flat = (String) payment.getField(PaymentDBF.FLAT);
        if (flat != null) {
            flat = flat.trim();
        }
        if (Strings.isEmpty(flat)) {
            flat = "";
        }
        payment.setOutgoingApartment(flat);
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
    public void processPaymentAndBenefit(Payment payment, Benefit benefit, long calculationCenterId) {
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
                    processData(calculationCenterId, payment, benefit, data.get(0));
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

    protected OwnershipCorrectionBean getOwnershipCorrectionBean() {
        return getEjbBean(OwnershipCorrectionBean.class);
    }

    protected TarifBean getTarifBean() {
        return getEjbBean(TarifBean.class);
    }

    protected void processData(long calculationCenterId, Payment payment, Benefit benefit, Map<String, Object> data) {
        //payment
        payment.setField(PaymentDBF.FROG, data.get("FROG"));
        payment.setField(PaymentDBF.FL_PAY, data.get("FL_PAY"));
        payment.setField(PaymentDBF.NM_PAY, data.get("NM_PAY"));
        payment.setField(PaymentDBF.DEBT, data.get("DEBT"));
        payment.setField(PaymentDBF.NORM_F_1, data.get("NORM_F_1"));
        payment.setField(PaymentDBF.NUMB, data.get("NUMB"));
        payment.setField(PaymentDBF.MARK, data.get("MARK"));

        Integer CODE2_1 = getCODE2_1((Double) data.get("T11_CS_UNI"));
        if (CODE2_1 == null) {
            payment.setStatus(Status.TARIF_CODE2_1_NOT_FOUND);
        } else {
            payment.setField(PaymentDBF.CODE2_1, CODE2_1);
            payment.setStatus(Status.PROCESSED);
        }

        //benefit
        benefit.setField(BenefitDBF.CM_AREA, payment.getField(PaymentDBF.NORM_F_1));
        benefit.setField(BenefitDBF.HOSTEL, data.get("HOSTEL"));
        benefit.setField(BenefitDBF.OWN_FRM, getOSZNOwnershipCode((String) data.get("OWN_FRM"), calculationCenterId, payment.getOrganizationId()));
    }

    protected String getOSZNOwnershipCode(String calculationCenterOwnership, long calculationCenterId, long osznId) {
        try {
            OwnershipCorrectionBean ownershipCorrectionBean = getOwnershipCorrectionBean();
            return ownershipCorrectionBean.getOSZNOwnershipCode(calculationCenterOwnership, calculationCenterId, osznId);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    protected Integer getCODE2_1(Double T11_CS_UNI) {
        try {
            TarifBean tarifBean = getTarifBean();
            return tarifBean.getCODE2_1(T11_CS_UNI);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    @Override
    public void processBenefit(Benefit benefit, long calculationCenterId) {
        SqlSession session = null;
        try {
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            params.put("accountNumber", benefit.getAccountNumber());
            Date dat1 = getDat1(benefit.getId());
            if (dat1 == null) {
                benefit.setStatus(Status.PROCESSED);
                return;
            }
            params.put("dat1", dat1);

            try {
                session.selectOne(MAPPING_NAMESPACE + ".processBenefit", params);
                List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("benefitData");
                if (data != null && !data.isEmpty()) {
                    processBenefitData(calculationCenterId, benefit, data);
                } else {
                    benefit.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("", e);
                benefit.setStatus(Status.ACCOUNT_NUMBER_NOT_FOUND);
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

    protected void processBenefitData(long calculationCenterId, Benefit benefit, List<Map<String, Object>> data) {
        log.info("Process benefit, data : {}", data);
        List<String> processed = Lists.newArrayList();
        Map<String, Object> el = null;
        for (final Map<String, Object> item : data) {
            final String inn = (String) item.get("INN");
            final String passportNumber = (String) item.get("PASSPORT_NUMBER");
            log.info("INN : {}, Passport : {}", inn, passportNumber);
            if (!processed.contains(inn)) {
                List<Map<String, Object>> theSameMan = null;
                if (existsWithINN(benefit, inn) || (!Strings.isEmpty(passportNumber) && existsWithPassportNumber(benefit, passportNumber))) {
                    theSameMan = Lists.newArrayList(Iterables.filter(data, new Predicate<Map<String, Object>>() {

                        @Override
                        public boolean apply(Map<String, Object> input) {
                            return input.get("INN").equals(inn);
                        }
                    }));
                } else {
                    benefit.setStatus(Status.WRONG_ACCOUNT_NUMBER);
                    setWrongAccountNumber(benefit.getAccountNumber());
                    return;
                }
                if (theSameMan != null) {
                    el = Collections.min(theSameMan, new Comparator<Map<String, Object>>() {

                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            //TODO: reimplement
                            return ((String) o1.get("BENEFIT_CODE")).compareTo((String) o2.get("BENEFIT_CODE"));
                        }
                    });
                    String cmBenefitCode = (String) el.get("BENEFIT_CODE");
                    String osznBenefitCode = getOSZNPrivilegeCode(cmBenefitCode, calculationCenterId, benefit.getOrganizationId());
                    if (osznBenefitCode == null) {
                        benefit.setStatus(Status.BENEFIT_NOT_FOUND);
                    } else {
                        benefit.setField(BenefitDBF.PRIV_CAT, osznBenefitCode);
                        benefit.setField(BenefitDBF.ORD_FAM, el.get("ORD_FAM"));
                        benefit.setStatus(Status.PROCESSED);
                        updateBenefit(inn, passportNumber, benefit);
                    }
                    processed.add(inn);
                }
            }
        }
    }

    protected void updateBenefit(String inn, String passportNumber, Benefit benefit) {
        getEjbBean(BenefitBean.class).updateByInnOrPassportNumber(inn, passportNumber, benefit);
    }

    protected void setWrongAccountNumber(String accountNumber) {
        getEjbBean(BenefitBean.class).setWrongAccountNumber(accountNumber);
    }

    protected String getOSZNPrivilegeCode(String calculationCenterPrivilege, long calculationCenterId, long osznId) {
        return getEjbBean(PrivilegeCorrectionBean.class).getOSZNPrivilegeCode(calculationCenterPrivilege, calculationCenterId, osznId);
    }

    protected boolean existsWithPassportNumber(Benefit benefit, String passportNumber) {
        return getEjbBean(BenefitBean.class).existsWithPassportNumber(benefit.getRequestFileId(), passportNumber);
    }

    protected boolean existsWithINN(Benefit benefit, String inn) {
        return getEjbBean(BenefitBean.class).existsWithInn(benefit.getRequestFileId(), inn);
    }

    protected Date getDat1(long benefitId) {
//        try {
//            PersonAccountLocalBean personAccountLocalBean = getEjbBean(PersonAccountLocalBean.class);
//            return personAccountLocalBean.findDat1(ownNumSr, accountNumber);
//        } catch (Exception e) {
//            log.error("", e);
//        }
//        return null;
        return getEjbBean(BenefitBean.class).findDat1(benefitId);
    }
}
