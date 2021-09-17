/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    static private DataSourceUtil dsUtil;

    static private TransactionManager tm;


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


    @Reference ( target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDriver)(dataSourceName=test)(user=readwrite))" )
    protected synchronized void setTestXADataSource ( XADataSource s ) {
        xads = s;
    }


    protected synchronized void unsetTestXADataSource ( XADataSource s ) {
        xads = null;
    }


    @Reference ( target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDriver)(dataSourceName=test)(user=readwrite)(dataSourceType=xa))" )
    protected synchronized void setTestDataSource ( DataSource s ) {
        ds = s;
    }


    protected synchronized void unsetTestDataSource ( DataSource s ) {
        ds = null;
    }


    @Reference ( target = "(&(dataSourceName=test)(user=readwrite))" )
    protected synchronized void setDataSourceUtil ( DataSourceUtil util ) {
        dsUtil = util;
    }


    protected synchronized void unsetDataSourceUtil ( DataSourceUtil util ) {
        dsUtil = null;
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


    // @AfterClass
    // static public void cleanup () throws IOException {
    // File testDir = new File("/tmp/derby-test-networked/");
    // if ( testDir.exists() ) {
    // FileUtils.deleteDirectory(testDir);
    // }
    // }

    @Test
    public void testSetup () {
        assertNotNull(xads);
        assertNotNull(ds);
        assertNotNull(tm);
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

        dsUtil.clearDatabase(null);
        dsUtil.ensureSchemaExists(null, "txTest");
        dsUtil.clearSchema(null, "txTest");

        try ( Connection ddlConn = ds.getConnection() ) {

            ddlConn.prepareStatement("CREATE TABLE \"txTest\".\"simple\" (test VARCHAR(10))").execute();
        }

        tm.begin();
        try ( Connection c = ds.getConnection() ) {
            dsUtil.setConnectionDefaultSchema(c, null, "txTest");
            c.prepareStatement("INSERT INTO \"simple\" (test) VALUES ('foo')").execute();
        }
        tm.rollback();

        tm.begin();
        try ( Connection c = ds.getConnection() ) {
            dsUtil.setConnectionDefaultSchema(c, null, "txTest");
            c.prepareStatement("INSERT INTO \"simple\" (test) VALUES ('bar')").execute();
        }
        tm.commit();

        try ( Connection testConn = ds.getConnection() ) {
            dsUtil.setConnectionDefaultSchema(testConn, null, "txTest");
            try ( ResultSet r1 = testConn.prepareStatement("SELECT COUNT(test) FROM \"simple\" WHERE test = 'foo'").executeQuery() ) {
                assertTrue(r1.next());
                assertEquals(0, r1.getInt(1));
            }

            try ( ResultSet r2 = testConn.prepareStatement("SELECT COUNT(test) FROM \"simple\" WHERE test = 'bar'").executeQuery() ) {
                assertTrue(r2.next());
                assertEquals(1, r2.getInt(1));
            }
        }

        dsUtil.clearSchema(null, "txTest");
        dsUtil.dropSchema(null, "txTest");

    }
}
