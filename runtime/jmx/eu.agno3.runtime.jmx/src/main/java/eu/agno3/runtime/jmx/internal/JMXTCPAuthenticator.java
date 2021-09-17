/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jmx.CredentialChecker;


/**
 * @author mbechler
 *
 */
public class JMXTCPAuthenticator extends AbstractJMXAuthenticator {

    private static final Logger log = Logger.getLogger(JMXTCPAuthenticator.class);
    private final boolean allowAnonymous;
    private CredentialChecker credentialChecker;


    /**
     * 
     */
    public JMXTCPAuthenticator () {
        this(true, null);
    }


    /**
     * @param pc
     */
    public JMXTCPAuthenticator ( CredentialChecker pc ) {
        this(false, pc);
    }


    /**
     * @param allowAnonymous
     * @param pc
     */
    public JMXTCPAuthenticator ( boolean allowAnonymous, CredentialChecker pc ) {
        this.allowAnonymous = allowAnonymous;
        this.credentialChecker = pc;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.AbstractJMXAuthenticator#authenticate(java.lang.Object)
     */
    @Override
    public Subject authenticate ( Object credentials ) {
        if ( this.credentialChecker != null && credentials instanceof String[] && ( (String[]) credentials ).length == 2 ) {
            String[] cred = (String[]) credentials;
            String user = cred[ 0 ];
            String pass = cred[ 1 ];

            if ( log.isDebugEnabled() ) {
                log.debug("Have username " + user); //$NON-NLS-1$
            }

            if ( this.credentialChecker.verifyPassword(user, pass) ) {
                Set<Principal> princs = new HashSet<>();
                princs.add(new JMXPrincipal("anonymous")); //$NON-NLS-1$
                return new Subject(true, princs, Collections.EMPTY_SET, Collections.EMPTY_SET);
            }
        }

        if ( !this.allowAnonymous ) {
            throw new SecurityException("Need authentication"); //$NON-NLS-1$
        }
        return super.authenticate(credentials);
    }

}
