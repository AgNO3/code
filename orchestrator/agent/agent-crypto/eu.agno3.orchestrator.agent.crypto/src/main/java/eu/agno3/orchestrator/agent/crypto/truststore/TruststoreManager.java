/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore;


import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Set;

import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public interface TruststoreManager {

    /**
     * @throws TruststoreManagerException
     * 
     */
    void init () throws TruststoreManagerException;


    /**
     * 
     * @return the revocation config
     * @throws TruststoreManagerException
     */
    RevocationConfig getRevocationConfig () throws TruststoreManagerException;


    /**
     * @param config
     * @throws TruststoreManagerException
     * 
     */
    void setRevocationConfig ( RevocationConfig config ) throws TruststoreManagerException;


    /**
     * 
     * @return version number for detecting changes
     * @throws TruststoreManagerException
     */
    long getVersion () throws TruststoreManagerException;


    /**
     * 
     * @return whether this store is read only
     */
    boolean isReadOnly ();


    /**
     * 
     * @param cert
     * @return whether the given certificate is trusted
     * @throws TruststoreManagerException
     */
    boolean hasCertificate ( X509Certificate cert ) throws TruststoreManagerException;


    /**
     * @return the trusted certificates
     * @throws TruststoreManagerException
     */
    Set<X509Certificate> listCertificates () throws TruststoreManagerException;


    /**
     * @param cert
     * @throws TruststoreManagerException
     */
    void addCertificate ( X509Certificate cert ) throws TruststoreManagerException;


    /**
     * @param cert
     * @throws TruststoreManagerException
     */
    void removeCertificate ( X509Certificate cert ) throws TruststoreManagerException;


    /**
     * @param crl
     * @throws TruststoreManagerException
     */
    void updateCRL ( X509CRL crl ) throws TruststoreManagerException;


    /**
     * @return the known CRLs
     * @throws TruststoreManagerException
     */
    Set<X509CRL> listCRLs () throws TruststoreManagerException;


    /**
     * @param trustAnchors
     * @throws TruststoreManagerException
     */
    void setCertificates ( Set<X509Certificate> trustAnchors ) throws TruststoreManagerException;


    /**
     * 
     */
    void deleteAllCertificates ();

}
