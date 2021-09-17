/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.messaging.server;


import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x500.X500Name;

import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;


/**
 * @author mbechler
 *
 */
public interface ClientCertificateListener {

    /**
     * @param princ
     * @param dn
     * @param primary
     * @param chain
     */
    void haveValid ( ComponentPrincipal princ, X500Name dn, X509Certificate primary, X509Certificate[] chain );

}
