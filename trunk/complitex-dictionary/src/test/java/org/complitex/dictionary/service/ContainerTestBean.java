package org.complitex.dictionary.service;

import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Pavel Sknar
 */
@Stateless
public class ContainerTestBean extends AbstractBean {
    @EJB
    private TestBean testBean;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void executeInOneTransaction() {
        testBean.deleteInNewTransaction(null);

        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test1"));
        long id = testBean.saveInCurrentTransaction("test1");
        assertTrue("id <= 0", id > 0);
        assertTrue("Value is not exists", testBean.isExistInCurrentTransaction("test1"));
        testBean.deleteInCurrentTransaction(id);
        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test1"));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void executeInDifferentTransaction() {
        testBean.deleteInNewTransaction(null);

        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test2"));
        long id = testBean.saveInNewTransaction("test2");
        assertTrue("id <= 0", id > 0);
        assertFalse("Value is exists in current transaction", testBean.isExistInCurrentTransaction("test2"));
        assertTrue("Value is not exists in new transaction", testBean.isExistInNewTransaction("test2"));
        testBean.deleteInNewTransaction(id);
        assertFalse("Value is exists", testBean.isExistInNewTransaction("test2"));
    }

    @Transactional
    public void executeInOneTransactionWithTransactional() {
        testBean.deleteInNewTransaction(null);

        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test3"));
        long id = testBean.saveInCurrentTransaction("test3");
        assertTrue("id <= 0", id > 0);
        long id2 = testBean.testSaveTransactional("test4");
        assertTrue("id <= 0", id > 0);
        assertTrue("id <= 0", id2 > 0);
        assertTrue("Value is not exists", testBean.isExistInCurrentTransaction("test3"));
        assertTrue("Value is not exists", testBean.isExistInCurrentTransaction("test4"));
        testBean.deleteInCurrentTransaction(id);
        testBean.deleteInCurrentTransaction(id2);
        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test3"));
        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test4"));
    }
}
