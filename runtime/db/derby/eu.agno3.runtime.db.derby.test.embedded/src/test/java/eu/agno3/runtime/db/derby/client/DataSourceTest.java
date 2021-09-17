/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.DataSourceMetaData;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.util.test.TestUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public class DataSourceTest {

    static private Semaphore wait = new Semaphore(0);
    static private ComponentContext componentContext;

    static private XADataSource xads;
    static private DataSource ds;

    static private TransactionManager tm;
    static private DataSourceUtil util;


    /**
     * @return the componentContext
     */
    public static ComponentContext getComponentContext () {
        return componentContext;
    }


    @Activate
    protected synchronized void servicesSetUp ( ComponentContext context ) {
        componentContext = context;
        wait.release();
    }


    @Reference ( target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)(dataSourceName=test2)(user=readwrite))" )
    protected synchronized void setTestXADataSource ( XADataSource s ) {
        xads = s;
    }


    protected synchronized void unsetTestXADataSource ( XADataSource s ) {
        xads = null;
    }


    @Reference (
        target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)(dataSourceName=test2)(user=readwrite)(dataSourceType=xa))" )
    protected synchronized void setTestDataSource ( DataSource s ) {
        ds = s;
    }


    protected synchronized void unsetTestDataSource ( DataSource s ) {
        ds = null;
    }


    @Reference ( target = "(&(dataSourceName=test2)(user=readwrite))" )
    protected synchronized void setTestDataSourceUtil ( DataSourceUtil dsUtil ) {
        util = dsUtil;
    }


    protected synchronized void unsetTestDataSourceUtil ( DataSourceUtil dsUtil ) {
        util = null;
    }


    @Reference
    protected synchronized void setTransactionManager ( TransactionManager t ) {
        tm = t;
    }


    protected synchronized void unsetTransactionManager ( TransactionManager t ) {
        tm = null;
    }


    @BeforeClass
    static public void waitForServices () {
        TestUtil.waitForServices(wait);
    }


    @Test
    public void testSetup () {
        assertNotNull(xads);
        assertNotNull(ds);
        assertNotNull(tm);
    }


    @Test
    public void testUtilAndMetaData () {

        assertNotNull(util);

        DataSourceMetaData meta = util.createMetadata();
        assertNotNull(meta);

        assertEquals("APP", meta.getDefaultSchema());
        assertNull(meta.getDefaultCatalog());
    }


    @Test
    public void testXAConnection () throws SQLException {
        xads.getXAConnection().getConnection().close();
    }


    @Test
    public void testConnection () throws SQLException {
        ds.getConnection().close();
    }


    @Test
    public void testTransactionIntegration () throws NotSupportedException, SystemException, SQLException, IllegalStateException, SecurityException,
            HeuristicMixedException, HeuristicRollbackException, RollbackException {

        util.clearDatabase(null);
        util.ensureSchemaExists(null, "txTest");
        util.clearSchema(null, "txTest");

        try ( Connection ddlConn = ds.getConnection() ) {
            ddlConn.prepareStatement("CREATE TABLE \"txTest\".\"simple\" (test VARCHAR(10))").execute();
        }

        tm.begin();
        try ( Connection c = ds.getConnection() ) {
            util.setConnectionDefaultSchema(c, null, "txTest");
            c.prepareStatement("INSERT INTO \"simple\" (test) VALUES ('foo')").execute();
        }
        tm.rollback();

        tm.begin();
        try ( Connection c = ds.getConnection() ) {
            util.setConnectionDefaultSchema(c, null, "txTest");
            c.prepareStatement("INSERT INTO \"simple\" (test) VALUES ('bar')").execute();
        }
        tm.commit();

        try ( Connection testConn = ds.getConnection() ) {
            util.setConnectionDefaultSchema(testConn, null, "txTest");
            try ( ResultSet r1 = testConn.prepareStatement("SELECT COUNT(test) FROM \"simple\" WHERE test = 'foo'").executeQuery() ) {
                assertTrue(r1.next());
                assertEquals(0, r1.getInt(1));
            }

            try ( ResultSet r2 = testConn.prepareStatement("SELECT COUNT(test) FROM \"simple\" WHERE test = 'bar'").executeQuery() ) {
                assertTrue(r2.next());
                assertEquals(1, r2.getInt(1));
            }
        }

        util.clearSchema(null, "txTest");
        util.dropSchema(null, "txTest");
    }
}
