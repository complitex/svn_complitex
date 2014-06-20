package org.complitex.dictionary.service;

import junit.framework.TestCase;
import org.complitex.dictionary.EjbTestBeanLocator;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
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
        ConfigBean configBean = EjbTestBeanLocator.getBean("ConfigBean");
        configBean.getConfigs();
    }

    @Test
    public void testContainer() throws NamingException {
        ContainerTestBean testBean = EjbTestBeanLocator.getBean("ContainerTestBean");
        assertNotNull("ContainerTestBean not found", testBean);
        testBean.executeInOneTransaction();
        testBean.executeInDifferentTransaction();
    }


    @Test
    public void testOneMethodInsert() throws NamingException {
        testBean.testInsertSimple(System.currentTimeMillis() + "");
    }

    @Test
    public void testOneMethodSelect() throws NamingException {
        testBean.testSelectSimple(System.currentTimeMillis() + "");
    }

    @Test
    public void testOneMethodSelectEx() throws NamingException {
        try {
            testBean.testSelectSimpleEx(System.currentTimeMillis() + "");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testSelectSimpleNonSupported() throws NamingException {
        testBean.testSelectSimpleNonSupported(System.currentTimeMillis() + "");
    }

    @Test
    public void testSelectSimpleNever() throws NamingException {
        testBean.testSelectSimpleNever(System.currentTimeMillis() + "");
    }

    @Test
    public void testSelectSimpleNeverEx() throws NamingException {
        try {
            testBean.testSelectSimpleNeverEx(System.currentTimeMillis() + "");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testInsertTwoEx(){
        try {
            testBean.testInsertTwoEx(System.currentTimeMillis() + "");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testInsertTwoNeverEx(){
        try {
            testBean.testInsertTwoNeverEx(System.currentTimeMillis() + "");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testUserInsertTwoEx(){
        try {
            testUserBean.testUserInsertTwoEx(System.currentTimeMillis() + "");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }

    @Test
    public void testUserInsertTwoExTr(){
        try {
            testUserBean.testUserInsertTwoExTr(System.currentTimeMillis() + "");
            TestCase.assertTrue("Error", false);
        } catch (Exception e) {
            //
        }
    }
}
