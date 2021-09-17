/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.messaging.server.ClientCertificateListener;
import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = ClientCertificateHandlerImpl.class )
public class ClientCertificateHandlerImpl {

    private static final Logger log = Logger.getLogger(ClientCertificateHandlerImpl.class);

    private final Set<ClientCertificateListener> listeners = new HashSet<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindCertificateListener ( ClientCertificateListener l ) {
        this.listeners.add(l);
    }


    protected synchronized void unbindCertificateListener ( ClientCertificateListener l ) {
        this.listeners.remove(l);
    }


    /**
     * @param princ
     * @param dn
     * @param primary
     * @param chain
     */
    public synchronized void haveValid ( ComponentPrincipal princ, X500Name dn, X509Certificate primary, X509Certificate[] chain ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Have certificate for %s: %s", princ, primary)); //$NON-NLS-1$
        }
        synchronized ( this ) {
            for ( ClientCertificateListener cl : this.listeners ) {
                try {
                    cl.haveValid(princ, dn, primary, chain);
                }
                catch ( Exception e ) {
                    log.warn("Certificate listener failed", e); //$NON-NLS-1$
                }
            }
        }
    }

}
