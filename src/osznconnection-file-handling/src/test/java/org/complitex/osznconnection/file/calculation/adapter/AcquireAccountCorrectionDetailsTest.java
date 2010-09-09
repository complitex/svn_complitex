/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;

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
        };
        System.out.println(adapter.acquireAccountCorrectionDetails(newPayment()));
    }

    private static Payment newPayment() {
        Payment p = new Payment();
        p.setField(PaymentDBF.DAT1, new Date());
        p.setOutgoingApartment("40");
        p.setOutgoingBuildingCorp("");
        p.setOutgoingBuildingNumber("25А");
        p.setOutgoingStreet("ФРАНТИШЕКА КРАЛА");
        p.setOutgoingStreetType("УЛ");
        return p;
    }
}
