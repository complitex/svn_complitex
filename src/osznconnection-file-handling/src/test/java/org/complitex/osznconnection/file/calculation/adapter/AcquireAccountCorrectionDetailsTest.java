/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountCorrectionDetail;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.Status;

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

            @Override
            protected SqlSession openSession() {
                return sqlSessionFactory.openSession(false);
            }

            @Override
            public List<AccountCorrectionDetail> acquireAccountCorrectionDetails(Payment payment) {
                List<AccountCorrectionDetail> accountCorrectionDetails = null;
                SqlSession session = null;
                try {
                    session = openSession();

                    Map<String, Object> params = Maps.newHashMap();
                    String districtName = "ЦЕНТРАЛЬНЫЙ";
                    params.put("pDistrName", districtName);
                    params.put("pStSortName", "УЛ");
                    params.put("pStreetName", "ФРАНТИШЕКА КРАЛА");
                    params.put("pHouseNum", "25А");
                    params.put("pHousePart", "");
                    params.put("pFlatNum", "40");
                    params.put("dat1", new Date());

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
        };
        System.out.println(adapter.acquireAccountCorrectionDetails(newPayment()));
    }

    private static Payment newPayment() {
        Payment p = new Payment();
        return p;
    }
}
