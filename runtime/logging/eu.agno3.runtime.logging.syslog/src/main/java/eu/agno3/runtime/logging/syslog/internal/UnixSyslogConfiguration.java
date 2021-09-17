/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.logging.syslog.internal;


import java.util.Dictionary;

import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogRuntimeException;
import org.graylog2.syslog4j.impl.AbstractSyslogConfig;
import org.graylog2.syslog4j.util.SyslogUtility;


/**
 * @author mbechler
 * 
 */
public class UnixSyslogConfiguration extends AbstractSyslogConfig {

    /**
     * 
     */
    private static final long serialVersionUID = -1186553053627234654L;
    private String socketPath;


    /**
     * @param socketPath
     * @param facility
     * 
     */
    public UnixSyslogConfiguration ( String socketPath, int facility ) {
        this.socketPath = socketPath;
        this.facility = facility;
        this.setSendLocalName(false);
        this.setIdent("java"); //$NON-NLS-1$
    }


    /**
     * @return the socket path
     */
    public String getSocketPath () {
        return this.socketPath;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslogConfig#getSyslogClass()
     */
    @Override
    public Class getSyslogClass () {
        return UnixSyslogImpl.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslogConfigIF#getMaxQueueSize()
     */
    @Override
    public int getMaxQueueSize () {
        return -1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslogConfigIF#setMaxQueueSize(int)
     */
    @Override
    public void setMaxQueueSize ( int arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.SyslogConfigIF#getHost()
     */
    @Override
    public String getHost () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.SyslogConfigIF#getPort()
     */
    @Override
    public int getPort () {
        return -1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.SyslogConfigIF#setHost(java.lang.String)
     */
    @Override
    public void setHost ( String arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.SyslogConfigIF#setPort(int)
     */
    @Override
    public void setPort ( int arg0 ) throws SyslogRuntimeException {
        throw new UnsupportedOperationException();
    }


    /**
     * @param properties
     * @return a configuration based on the given properties
     */
    public static SyslogConfigIF createConfig ( Dictionary<String, Object> properties ) {
        String socketSpec = (String) properties.get(SyslogConfiguration.SOCKET);
        String socketPath = SyslogConfiguration.SOCKET_DEFAULT;
        if ( socketSpec != null ) {
            socketPath = socketSpec;
        }

        String facilitySpec = (String) properties.get(SyslogConfiguration.FACILITY);
        int facility = FACILITY_DAEMON;

        if ( facilitySpec != null ) {
            facility = SyslogUtility.getFacility(facilitySpec);
        }
        UnixSyslogConfiguration config = new UnixSyslogConfiguration(socketPath, facility);

        String nameSpec = (String) properties.get(SyslogConfiguration.NAME);

        if ( nameSpec != null ) {
            config.setIdent(nameSpec);
        }

        return config;
    }

}
