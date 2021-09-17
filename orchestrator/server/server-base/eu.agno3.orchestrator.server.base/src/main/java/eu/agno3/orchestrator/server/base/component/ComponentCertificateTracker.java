/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import java.security.cert.X509Certificate;

import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;


/**
 * @author mbechler
 *
 */
public interface ComponentCertificateTracker {

    /**
     * @param princ
     * @return most recent certificate for component, null if unknown
     */
    X509Certificate getComponentCertificate ( ComponentPrincipal princ );

}
