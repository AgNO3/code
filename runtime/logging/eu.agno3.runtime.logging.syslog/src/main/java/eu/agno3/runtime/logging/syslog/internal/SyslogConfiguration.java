/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.logging.syslog.internal;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
public final class SyslogConfiguration {

    /**
     * 
     */
    private SyslogConfiguration () {}

    /**
     * Configuration PID
     */
    public static final String PID = "log.syslog"; //$NON-NLS-1$
    public static final String JORNAL_PID = "log.journal"; //$NON-NLS-1$

    public static final String SOCKET = "socket"; //$NON-NLS-1$
    public static final String SOCKET_DEFAULT = "/dev/log"; //$NON-NLS-1$

    public static final String FACILITY = "facility"; //$NON-NLS-1$
    public static final String NAME = "name"; //$NON-NLS-1$
    public static final String CHARSET = "charset"; //$NON-NLS-1$

}
