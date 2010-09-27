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

/**
 *
 * @author Artem
 */
public class ProcessBenefitTest {

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
            protected Date getDat1(String ownNumSr, String accountNumber) {
                return new Date();
            }

            @Override
            protected boolean existsWithINN(Benefit benefit, String inn) {
                return true;
            }

            @Override
            protected boolean existsWithPassportNumber(Benefit benefit, String passportNumber) {
                return false;
            }

            @Override
            protected void setWrongAccountNumber(String accountNumber) {
                System.out.println("setWrongAccountNumber, an : " + accountNumber);
            }

            @Override
            protected String getOSZNPrivilegeCode(String calculationCenterPrivilege, long calculationCenterId, long osznId) {
                System.out.println("calculationCenterPrivilege : " + calculationCenterPrivilege);
                return null;
            }

            @Override
            protected void updateBenefit(String inn, String passportNumber, Benefit benefit) {
                System.out.println("updateBenefit, inn :  " + inn + ", passport : " + passportNumber);
            }
        };
        Benefit b = new Benefit();
        b.setAccountNumber("1000001108");
        b.setOrganizationId(1L);
        adapter.processBenefit(b, 2);
        System.out.println("Status : " + b.getStatus());
    }
}
