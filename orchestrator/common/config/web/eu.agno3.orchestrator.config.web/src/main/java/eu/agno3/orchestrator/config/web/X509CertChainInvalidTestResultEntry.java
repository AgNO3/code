/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.agno3.orchestrator.config.model.validation.ConfigTestChallenge;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultEntry;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultSeverity;
import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class X509CertChainInvalidTestResultEntry extends ConfigTestResultEntry implements ConfigTestChallenge {

    /**
     * 
     */
    private static final long serialVersionUID = 8281089720142245699L;

    private List<X509Certificate> chain = new ArrayList<>();


    /**
     * 
     */
    public X509CertChainInvalidTestResultEntry () {}


    /**
     * 
     * @param sev
     * @param msgTemplate
     * @param chain
     * @param args
     */
    public X509CertChainInvalidTestResultEntry ( ConfigTestResultSeverity sev, String msgTemplate, X509Certificate[] chain, String... args ) {
        super(sev, msgTemplate, args);
        if ( chain != null ) {
            List<X509Certificate> ces = new ArrayList<>();
            ces.addAll(Arrays.asList(chain));
            this.chain = ces;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ConfigTestChallenge#getType()
     */
    @Override
    public String getType () {
        return "x509-certificate-trust"; //$NON-NLS-1$
    }


    /**
     * @return the chain
     */
    public List<X509Certificate> getChain () {
        return this.chain;
    }


    /**
     * @param chain
     *            the chain to set
     */
    public void setChain ( List<X509Certificate> chain ) {
        this.chain = chain;
    }


    /**
     * 
     * @return the end entity certificate
     */
    public X509Certificate getEndEntityCertificate () {
        if ( this.chain == null || this.chain.isEmpty() ) {
            return null;
        }
        return this.chain.get(0);
    }
}
