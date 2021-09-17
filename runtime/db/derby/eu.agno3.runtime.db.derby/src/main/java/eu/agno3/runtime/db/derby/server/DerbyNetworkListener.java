/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.05.2014 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration;
import eu.agno3.runtime.util.log.LogWriter;


/**
 * @author mbechler
 * 
 */
@Component (
    immediate = true,
    service = DerbyNetworkListener.class,
    configurationPid = DerbyNetworkListener.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DerbyNetworkListener {

    /**
     * Configuration PID
     */
    public static final String PID = "db.server.derby.net"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DerbyNetworkListener.class);
    private DerbyAuthConfiguration authConfig;
    private NetworkServerControl derbyServer;

    private Dictionary<String, Object> servProps;


    @Activate
    protected void activate ( ComponentContext ctx ) throws DatabaseConfigurationException {
        this.servProps = ctx.getProperties();
        this.startNetworkServer();
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.stopNetworkServer();
    }


    @Reference ( updated = "updatedAuthConfiguration" )
    protected synchronized void setAuthConfiguration ( DerbyAuthConfiguration config ) {
        this.authConfig = config;
    }


    protected synchronized void updatedAuthConfiguration ( DerbyAuthConfiguration config ) {
        // ignore for new
    }


    protected synchronized void unsetAuthConfiguration ( DerbyAuthConfiguration config ) {
        if ( this.authConfig == config ) {
            this.authConfig = null;
        }
    }


    @Reference
    protected void setDerbyGlobalConfig ( DerbyGlobalConfig cfg ) {
        // dependency only
    }


    protected void unsetDerbyGlobalConfig ( DerbyGlobalConfig cfg ) {
        // dependency only
    }


    /**
     * @return
     */
    private DerbyAuthConfiguration getAuthConfig () {
        return this.authConfig;
    }


    /**
     * @throws DatabaseConfigurationException
     */
    private void startNetworkServer () throws DatabaseConfigurationException {
        log.info("Starting Derby Network Server"); //$NON-NLS-1$

        InetAddress bindAddress = InetAddress.getLoopbackAddress();
        String bindSpec = this.getServerName();
        if ( bindSpec != null ) {
            try {
                bindAddress = InetAddress.getByName(bindSpec);
            }
            catch ( UnknownHostException e ) {
                throw new DatabaseConfigurationException("Invalid bind address:", e); //$NON-NLS-1$
            }
        }

        int port = 1527;
        String portSpec = this.getPortNumber();
        if ( portSpec != null ) {
            port = Integer.parseInt(portSpec);
        }

        try {
            this.derbyServer = new NetworkServerControl(bindAddress, port, this.getAuthConfig().getAdminUser(), this.getAuthConfig()
                    .getAdminPassword());

            this.derbyServer.start(LogWriter.createWriter(log, Level.DEBUG));

        }
        catch ( Exception e ) {
            this.derbyServer = null;
            log.error("Failed to start derby network control server:", e); //$NON-NLS-1$
        }
    }


    private String getPortNumber () {
        return (String) this.servProps.get(DataSourceFactory.JDBC_PORT_NUMBER);
    }


    private String getServerName () {
        return (String) this.servProps.get(DataSourceFactory.JDBC_SERVER_NAME);
    }


    /**
     * @param props
     */
    public void applySettings ( Dictionary<String, Object> props ) {
        props.put(DataSourceFactory.JDBC_SERVER_NAME, this.getServerName());

        if ( this.getPortNumber() != null ) {
            props.put(DataSourceFactory.JDBC_PORT_NUMBER, this.getPortNumber());
        }
    }


    /**
     * 
     * @param props
     */
    public void applySettings ( Properties props ) {
        props.put(DataSourceFactory.JDBC_SERVER_NAME, this.getServerName());

        if ( this.getPortNumber() != null ) {
            props.put(DataSourceFactory.JDBC_PORT_NUMBER, this.getPortNumber());
        }
    }


    /**
     * 
     * @throws SQLException
     */
    private void stopNetworkServer () {
        log.info("Stopping Derby Network Server"); //$NON-NLS-1$

        try {
            this.derbyServer.shutdown();
        }
        catch ( Exception e ) {
            log.error("Failed to stop derby server:", e); //$NON-NLS-1$
        }

    }
}
