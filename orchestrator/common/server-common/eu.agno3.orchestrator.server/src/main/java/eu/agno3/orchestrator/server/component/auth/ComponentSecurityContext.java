/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.auth;


import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface ComponentSecurityContext {

    /**
     * 
     * @return principal collection
     */
    Set<Principal> getPrincipals ();


    /**
     * @return the component principal
     */
    ComponentPrincipal getComponentPrincipal ();


    /**
     * @return the certificate chain
     */
    X509Certificate[] getCertificateChain ();


    /**
     * @return the primary certificate
     */
    X509Certificate getCertificate ();

}