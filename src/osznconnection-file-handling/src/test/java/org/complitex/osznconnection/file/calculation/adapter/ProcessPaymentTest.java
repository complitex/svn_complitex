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
public class ProcessPaymentTest {

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
        Payment p = new Payment();
        p.setAccountNumber("1000000015");
        p.setField(PaymentDBF.DAT1, new Date());
        adapter.processPayment(p);
        System.out.println("Status : " + p.getStatus() + ", FROG : " + p.getField(PaymentDBF.FROG) + ", FL_PAY : " + p.getField(PaymentDBF.FL_PAY)
                + ", NM_PAY : "
                + p.getField(PaymentDBF.NM_PAY) + ", DEBT : " + p.getField(PaymentDBF.DEBT) + ", NORM_F_1 : " + p.getField(PaymentDBF.NORM_F_1)
                + ", NUMB : " + p.getField(PaymentDBF.NUMB) + ", MARK : " + p.getField(PaymentDBF.MARK));
    }
}
