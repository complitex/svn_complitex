/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.calculation.entity.BenefitData;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.OwnershipCorrectionBean;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;
import org.complitex.osznconnection.file.service.TarifBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.*;
import org.complitex.dictionaryfw.entity.Log.EVENT;
import org.complitex.dictionaryfw.service.LocaleBean;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.service.warning.RequestWarningBean;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;

/**
 * Класс по умолчанию для взаимодействия с ЦН.
 * @author Artem
 */
@Stateless(name = "org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter")
@TransactionManagement(TransactionManagementType.BEAN)
public class DefaultCalculationCenterAdapter extends AbstractCalculationCenterAdapter {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCalculationCenterAdapter.class);

    protected static final String MAPPING_NAMESPACE = DefaultCalculationCenterAdapter.class.getName();

    @EJB(beanName = "OwnershipCorrectionBean")
    private OwnershipCorrectionBean ownershipCorrectionBean;

    @EJB(beanName = "TarifBean")
    private TarifBean tarifBean;

    @EJB(beanName = "PrivilegeCorrectionBean")
    private PrivilegeCorrectionBean privilegeCorrectionBean;

    @EJB
    private LogBean logBean;

    @EJB
    private LocaleBean localeBean;

    @EJB
    private RequestWarningBean warningBean;
    
    @EJB
    private WebWarningRenderer webWarningRenderer;

    /**
     * Группа методов для проставления "внешнего адреса", т.е. адреса для ЦН, по полному названию и коду коррекции
     * элемента адреса, полученному из таблиц коррекций.
     */
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

    /**
     * Для квартиры номер проставляется напрямую из ОСЗН адреса, с обрезанием начальных и конечных пробелов.
     * @param payment
     * @param apartment
     * @param apartmentCode
     */
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

    /**
     * Получить номер личного счета в ЦН.
     * @param payment запрос начислений
     */
    @Override
    public void acquirePersonAccount(Payment payment) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("pDistrName", payment.getOutgoingDistrict());
        params.put("pStSortName", payment.getOutgoingStreetType());
        params.put("pStreetName", payment.getOutgoingStreet());
        params.put("pHouseNum", payment.getOutgoingBuildingNumber());
        params.put("pHousePart", payment.getOutgoingBuildingCorp());
        params.put("pFlatNum", payment.getOutgoingApartment());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        String result = (String) sqlSession().selectOne(MAPPING_NAMESPACE + ".acquirePersonAccount", params);
        log.info("acquirePersonAccount, parameters : {}, account number : {}", params, result);
        processPersonAccountResult(payment, result);
    }

    /**
     * Обработка возвращаемых значений при получении л/с.
     * 0 - нет л/с,
     * -1 - больше 1 л/с, когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
     * -2 - нет квартиры,
     * -3 - нет корпуса,
     * -4 - нет дома,
     * -5 - нет улицы,
     * -6 - нет типа улицы,
     * -7 - нет района,
     * остальное - номер л/с
     * 
     * @param payment запрос начислений
     * @param result результат
     */
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

    /**
     * Получить детали по л/c, если acquirePersonAccount() возвратила код -1 : больше 1 л/с.
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETACCATTRS.
     * Используется для уточнения в UI номера л/c, когда больше одного человека в ЦН, имеющие разные номера л/c,
     * привязаны к одному адресу и для поиска номеров л/c в PaymentLookupPanel.
     * См. также PaymentLookupBean.getAccounts().
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт,
     * и драйвер с соотвествии со стандартом JDBC рассматривает закрытый курсор как ошибку и выбрасывает исключение.
     * 
     * @param payment
     * @return
     */
    @Override
    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) throws AccountNotFoundException {
        Map<String, Object> params = Maps.newHashMap();

        params.put("pDistrName", payment.getOutgoingDistrict());
        params.put("pStSortName", payment.getOutgoingStreetType());
        params.put("pStreetName", payment.getOutgoingStreet());
        params.put("pHouseNum", payment.getOutgoingBuildingNumber());
        params.put("pHousePart", payment.getOutgoingBuildingCorp());
        params.put("pFlatNum", payment.getOutgoingApartment());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".acquireAccountCorrectionDetails", params);
        } catch (Exception e) {
            throw new AccountNotFoundException(e, payment);
        } finally {
            log.info("acquireAccountCorrectionDetails, parameters : {}", params);
        }

        List<AccountDetail> accountCorrectionDetails = (List<AccountDetail>) params.get("details");
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

        return accountCorrectionDetails;
    }

    /**
     * Обработать payment и заполнить некоторые поля в соответствующих данному payment benefit записях.
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETCHARGEANDPARAMS.
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт, и драйвер с соотвествии со стандартом JDBC рассматривает
     * закрытый курсор как ошибку и выбрасывает исключение.
     *
     * @param payment
     * @param benefits
     * @param calculationCenterId
     */
    @Override
    public void processPaymentAndBenefit(Payment payment, List<Benefit> benefits, long calculationCenterId) throws AccountNotFoundException {
        payment.setField(PaymentDBF.OPP, "00000001");

        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", payment.getAccountNumber());
        params.put("dat1", payment.getField(PaymentDBF.DAT1));

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".processPaymentAndBenefit", params);
        } catch (Exception e) {
            throw new AccountNotFoundException(e, payment);
        } finally {
            log.info("processPaymentAndBenefit, parameters : {}", params);
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("data");
        if (data != null && (data.size() == 1)) {
            processData(calculationCenterId, payment, benefits, data.get(0));
        } else {
            throw new AccountNotFoundException(null, payment);
        }
    }

    /**
     * Заполнить payment и benefit записи данными из processPaymentAndBenefit().
     * Для payment:
     * Все поля кроме CODE2_1 проставляются напрямую из data в payment.
     * Поле CODE2_1 заполняются не напрямую, а по таблице тарифов(метод TarifBean.getCODE2_1()).
     * Если в тарифах не нашли, то проставляем статус RequestStatus.TARIF_CODE2_1_NOT_FOUND и значение из ЦН(T11_CS_UNI) в
     * сalculationCenterCode2_1 для дальнейшего отображения в UI деталей. Иначе тариф сохраняется в payment и он считается обработанным.
     *
     * Для benefit:
     * Все поля проставляются напрямую, кроме:
     * поле OWN_FRM проставляется из таблицы коррекций для форм власти(ownership). На данный момент для всех форм власти в ЦН существуют коррекции,
     * поэтому ситуации с не найденной коррекцией нет.
     *
     * @param calculationCenterId id ЦН
     * @param payment
     * @param benefits
     * @param benefitData данные пришедшие из ЦН.
     */
    protected void processData(long calculationCenterId, Payment payment, List<Benefit> benefits, Map<String, Object> data) {
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
            payment.setStatus(RequestStatus.TARIF_CODE2_1_NOT_FOUND);

            log.warn("Couldn't find tarif code by calculation center's tarif: '{}' and calculation center id: {}", T11_CS_UNI, calculationCenterId);

            RequestWarning warning = new RequestWarning(payment.getId(), RequestFile.TYPE.PAYMENT, RequestWarningStatus.TARIF_NOT_FOUND);
            warning.addParameter(new RequestWarningParameter(0, T11_CS_UNI));
            warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
            warningBean.save(warning);

            logBean.error(Module.NAME, getClass(), Payment.class, payment.getId(), EVENT.EDIT,
                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
        } else {
            payment.setField(PaymentDBF.CODE2_1, CODE2_1);
            payment.setStatus(RequestStatus.PROCESSED);
        }

        //benefits
        if (benefits != null && !benefits.isEmpty()) {
            String calcCenterOwnershipCode = (String) data.get("OWN_FRM");
            Long internalOwnershipId = findInternalOwnership(calcCenterOwnershipCode, calculationCenterId);
            if (internalOwnershipId == null) {
                log.warn("Couldn't find in corrections internal ownership object by calculation center's ownership code: '{}' "
                        + "and calculation center id: {}", calcCenterOwnershipCode, calculationCenterId);

                for (Benefit benefit : benefits) {
                    RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT, RequestWarningStatus.OWNERSHIP_OBJECT_NOT_FOUND);
                    warning.addParameter(new RequestWarningParameter(0, calcCenterOwnershipCode));
                    warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                    warningBean.save(warning);

                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                            webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                }
            } else {
                long osznId = payment.getOrganizationId();
                String osznOwnershipCode = findOSZNOwnershipCode(internalOwnershipId, osznId);
                if (osznOwnershipCode == null) {
                    log.warn("Couldn't find in corrections oszn's ownership code by internal ownership object id: {} "
                            + "and oszn id: {}", internalOwnershipId, osznId);

                    for (Benefit benefit : benefits) {
                        RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                RequestWarningStatus.OWNERSHIP_CODE_NOT_FOUND);
                        warning.addParameter(new RequestWarningParameter(0, "ownership", internalOwnershipId));
                        warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                        warningBean.save(warning);

                        logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                    }
                } else {
                    Integer ownershipCodeAsInt = null;
                    try {
                        ownershipCodeAsInt = Integer.valueOf(osznOwnershipCode);
                    } catch (NumberFormatException e) {
                        log.warn("Couldn't transform OWN_FRM value '{}' from correction to integer value. Oszn id: {}, internal ownership id: {}",
                                new Object[]{osznOwnershipCode, osznId, internalOwnershipId});

                        for (Benefit benefit : benefits) {
                            RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                    RequestWarningStatus.OWNERSHIP_CODE_INVALID);
                            warning.addParameter(new RequestWarningParameter(0, osznOwnershipCode));
                            warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                            warning.addParameter(new RequestWarningParameter(2, "ownership", internalOwnershipId));
                            warningBean.save(warning);

                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                        }
                    }

                    if (ownershipCodeAsInt != null) {
                        for (Benefit benefit : benefits) {
                            benefit.setField(BenefitDBF.OWN_FRM, osznOwnershipCode);
                        }
                    }
                }
            }
            for (Benefit benefit : benefits) {
                benefit.setField(BenefitDBF.CM_AREA, payment.getField(PaymentDBF.NORM_F_1));
                benefit.setField(BenefitDBF.HOSTEL, data.get("HOSTEL"));
            }
        }
    }

    protected Long findInternalOwnership(String calculationCenterOwnership, long calculationCenterId) {
        return ownershipCorrectionBean.findInternalOwnership(calculationCenterOwnership, calculationCenterId);
    }

    protected String findOSZNOwnershipCode(Long internalOwnership, long osznId) {
        return ownershipCorrectionBean.findOwnershipCode(internalOwnership, osznId);
    }

    /**
     * Получить тариф.
     * См. TarifBean.getCODE2_1().
     *
     * @param T11_CS_UNI
     * @param organizationId
     * @return
     */
    protected Integer getCODE2_1(Double T11_CS_UNI, long organizationId) {
        return tarifBean.getCODE2_1(T11_CS_UNI, organizationId);
    }

    /**
     * Обработать группу benefit записей с одинаковым account number.
     * Процедура COMP.Z$RUNTIME_SZ_UTL.GETPRIVS.
     *
     * При возникновении ошибок при вызове процедуры проставляется статус RequestStatus.ACCOUNT_NUMBER_NOT_FOUND.
     * Так сделано потому, что проанализировать возвращаемое из процедуры значение не удается если номер л/c не найден
     * в ЦН по причине того что курсор в этом случае закрыт, и драйвер с соответствии со стандартом JDBC рассматривает
     * закрытый курсор как ошибку и выбрасывает исключение.
     *
     * @param dat1 дата из поля DAT1 payment записи, соответствующей группе benefits записей со значением в поле FROG большим 0
     * @param benefits группа benefit записей
     * @param calculationCenterId id ЦН
     */
    @Override
    public void processBenefit(Date dat1, List<Benefit> benefits, long calculationCenterId) throws AccountNotFoundException {
        String accountNumber = benefits.get(0).getAccountNumber();

        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", accountNumber);
        params.put("dat1", dat1);

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".processBenefit", params);
        } catch (Exception e) {
            throw new AccountNotFoundException(e, benefits);
        } finally {
            log.info("processBenefit, parameters : {}", params);
        }

        List<BenefitData> benefitData = (List<BenefitData>) params.get("benefitData");
        if (benefitData != null && !benefitData.isEmpty()) {
            processBenefitData(calculationCenterId, benefits, benefitData);
        } else {
            throw new AccountNotFoundException(null, benefits);
        }
    }

    @Override
    public Collection<BenefitData> getBenefitData(String accountNumber, Date dat1) throws AccountNotFoundException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("accountNumber", accountNumber);
        params.put("dat1", new Date());

        try {
            sqlSession().selectOne(MAPPING_NAMESPACE + ".getBenefitData", params);
        } catch (Exception e) {
            throw new AccountNotFoundException(e);
        } finally {
            log.info("getBenefitData, parameters : {}", params);
        }

        List<BenefitData> benefitData = (List<BenefitData>) params.get("benefitData");
        Map<String, BenefitData> innToBenefitDataMap = Maps.newHashMap();
        for (BenefitData item : benefitData) {
            final String inn = item.getInn();
            if (innToBenefitDataMap.get(inn) == null) {
                List<BenefitData> theSameMans = getBenefitDataByINN(benefitData, inn);
                if (theSameMans != null && !theSameMans.isEmpty()) {
                    BenefitData min = Collections.min(theSameMans, BENEFIT_DATA_COMPARATOR);
                    innToBenefitDataMap.put(inn, min);
                }
            }
        }
        return innToBenefitDataMap.values();
    }

    protected List<BenefitData> getBenefitDataByINN(List<BenefitData> benefitDatas, final String inn) {
        return Lists.newArrayList(Iterables.filter(benefitDatas, new Predicate<BenefitData>() {

            @Override
            public boolean apply(BenefitData benefitData) {
                return benefitData.getInn().equals(inn);
            }
        }));
    }

    private static class BenefitDataComparator implements Comparator<BenefitData> {

        @Override
        public int compare(BenefitData o1, BenefitData o2) {
            String benefitCode1 = o1.getCode();
            Integer i1 = null;
            try {
                i1 = Integer.parseInt(benefitCode1);
            } catch (NumberFormatException e) {
            }

            String benefitCode2 = o2.getCode();
            Integer i2 = null;
            try {
                i2 = Integer.parseInt(benefitCode2);
            } catch (NumberFormatException e) {
            }

            if (i1 != null && i2 != null) {
                return i1.compareTo(i2);
            } else {
                return benefitCode1.compareTo(benefitCode2);
            }
        }
    }
    private static final BenefitDataComparator BENEFIT_DATA_COMPARATOR = new BenefitDataComparator();

    /**
     * Из требований заказчика: "Среди жильцов на обрабатываемом л/с по идентификационному коду (IND_COD) пытаемся
     * найти носителя льготы, если не нашли, то ищем по номеру паспорта (без серии). Если нашли, проставляем категорию
     * льготы, если не нашли - отмечаем все записи, относящиеся к данному л/с как ошибку связывания. Надо иметь ввиду,
     * что на одной персоне может быть более одной льготы. В этом случае надо брать льготу с меньшим номером категории."
     *
     * Алгоритм:
     * Для каждой записи из data выделяем текущий ИНН(обязательно не NULL) и текущий номер паспорта.
     * Если этот ИНН еще не обрабатывался, то ищем среди benefits с таким же ИНН.
     * Если не нашли, то ищем с текущим номером паспорта.
     * В итоге получим некоторое подмножество benefits - theSameBenefits.
     * Если theSameBenefits не пусто, то выделим из data записи с текущим ИНН - список theSameMan.
     * Если же theSameBenefits пусто, то помечаем все записи benefits статусом RequestStatus.WRONG_ACCOUNT_NUMBER
     * и выходим.
     * В theSameMan найдем запись с наименьшим кодом привилегии  - min. Порядок сравнения: пытаемся преобразовать
     * строковые значения кодов привилегий в числа и сравнить как числа, иначе сравниваем как строки.
     * По полученному наименьшему коду привилегии(cmBenefitCode) ищем методом getOSZNPrivilegeCode код привилегии
     * для ОСЗН(osznBenefitCode) в таблице коррекций привилегий.
     * Если нашли код привилегии(osznBenefitCode != null), то проставляем во все записи в benefits: в поле PRIV_CAT
     * - osznBenefitCode, в поле ORD_FAM - порядок льготы из min(min.get("ORD_FAM"))
     * Если не нашли код привилегии, то все записи в theSameBenefits помечаются статусом RequestStatus.BENEFIT_NOT_FOUND.
     * Наконец все записи benefits, для которых код не был проставлен в RequestStatus.BENEFIT_NOT_FOUND помечаются
     * статусом RequestStatus.PROCESSED.
     *
     * @param calculationCenterId id ЦН
     * @param benefits Список benefit записей с одинаковым номером л/c
     * @param benefitData Список записей данных из ЦН
     */
    protected void processBenefitData(long calculationCenterId, List<Benefit> benefits, List<BenefitData> benefitData) {
        long osznId = benefits.get(0).getOrganizationId();
        List<String> processedINNs = Lists.newArrayList();
        for (BenefitData item : benefitData) {
            final String inn = item.getInn();
            final String passportNumber = item.getPassportNumber();
            if (!processedINNs.contains(inn)) {
                List<BenefitData> theSameMans;
                //предполагается что инн и паспорт уникален для группы льгот
                List<Benefit> theSameBenefits = findByINN(benefits, inn);
                if (!theSameBenefits.isEmpty()) {
                    theSameMans = getBenefitDataByINN(benefitData, inn);
                } else {
                    theSameBenefits = findByPassportNumber(benefits, passportNumber);
                    if (!theSameBenefits.isEmpty()) {
                        theSameMans = getBenefitDataByINN(benefitData, inn);
                    } else {
                        setStatus(benefits, RequestStatus.BENEFIT_OWNER_NOT_ASSOCIATED);
                        return;
                    }
                }
                if (theSameMans != null && !theSameMans.isEmpty()) {
                    BenefitData min = Collections.min(theSameMans, BENEFIT_DATA_COMPARATOR);
                    String calcCenterBenefitCode = min.getCode();
                    Long internalPrivilegeId = findInternalPrivilege(calcCenterBenefitCode, calculationCenterId);
                    if (internalPrivilegeId == null) {
                        log.warn("Couldn't find in corrections internal privilege object by calculation center's privilege code: '{}' "
                                + "and calculation center id: {}", calcCenterBenefitCode, calculationCenterId);

                        for (Benefit benefit : theSameBenefits) {
                            benefit.setStatus(RequestStatus.BENEFIT_NOT_FOUND);

                            RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                    RequestWarningStatus.PRIVILEGE_OBJECT_NOT_FOUND);
                            warning.addParameter(new RequestWarningParameter(0, calcCenterBenefitCode));
                            warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                            warningBean.save(warning);

                            logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                    webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                        }
                    } else {
                        String osznBenefitCode = findOSZNPrivilegeCode(internalPrivilegeId, osznId);
                        if (osznBenefitCode == null) {
                            log.warn("Couldn't find in corrections oszn's privilege code by internal privilege object id: {} "
                                    + "and oszn id: {}", internalPrivilegeId, osznId);

                            for (Benefit benefit : theSameBenefits) {
                                benefit.setStatus(RequestStatus.BENEFIT_NOT_FOUND);

                                RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                        RequestWarningStatus.PRIVILEGE_CODE_NOT_FOUND);
                                warning.addParameter(new RequestWarningParameter(0, "privilege", internalPrivilegeId));
                                warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                                warningBean.save(warning);

                                logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                        webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                            }
                        } else {
                            //set benefit code and ord fam into benefit entry.
                            Integer benefitCodeAsInt = null;
                            try {
                                benefitCodeAsInt = Integer.valueOf(osznBenefitCode);
                            } catch (NumberFormatException e) {
                                log.warn("Couldn't transform privilege code '{}' from correction to integer value. Oszn id: {}, "
                                        + "internal privilege id: {}", new Object[]{osznBenefitCode, osznId, internalPrivilegeId});

                                for (Benefit benefit : theSameBenefits) {
                                    RequestWarning warning = new RequestWarning(benefit.getId(), RequestFile.TYPE.BENEFIT,
                                            RequestWarningStatus.PRIVILEGE_CODE_INVALID);
                                    warning.addParameter(new RequestWarningParameter(0, osznBenefitCode));
                                    warning.addParameter(new RequestWarningParameter(1, "organization", osznId));
                                    warning.addParameter(new RequestWarningParameter(2, "privilege", internalPrivilegeId));
                                    warningBean.save(warning);

                                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                            webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                                }
                            }
                            Integer ordFamAsInt = null;
                            try {
                                ordFamAsInt = Integer.valueOf(min.getOrderFamily());
                            } catch (NumberFormatException e) {
                                log.warn("Couldn't transform ord fam value '{}' from calculation center to integer value.", min.getOrderFamily());

                                for (Benefit benefit : theSameBenefits) {
                                    RequestWarning warning = new RequestWarning(RequestFile.TYPE.BENEFIT, RequestWarningStatus.ORD_FAM_INVALID);
                                    warning.addParameter(new RequestWarningParameter(0, min.getOrderFamily()));
                                    warning.addParameter(new RequestWarningParameter(1, "organization", calculationCenterId));
                                    warningBean.save(warning);

                                    logBean.error(Module.NAME, getClass(), Benefit.class, benefit.getId(), EVENT.EDIT,
                                            webWarningRenderer.display(warning, localeBean.getSystemLocale()));
                                }
                            }

                            if (benefitCodeAsInt == null) {
                                setStatus(theSameBenefits, RequestStatus.INVALID_FORMAT);
                            } else {
                                for (Benefit benefit : theSameBenefits) {
                                    benefit.setField(BenefitDBF.PRIV_CAT, benefitCodeAsInt);
                                }
                            }

                            if (ordFamAsInt == null) {
                                setStatus(theSameBenefits, RequestStatus.INVALID_FORMAT);
                            } else {
                                for (Benefit benefit : theSameBenefits) {
                                    benefit.setField(BenefitDBF.ORD_FAM, ordFamAsInt);
                                }
                            }
                        }
                    }
                }
                processedINNs.add(inn);
            }
        }

        for (Benefit benefit : benefits) {
            if (benefit.getStatus() != RequestStatus.BENEFIT_NOT_FOUND && benefit.getStatus() != RequestStatus.INVALID_FORMAT) {
                benefit.setStatus(RequestStatus.PROCESSED);
            }
        }
    }

    protected void setStatus(List<Benefit> benefits, RequestStatus status) {
        for (Benefit benefit : benefits) {
            benefit.setStatus(status);
        }
    }

    protected Long findInternalPrivilege(String calculationCenterPrivilege, long calculationCenterId) {
        return privilegeCorrectionBean.findInternalPrivilege(calculationCenterPrivilege, calculationCenterId);
    }

    protected String findOSZNPrivilegeCode(Long internalPrivilege, long osznId) {
        return privilegeCorrectionBean.findPrivilegeCode(internalPrivilege, osznId);
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
