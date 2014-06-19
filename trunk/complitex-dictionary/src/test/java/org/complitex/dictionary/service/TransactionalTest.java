package org.complitex.dictionary.service;

import com.beust.jcommander.internal.Maps;
import org.junit.Before;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import java.io.File;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Pavel Sknar
 */
public class TransactionalTest {

    private static Context ctx;

    @Before
    public void createContainer() throws NamingException {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(EJBContainer.MODULES, new File[]{
                new File("complitex-dictionary/target/classes"),
                new File("complitex-dictionary/target/test-classes")
        });
        properties.put("org.glassfish.ejb.embedded.glassfish.installation.root",
                "complitex-dictionary/src/test/glassfish");
        EJBContainer container = EJBContainer.createEJBContainer(properties);
        ctx = container.getContext();

//        final InitialContext ctx = new InitialContext();
//        final CommandRunner runner = (CommandRunner) ctx.lookup("org.glassfish.embeddable.CommandRunner");
//        runner.run("create-jdbc-connection-pool", "datasourceclassname=com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
//                "restype=javax.sql.ConnectionPoolDataSource",
//                "property=url=jdbc\\:mysql\\://localhost\\:3306/eirc:user=eirc:password=eirc:characterResultSets=utf8:characterEncoding=utf8:useUnicode=true:connectionCollation=utf8_unicode_ci:autoReconnect=true",
//                "eircPool");
//        runner.run("create-jdbc-resource", "connectionpoolid eircPool", "jdbc/eircResource");
    }

    @Test
    public void create() throws NamingException {
        ContainerTestBean testBean = (ContainerTestBean)ctx.lookup("java:global/ejb-app/test-classes/ContainerTestBean");
        assertNotNull("ContainerTestBean not found", testBean);
        testBean.executeInOneTransaction();
        testBean.executeInDifferentTransaction();

        /*TestBean testBean = (TestBean)ctx.lookup("java:global/ejb-app/test-classes/TestBean");
        assertNotNull("TestBean not found", testBean);
        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test1"));
        long id = testBean.saveInNewTransaction("test1");
        assertTrue("id <= 0", id > 0);
        assertTrue("Value is not exists", testBean.isExistInCurrentTransaction("test1"));
        testBean.deleteInCurrentTransaction(id);
        assertFalse("Value is exists", testBean.isExistInCurrentTransaction("test1"));
        */
    }
}
