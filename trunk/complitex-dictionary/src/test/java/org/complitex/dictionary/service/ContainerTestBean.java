package org.complitex.dictionary.service;

import javax.ejb.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Pavel Sknar
 */
@Stateless
public class ContainerTestBean {
    @EJB
    private TestBean testBean;

    //@Transactional
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
}
