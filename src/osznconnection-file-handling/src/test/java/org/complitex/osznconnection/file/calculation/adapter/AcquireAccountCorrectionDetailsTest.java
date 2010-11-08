/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.collect.Maps;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
public class AcquireAccountCorrectionDetailsTest {

    private static SqlSessionFactory sqlSessionFactory;

    private static void init() {
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader("Configuration-test.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "remote");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        init();

        ICalculationCenterAdapter adapter = new DefaultCalculationCenterAdapter() {

//            @Override
            protected SqlSession openSession() {
                return sqlSessionFactory.openSession(false);
            }

            @Override
            public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment) {
                List<AccountDetail> accountCorrectionDetails = null;
                SqlSession session = null;
                try {
                    session = openSession();

                    Map<String, Object> params = Maps.newHashMap();
                    String districtName = "ДЗЕРЖИНСКИЙ";
                    params.put("pDistrName", districtName);
                    params.put("pStSortName", "УЛ");
                    params.put("pStreetName", "АХСАРОВА");
                    params.put("pHouseNum", "23");
                    params.put("pHousePart", "");
                    params.put("pFlatNum", "240");
                    params.put("dat1", new Date());

                    try {
                        session.selectOne(MAPPING_NAMESPACE + ".acquireAccountCorrectionDetails", params);
//                if (processAccountCorrectionDetailsResult(payment, String.valueOf(resultCode))) {
                        accountCorrectionDetails = (List<AccountDetail>) params.get("details");
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
//                }
                    } catch (Exception e) {
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
        };
//        System.out.println(adapter.acquireAccountCorrectionDetails(newPayment()));
    }

    private static Payment newPayment() {
        Payment p = new Payment();
        return p;
    }
}
