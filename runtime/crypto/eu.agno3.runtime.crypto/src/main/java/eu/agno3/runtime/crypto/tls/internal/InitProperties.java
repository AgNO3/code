/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true )
public class InitProperties {

    private static final String JDK_TLS_EPHEMERAL_DH_KEY_SIZE = "jdk.tls.ephemeralDHKeySize"; //$NON-NLS-1$
    private static final String JDK_TLS_REJECT_CLIENT_INITIATED_RENEGOTIATION = "jdk.tls.rejectClientInitiatedRenegotiation"; //$NON-NLS-1$
    private static final String SUN_SECURITY_SSL_ALLOW_LEGACY_HELLO_MESSAGES = "sun.security.ssl.allowLegacyHelloMessages"; //$NON-NLS-1$
    private static final String SUN_SECURITY_SSL_ALLOW_UNSAFE_RENEGOTIATION = "sun.security.ssl.allowUnsafeRenegotiation"; //$NON-NLS-1$


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        if ( System.getProperty(SUN_SECURITY_SSL_ALLOW_UNSAFE_RENEGOTIATION) == null ) {
            System.setProperty(SUN_SECURITY_SSL_ALLOW_UNSAFE_RENEGOTIATION, Boolean.FALSE.toString()); // $NON-NLS-1$
        }
        if ( System.getProperty(SUN_SECURITY_SSL_ALLOW_LEGACY_HELLO_MESSAGES) == null ) {
            System.setProperty(SUN_SECURITY_SSL_ALLOW_LEGACY_HELLO_MESSAGES, Boolean.FALSE.toString()); // $NON-NLS-1$
        }
        if ( System.getProperty(JDK_TLS_REJECT_CLIENT_INITIATED_RENEGOTIATION) == null ) {
            System.setProperty(JDK_TLS_REJECT_CLIENT_INITIATED_RENEGOTIATION, Boolean.TRUE.toString()); // $NON-NLS-1$
        }
        if ( System.getProperty(JDK_TLS_EPHEMERAL_DH_KEY_SIZE) == null ) {
            System.setProperty(JDK_TLS_EPHEMERAL_DH_KEY_SIZE, "2048"); //$NON-NLS-1$
        }
    }
}
