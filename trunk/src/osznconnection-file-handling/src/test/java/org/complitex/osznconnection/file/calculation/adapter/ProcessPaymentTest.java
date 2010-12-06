/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;

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
            protected SqlSession sqlSession() {
                return sqlSessionFactory.openSession(false);
            }

            @Override
            protected String getOSZNOwnershipCode(String calculationCenterOwnership, long calculationCenterId, long osznId) {
                System.out.println("Original OWN_FRM : " + calculationCenterOwnership);
                return "12";
            }

            @Override
            protected Integer getCODE2_1(Double T11_CS_UNI, long organizationId) {
                System.out.println("T11_CS_UNI : " + T11_CS_UNI);
                return 0;
            }
        };
        Payment p = new Payment();
        Benefit b = new Benefit();
        p.setAccountNumber("1000460875");
        p.setOrganizationId(1L);
        p.setField(PaymentDBF.DAT1, new Date());
        try {
            adapter.processPaymentAndBenefit(p, b, 2);
        } catch (AccountNotFoundException e) {
            System.out.println("Account not found");
        }
        System.out.println("Status : " + p.getStatus() + ", FROG : " + p.getField(PaymentDBF.FROG) + ", FL_PAY : " + p.getField(PaymentDBF.FL_PAY)
                + ", NM_PAY : " + p.getField(PaymentDBF.NM_PAY) + ", DEBT : " + p.getField(PaymentDBF.DEBT) + ", NORM_F_1 : "
                + p.getField(PaymentDBF.NORM_F_1) + ", NUMB : " + p.getField(PaymentDBF.NUMB) + ", CODE2_1 : " + p.getField(PaymentDBF.CODE2_1)
                + ", MARK : " + p.getField(PaymentDBF.MARK) + ", HOSTEL : " + b.getField(BenefitDBF.HOSTEL)
                + ", OWN_FRM : " + b.getField(BenefitDBF.OWN_FRM));
    }
}
