/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.OwnershipCorrectionBean;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;
import org.complitex.osznconnection.file.service.TarifBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
            params.put("dat1", payment.getField(PaymentDBF.DAT1));

            String result = (String) session.selectOne(MAPPING_NAMESPACE + ".acquirePersonAccount", params);
            log.info("acquirePersonAccount, parameters : {}, account number : {}", params, result);
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
            payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
        } else if (result.equals("-1")) {
            payment.setStatus(RequestStatus.MORE_ONE_ACCOUNTS);
        } else if (result.equals("-2")) {
            payment.setStatus(RequestStatus.APARTMENT_UNRESOLVED);
        } else if (result.equals("-3")) {
            payment.setStatus(RequestStatus.BUILDING_CORP_UNRESOLVED);
        } else if (result.equals("-4")) {
            payment.setStatus(RequestStatus.BUILDING_UNRESOLVED);
        } else if (result.equals("-5")) {
            payment.setStatus(RequestStatus.STREET_UNRESOLVED);
        } else if (result.equals("-6")) {
            payment.setStatus(RequestStatus.STREET_TYPE_UNRESOLVED);
        } else if (result.equals("-7")) {
            payment.setStatus(RequestStatus.DISTRICT_UNRESOLVED);
        } else {
            if (Strings.isEmpty(result)) {
                payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
            } else {
                payment.setAccountNumber(result);
                payment.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) {
        List<AccountDetail> accountCorrectionDetails = null;
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
            params.put("dat1", payment.getField(PaymentDBF.DAT1));

            try {
                session.selectOne(MAPPING_NAMESPACE + ".acquireAccountCorrectionDetails", params);
                accountCorrectionDetails = (List<AccountDetail>) params.get("details");
                log.info("acquireAccountCorrectionDetails, parameters : {}", params);
                if (accountCorrectionDetails != null) {
                    boolean isIncorrectResult = false;
                    for (AccountDetail detail : accountCorrectionDetails) {
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
                log.error("", e);
                payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
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

    @SuppressWarnings({"unchecked"})
    @Override
    public void processPaymentAndBenefit(Payment payment, Benefit benefit, long calculationCenterId) {
        SqlSession session = null;
        try {
            payment.setField(PaymentDBF.OPP, "00000001");
            session = openSession();

            Map<String, Object> params = Maps.newHashMap();
            params.put("accountNumber", payment.getAccountNumber());
            params.put("dat1", payment.getField(PaymentDBF.DAT1));

            try {
                session.selectOne(MAPPING_NAMESPACE + ".processPaymentAndBenefit", params);
                List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("data");
                log.info("processPaymentAndBenefit, parameters : {}", params);
                if (data != null && (data.size() == 1)) {
                    processData(calculationCenterId, payment, benefit, data.get(0));
                } else {
                    payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("", e);
                payment.setStatus(RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
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

        Double T11_CS_UNI = (Double) data.get("T11_CS_UNI");
        Integer CODE2_1 = getCODE2_1(T11_CS_UNI, payment.getOrganizationId());
        if (CODE2_1 == null) {
            payment.setCalculationCenterCode2_1(T11_CS_UNI);
            payment.setStatus(RequestStatus.TARIF_CODE2_1_NOT_FOUND);
        } else {
            payment.setField(PaymentDBF.CODE2_1, CODE2_1);
            payment.setStatus(RequestStatus.PROCESSED);
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

    protected Integer getCODE2_1(Double T11_CS_UNI, long organizationId) {
        try {
            TarifBean tarifBean = getTarifBean();
            return tarifBean.getCODE2_1(T11_CS_UNI, organizationId);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void processBenefit(Date dat1, List<Benefit> benefits, long calculationCenterId) {
        SqlSession session = null;
        try {
            session = openSession();

            String accountNumber = benefits.get(0).getAccountNumber();
            Map<String, Object> params = Maps.newHashMap();
            params.put("accountNumber", accountNumber);
            params.put("dat1", dat1);

            try {
                session.selectOne(MAPPING_NAMESPACE + ".processBenefit", params);
                List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("benefitData");
                log.info("processBenefit, parameters : {}", params);
                if (data != null && !data.isEmpty()) {
                    processBenefitData(calculationCenterId, benefits, data);
                } else {
                    setStatus(benefits, RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("", e);
                setStatus(benefits, RequestStatus.ACCOUNT_NUMBER_NOT_FOUND);
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

    protected void processBenefitData(long calculationCenterId, List<Benefit> benefits, List<Map<String, Object>> data) {
        List<String> processed = Lists.newArrayList();
        Map<String, Object> el;
        for (final Map<String, Object> item : data) {
            final String inn = (String) item.get("INN");
            final String passportNumber = (String) item.get("PASSPORT_NUMBER");
//            log.info("INN : {}, Passport : {}", inn, passportNumber);
            if (!processed.contains(inn)) {
                List<Map<String, Object>> theSameMan;
                List<Benefit> theSameBenefits = findByINN(benefits, inn);
                if (!theSameBenefits.isEmpty()) {
                    theSameMan = Lists.newArrayList(Iterables.filter(data, new Predicate<Map<String, Object>>() {

                        @Override
                        public boolean apply(Map<String, Object> input) {
                            return input.get("INN").equals(inn);
                        }
                    }));
                } else {
                    theSameBenefits = findByPassportNumber(benefits, passportNumber);
                    if (!theSameBenefits.isEmpty()) {
                        theSameMan = Lists.newArrayList(Iterables.filter(data, new Predicate<Map<String, Object>>() {

                            @Override
                            public boolean apply(Map<String, Object> input) {
                                return input.get("INN").equals(inn);
                            }
                        }));
                    } else {
                        setStatus(benefits, RequestStatus.WRONG_ACCOUNT_NUMBER);
                        return;
                    }
                }
                if (theSameMan != null && !theSameMan.isEmpty()) {
                    el = Collections.min(theSameMan, new Comparator<Map<String, Object>>() {

                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            String benefitCode1 = (String) o1.get("BENEFIT_CODE");
                            Integer i1 = null;
                            try {
                                i1 = Integer.parseInt(benefitCode1);
                            } catch (NumberFormatException e) {
                                //oh
                            }

                            String benefitCode2 = (String) o2.get("BENEFIT_CODE");
                            Integer i2 = null;
                            try {
                                i2 = Integer.parseInt(benefitCode2);
                            } catch (NumberFormatException e) {
                                //ah
                            }

                            if (i1 != null && i2 != null) {
                                return i1.compareTo(i2);
                            } else {
                                return benefitCode1.compareTo(benefitCode2);
                            }
                        }
                    });
                    String cmBenefitCode = (String) el.get("BENEFIT_CODE");
                    String osznBenefitCode = getOSZNPrivilegeCode(cmBenefitCode, calculationCenterId, benefits.get(0).getOrganizationId());
                    if (osznBenefitCode == null) {
                        setStatus(theSameBenefits, RequestStatus.BENEFIT_NOT_FOUND);
                    } else {
                        for (Benefit benefit : theSameBenefits) {
                            benefit.setField(BenefitDBF.PRIV_CAT, osznBenefitCode);
                            benefit.setField(BenefitDBF.ORD_FAM, el.get("ORD_FAM"));
                        }
                    }
                    processed.add(inn);
                }
            }
        }
        setStatus(benefits, RequestStatus.PROCESSED);
    }

    protected void setStatus(List<Benefit> benefits, RequestStatus status) {
        for (Benefit benefit : benefits) {
            benefit.setStatus(status);
        }
    }

    protected String getOSZNPrivilegeCode(String calculationCenterPrivilege, long calculationCenterId, long osznId) {
        return getEjbBean(PrivilegeCorrectionBean.class).getOSZNPrivilegeCode(calculationCenterPrivilege, calculationCenterId, osznId);
    }

    protected List<Benefit> findByPassportNumber(List<Benefit> benefits, final String passportNumber) {
        if (!Strings.isEmpty(passportNumber)) {
            return Lists.newArrayList(Iterables.filter(benefits, new Predicate<Benefit>() {

                @Override
                public boolean apply(Benefit benefit) {
                    return passportNumber.equals(benefit.getField(BenefitDBF.PSP_NUM));
                }
            }));
        } else {
            return Collections.emptyList();
        }
    }

    protected List<Benefit> findByINN(List<Benefit> benefits, final String inn) {
        return Lists.newArrayList(Iterables.filter(benefits, new Predicate<Benefit>() {

            @Override
            public boolean apply(Benefit benefit) {
                return inn.equals(benefit.getField(BenefitDBF.IND_COD));
            }
        }));
    }
}
