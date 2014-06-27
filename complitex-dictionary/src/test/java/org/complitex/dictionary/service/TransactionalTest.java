package org.complitex.dictionary.service;

import junit.framework.TestCase;
import org.complitex.dictionary.EjbTestBeanLocator;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingException;

import static junit.framework.TestCase.assertNotNull;

/**
 * @author Pavel Sknar
 */
public class TransactionalTest {

    private TestBean testBean;
    private TestUserBean testUserBean;

    @Before
    public void createContainer() {
        testBean = EjbTestBeanLocator.getBean("TestBean");
        testUserBean = EjbTestBeanLocator.getBean("TestUserBean");
    }

    @Test
    public void testInOneTransaction() throws NamingException {
        ContainerTestBean testBean = EjbTestBeanLocator.getBean("ContainerTestBean");
        assertNotNull("ContainerTestBean not found", testBean);
        testBean.executeInOneTransaction();
    }

    @Test
    public void testInDifferentTransaction() throws NamingException {
        ContainerTestBean testBean = EjbTestBeanLocator.getBean("ContainerTestBean");
        assertNotNull("ContainerTestBean not found", testBean);
        testBean.executeInDifferentTransaction();
    }

    @Test
    public void testInOneTransactionWithTransactional() throws NamingException {
        ContainerTestBean testBean = EjbTestBeanLocator.getBean("ContainerTestBean");
        assertNotNull("ContainerTestBean not found", testBean);
        testBean.executeInOneTransactionWithTransactional();
    }

    @Test
    public void testOneMethodInsert() throws NamingException {
        testBean.testInsertSimple(System.currentTimeMillis() + "testOneMethodInsert");
    }

    @Test
    public void testOneMethodSelect() throws NamingException {
        testBean.testSelectSimple(System.currentTimeMillis() + "testOneMethodSelect");
    }

    @Test
    public void testOneMethodSelectEx() throws NamingException {
        try {
            testBean.testSelectSimpleEx(System.currentTimeMillis() + "testOneMethodSelectEx");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testSelectSimpleNonSupported() throws NamingException {
        testBean.testSelectSimpleNonSupported(System.currentTimeMillis() + "testSelectSimpleNonSupported");
    }

    @Test
    public void testSelectSimpleNever() throws NamingException {
        testBean.testSelectSimpleNever(System.currentTimeMillis() + "testSelectSimpleNever");
    }

    @Test
    public void testSelectSimpleNeverEx() throws NamingException {
        try {
            testBean.testSelectSimpleNeverEx(System.currentTimeMillis() + "testSelectSimpleNeverEx");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testInsertTwoEx(){
        try {
            testBean.testInsertTwoEx(System.currentTimeMillis() + "testInsertTwoEx");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testInsertTwoNeverEx(){
        try {
            testBean.testInsertTwoNeverEx(System.currentTimeMillis() + "testInsertTwoNeverEx");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testUserInsertTwoEx(){
        try {
            testUserBean.testUserInsertTwoEx(System.currentTimeMillis() + "testUserInsertTwoEx");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testUserInsertTwoExTr(){
        try {
            testUserBean.testUserInsertTwoExTr(System.currentTimeMillis() + "testUserInsertTwoExTr");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }
}
