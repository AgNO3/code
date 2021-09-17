/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.base.internal;


import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.messaging.server.ClientCertificateListener;
import eu.agno3.orchestrator.server.base.component.ComponentCertificateTracker;
import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ComponentCertificateTracker.class, ClientCertificateListener.class
} )
public class ComponentCertificateTrackerImpl implements ComponentCertificateTracker, ClientCertificateListener {

    private static final Logger log = Logger.getLogger(ComponentCertificateTrackerImpl.class);

    private final Map<ComponentPrincipal, X509Certificate> certificates = new ConcurrentHashMap<>();


    @Override
    public X509Certificate getComponentCertificate ( ComponentPrincipal princ ) {
        return this.certificates.get(princ);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.messaging.server.ClientCertificateListener#haveValid(eu.agno3.orchestrator.server.component.auth.ComponentPrincipal,
     *      org.bouncycastle.asn1.x500.X500Name, java.security.cert.X509Certificate,
     *      java.security.cert.X509Certificate[])
     */
    @Override
    public void haveValid ( ComponentPrincipal princ, X500Name dn, X509Certificate primary, X509Certificate[] chain ) {
        X509Certificate old = this.certificates.put(princ, primary);
        if ( old == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Have new certificate for " + princ); //$NON-NLS-1$
            }
        }
        else if ( !old.equals(primary) ) {
            log.info("Certificate changed for for " + princ); //$NON-NLS-1$
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("Same certificate for " + princ); //$NON-NLS-1$
        }

        DateTime notAfter = new DateTime(primary.getNotAfter());
        DateTime limit = DateTime.now().plusWeeks(2);
        if ( limit.isAfter(notAfter) ) {
            log.warn(String.format("Certificate for %s is going to expire on %s", princ, notAfter)); //$NON-NLS-1$
        }
    }

}
