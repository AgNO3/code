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

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;


/**
 * @author mbechler
 *
 */
public class AbstractJMXAuthenticator implements JMXAuthenticator {

    /**
     * {@inheritDoc}
     *
     * @see javax.management.remote.JMXAuthenticator#authenticate(java.lang.Object)
     */
    @Override
    public Subject authenticate ( Object credentials ) {
        Set<Principal> princs = new HashSet<>();
        princs.add(new JMXPrincipal("anonymous")); //$NON-NLS-1$
        return new Subject(true, princs, Collections.EMPTY_SET, Collections.EMPTY_SET);
    }
}
