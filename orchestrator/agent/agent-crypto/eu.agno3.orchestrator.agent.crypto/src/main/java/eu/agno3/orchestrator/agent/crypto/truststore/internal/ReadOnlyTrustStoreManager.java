/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Set;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class ReadOnlyTrustStoreManager implements TruststoreManager {

    private TruststoreManager tm;


    /**
     * @param tm
     */
    public ReadOnlyTrustStoreManager ( TruststoreManager tm ) {
        this.tm = tm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#init()
     */
    @Override
    public void init () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#getVersion()
     */
    @Override
    public long getVersion () throws TruststoreManagerException {
        return this.tm.getVersion();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#hasCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public boolean hasCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        return this.tm.hasCertificate(cert);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCertificates()
     */
    @Override
    public Set<X509Certificate> listCertificates () throws TruststoreManagerException {
        return this.tm.listCertificates();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#addCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public void addCertificate ( X509Certificate cert ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#deleteAllCertificates()
     */
    @Override
    public void deleteAllCertificates () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#removeCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public void removeCertificate ( X509Certificate cert ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#setCertificates(java.util.Set)
     */
    @Override
    public void setCertificates ( Set<X509Certificate> trustAnchors ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#setRevocationConfig(eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig)
     */
    @Override
    public void setRevocationConfig ( RevocationConfig config ) throws TruststoreManagerException {
        this.tm.setRevocationConfig(config);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#getRevocationConfig()
     */
    @Override
    public RevocationConfig getRevocationConfig () throws TruststoreManagerException {
        return this.tm.getRevocationConfig();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#updateCRL(java.security.cert.X509CRL)
     */
    @Override
    public void updateCRL ( X509CRL crl ) throws TruststoreManagerException {
        this.tm.updateCRL(crl);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCRLs()
     */
    @Override
    public Set<X509CRL> listCRLs () throws TruststoreManagerException {
        return this.tm.listCRLs();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#isReadOnly()
     */
    @Override
    public boolean isReadOnly () {
        return true;
    }
}
