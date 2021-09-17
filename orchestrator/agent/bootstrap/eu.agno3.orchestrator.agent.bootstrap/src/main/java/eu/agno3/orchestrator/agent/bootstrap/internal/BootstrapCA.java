/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.security.KeyPair;
import java.security.Provider;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;


/**
 * @author mbechler
 *
 */
public class BootstrapCA implements AutoCloseable {

    private KeyPair keyPair;
    private X509Certificate cert;
    private X509CRL crl;
    private KeystoreManager caKeystore;


    /**
     * @param caKeystore
     * @param caKeyPair
     * @param caCert
     * @param crl
     */
    public BootstrapCA ( KeystoreManager caKeystore, KeyPair caKeyPair, X509Certificate caCert, X509CRL crl ) {
        this.caKeystore = caKeystore;
        this.keyPair = caKeyPair;
        this.cert = caCert;
        this.crl = crl;
    }


    /**
     * @return the crypto provider
     */
    public Provider getProvider () {
        return this.caKeystore.getProvider();
    }


    /**
     * @return the key pair reference
     */
    public KeyPair getKeyPair () {
        return this.keyPair;
    }


    /**
     * @return the CA certificate
     */
    public X509Certificate getCert () {
        return this.cert;
    }


    /**
     * @return the crl
     */
    public X509CRL getCrl () {
        return this.crl;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws KeystoreManagerException {
        this.caKeystore.close();
    }

}
