/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh;


/**
 * @author mbechler
 * 
 */
public final class SSHServiceConfiguration {

    private SSHServiceConfiguration () {}

    /**
     * PID of SSH service
     */
    public static final String PID = "console.ssh"; //$NON-NLS-1$

    /**
     * PID of SSH service hostkey config
     */
    public static final String PID_HOSTKEY = "console.ssh.hostkey"; //$NON-NLS-1$

    /**
     * PID of SSH service simple auth config
     */
    public static final String PID_SIMPLE_AUTH = "console.ssh.auth.simple"; //$NON-NLS-1$

    /**
     * Port number to bind to
     */
    public static final String PORT = "port"; //$NON-NLS-1$

    /**
     * Address to bind to
     */
    public static final String BIND = "bind"; //$NON-NLS-1$

    /**
     * Whether to enable SSH service
     */
    public static final String ENABLE = "enable"; //$NON-NLS-1$

    /**
     * Password for administrative user
     */
    public static final String ADMIN_PASSWORD = "admin.password"; //$NON-NLS-1$

    /**
     * Username of administrative user
     */
    public static final String ADMIN_USER = "admin.user"; //$NON-NLS-1$

    /**
     * Defaukt Username of administrative user
     */
    public static final String ADMIN_USERNAME_DEFAULT = "admin"; //$NON-NLS-1$

    /**
     * Hostkey to use, "dummy" for internal static one (do _never_ use in production)
     */
    public static final String HOSTKEY = "hostkey"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String NUM_THREADS = "numThreads"; //$NON-NLS-1$

}
