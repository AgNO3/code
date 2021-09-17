/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public final class BootstrapConstants {

    public static final String ORCHSERVER_USER = "orchserver"; //$NON-NLS-1$
    public static final String RSA = "RSA"; //$NON-NLS-1$
    public static final String CA_KEY_ALIAS = "ca"; //$NON-NLS-1$
    public static final String AGENT_KEY_ALIAS = "agent"; //$NON-NLS-1$
    public static final String SERVER_KEY_ALIAS = "server"; //$NON-NLS-1$
    public static final String WEB_KEY_ALIAS = "web"; //$NON-NLS-1$

    public static final String INTERNAL_CA_KEYSTORE = "internalCA"; //$NON-NLS-1$
    public static final String AGENT_KEYSTORE = "orchagent"; //$NON-NLS-1$
    public static final String SERVER_KEYSTORE = "orchserver"; //$NON-NLS-1$
    public static final String WEB_KEYSTORE = "web"; //$NON-NLS-1$

    public static final String INTERNAL_TRUSTSTORE = "internal"; //$NON-NLS-1$

    public static final String DEFAULT_OR_LOCAL_HOSTNAME_VERIFIER = "defaultOrLocalhost"; //$NON-NLS-1$

    public static final String WEB_KEYSTORE_TRUSTSTORE = "keyStore:web"; //$NON-NLS-1$


    /**
     * 
     */
    private BootstrapConstants () {}
}
