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
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
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

            @Override
            protected String getOSZNOwnershipCode(String calculationCenterOwnership, long calculationCenterId, long osznId) {
                System.out.println("Original OWN_FRM : " + calculationCenterOwnership);
                return "12";
            }

            @Override
            protected Integer getCODE2_1(Double T11_CS_UNI) {
                System.out.println("T11_CS_UNI : " + T11_CS_UNI);
                return 0;
            }
        };
        Payment p = new Payment();
        Benefit b = new Benefit();
        p.setAccountNumber("1000000015");
        p.setOrganizationId(1L);
        p.setField(PaymentDBF.DAT1, new Date());
        adapter.processPaymentAndBenefit(p, b, 2);
        System.out.println("Status : " + p.getStatus() + ", FROG : " + p.getField(PaymentDBF.FROG) + ", FL_PAY : " + p.getField(PaymentDBF.FL_PAY)
                + ", NM_PAY : " + p.getField(PaymentDBF.NM_PAY) + ", DEBT : " + p.getField(PaymentDBF.DEBT) + ", NORM_F_1 : "
                + p.getField(PaymentDBF.NORM_F_1) + ", NUMB : " + p.getField(PaymentDBF.NUMB) + ", CODE2_1 : " + p.getField(PaymentDBF.CODE2_1)
                + ", MARK : " + p.getField(PaymentDBF.MARK) + ", HOSTEL : " + b.getField(BenefitDBF.HOSTEL)
                + ", OWN_FRM : " + b.getField(BenefitDBF.OWN_FRM));
    }
}
