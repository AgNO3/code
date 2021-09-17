/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import eu.agno3.runtime.jmx.CredentialChecker;


/**
 * @author mbechler
 *
 */
public class JMXSSLAuthenticator extends JMXTCPAuthenticator {

    /**
     * @param allowAnonymous
     * @param cc
     */
    public JMXSSLAuthenticator ( boolean allowAnonymous, CredentialChecker cc ) {
        super(allowAnonymous, cc);
    }

}
