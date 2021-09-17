/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.activemq.security.SecurityContext;

import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;
import eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext;


/**
 * @author mbechler
 *
 */
public class ComponentSecurityContextImpl extends SecurityContext implements ComponentSecurityContext {

    private X509Certificate certificate;
    private ComponentPrincipal princ;
    private X509Certificate chain[];


    /**
     * @param princ
     * @param cert
     * @param chain
     */
    public ComponentSecurityContextImpl ( ComponentPrincipal princ, X509Certificate cert, X509Certificate chain[] ) {
        super(cert.getSubjectX500Principal().getName("CANONICAL")); //$NON-NLS-1$
        this.princ = princ;
        this.certificate = cert;
        if ( chain != null ) {
            this.chain = Arrays.copyOf(chain, chain.length);
        }
        else {
            this.chain = chain;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext#getPrincipals()
     */
    @Override
    public Set<Principal> getPrincipals () {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(this.getX509Principal(), this.getComponentPrincipal())));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext#getComponentPrincipal()
     */
    @Override
    public ComponentPrincipal getComponentPrincipal () {
        return this.princ;
    }


    /**
     * @return
     */
    private X500Principal getX509Principal () {
        return this.getCertificate().getSubjectX500Principal();
    }


    /**
     * @return the certificate
     */
    @Override
    public X509Certificate getCertificate () {
        return this.certificate;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext#getCertificateChain()
     */
    @Override
    public X509Certificate[] getCertificateChain () {
        if ( this.chain != null ) {
            return Arrays.copyOf(this.chain, this.chain.length);
        }
        return this.chain;
    }

}
