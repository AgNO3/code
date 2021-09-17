/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.sql.XAConnection;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration;
import eu.agno3.runtime.db.embedded.EmbeddedDBServer;
import eu.agno3.runtime.util.test.TestUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public class EmbeddedServerTest {

    private static Logger log = Logger.getLogger(EmbeddedServerTest.class);

    private static Semaphore wait = new Semaphore(0);
    private static ComponentContext componentContext;

    private static AbstractDerbyServer db1;
    private static AbstractDerbyServer db2;
    private static DerbyAuthConfiguration authConfig;


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


    @Reference ( target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)(dataSourceName=test))" )
    protected synchronized void setDBServer1 ( EmbeddedDBServer s ) {
        db1 = (AbstractDerbyServer) s;
    }


    protected synchronized void unsetDBServer1 ( EmbeddedDBServer s ) {
        db1 = null;
    }


    @Reference
    protected synchronized void setAuthConfig ( DerbyAuthConfiguration ac ) {
        authConfig = ac;
    }


    protected synchronized void unsetAuthConfig ( DerbyAuthConfiguration ac ) {
        authConfig = null;
    }


    @Reference ( target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)(dataSourceName=test2))" )
    protected synchronized void setDBServer2 ( EmbeddedDBServer s ) {
        db2 = (AbstractDerbyServer) s;
    }


    protected synchronized void unsetDBServer2 ( EmbeddedDBServer s ) {
        db2 = null;
    }


    @BeforeClass
    static public void waitForServices () {
        TestUtil.waitForServices(wait);
    }


    @Test
    public void testSetup () {
        assertNotNull(db1);
        assertNotNull(db2);
    }


    @Test
    public void testConnection () throws SQLException {
        log.info("Testing database connection");
        XAConnection conn = db1.createXADataSource(new Properties()).getXAConnection();

        try ( Connection c = conn.getConnection();
              PreparedStatement s = c.prepareStatement("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('DataDictionaryVersion')");
              ResultSet r = s.executeQuery() ) {

            assertTrue(r.next());
            assertNotNull(r.getObject(1));
        }

    }


    @Test
    public void testUnauthedConnection () throws SQLException {
        log.info("Testing database connection (unauthed)");
        XAConnection conn = null;
        try {
            conn = db2.createXADataSource(new Properties()).getXAConnection();
        }
        catch ( SQLException e ) {
            if ( "08004".equals(e.getSQLState()) ) {
                return;
            }
            throw e;
        }

        try ( Connection c = conn.getConnection();
              PreparedStatement s = c.prepareStatement("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('DataDictionaryVersion')");
              ResultSet r = s.executeQuery() ) {

        }
        catch ( SQLException e ) {
            if ( "08004".equals(e.getSQLState()) ) {
                return;
            }
        }

        fail("The server allowed an unauthenticated connection");

    }


    @Test
    public void testWrongPasswordConnection () throws SQLException {
        log.info("Testing database connection (unauthed)");
        XAConnection conn = null;
        try {
            conn = db2.createXADataSource(new Properties()).getXAConnection("readonly", "wrongpassword");
        }
        catch ( SQLException e ) {
            if ( "08004".equals(e.getSQLState()) ) {
                return;
            }
            throw e;
        }

        try ( Connection c = conn.getConnection();
              PreparedStatement s = c.prepareStatement("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('DataDictionaryVersion')");
              ResultSet r = s.executeQuery() ) {

        }
        catch ( SQLException e ) {
            if ( "08004".equals(e.getSQLState()) ) {
                return;
            }
            throw e;
        }

        fail("The server allowed an connection using a wrong password");

    }


    @Test
    public void testAuthedConnection () throws SQLException {
        log.info("Testing database connection (authed)");
        XAConnection conn = db2.createXADataSource(new Properties()).getXAConnection("readwrite", authConfig.getPassword("readwrite"));

        try ( Connection c = conn.getConnection();
              PreparedStatement s = c.prepareStatement("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY('DataDictionaryVersion')");
              ResultSet r = s.executeQuery() ) {

            assertTrue(r.next());
            assertNotNull(r.getObject(1));
        }

    }


    @Test
    public void testNoWriteDDL () throws SQLException {
        log.info("Testing DDL to readonly database");

        XAConnection conn = db2.createXADataSource(new Properties()).getXAConnection("readonly", authConfig.getPassword("readonly"));

        try ( Connection c = conn.getConnection();
              PreparedStatement s = c.prepareStatement("CREATE SCHEMA test") ) {

            assertTrue(s.execute());
        }
        catch ( SQLException e ) {
            if ( "25503".equals(e.getSQLState()) ) {
                return;
            }
            throw e;
        }

        fail("DDL to read-only conn was allowed");
    }


    @Test
    public void testNoWriteData () throws SQLException {
        log.info("Testing writing to readonly database");

        try ( Connection ddlConn = db2.createXADataSource(new Properties()).getXAConnection("readwrite", authConfig.getPassword("readwrite"))
                .getConnection() ) {

            try {
                ddlConn.prepareStatement("DROP TABLE test.writetest").execute();
            }
            catch ( SQLException e ) {}

            try {
                ddlConn.prepareStatement("DROP SCHEMA test RESTRICT").execute();
            }
            catch ( SQLException e ) {}

            ddlConn.prepareStatement("CREATE SCHEMA test").execute();
            ddlConn.prepareStatement("SET SCHEMA test").execute();
            ddlConn.prepareStatement("CREATE TABLE writeTest (pid INT, value VARCHAR(10), PRIMARY KEY (pid))").execute();
            ddlConn.prepareStatement("INSERT INTO writeTest ( pid, value ) VALUES (1, 'foo')").execute();
        }

        try ( Connection roConn = db2.createXADataSource(new Properties()).getXAConnection("readonly", authConfig.getPassword("readonly"))
                .getConnection() ) {
            roConn.prepareStatement("SET SCHEMA test").execute();

            try {
                roConn.prepareStatement("INSERT INTO writeTest ( pid, value ) VALUES (2, 'bar')").execute();
            }
            catch ( SQLException e ) {
                if ( "25502".equals(e.getSQLState()) ) {
                    return;
                }
                throw e;
            }

            fail("Wrote to read only database");
        }

    }
}
