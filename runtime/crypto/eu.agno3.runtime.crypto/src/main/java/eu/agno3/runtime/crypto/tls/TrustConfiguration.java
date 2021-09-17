/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.KeyStore;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXCertPathChecker;

import javax.net.ssl.TrustManagerFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public interface TrustConfiguration {

    /**
     * 
     */
    public static final String PID = "x509.trust"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String STORE = "store"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String ID = "instanceId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String STOREPASS = "storePass"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CHECK_REVOCATION = "checkRevocation"; //$NON-NLS-1$


    /**
     * @return the trust store
     */
    KeyStore getTrustStore ();


    /**
     * @return a unique identifier for this trust store
     */
    String getId ();


    /**
     * @return whether recovation checking (CRL, OCSP) is enabled.
     */
    boolean isCheckRevocation ();


    /**
     * @return extra path validating checkers
     * @throws CryptoException
     */
    PKIXCertPathChecker[] getExtraCertPathCheckers () throws CryptoException;


    /**
     * @return extra certificate stores (for CRL retrieval)
     * @throws CryptoException
     */
    CertStore[] getExtraCertStores () throws CryptoException;


    /**
     * 
     * @return constraint on trusted certificates
     * @throws CryptoException
     */
    CertSelector getConstraint () throws CryptoException;


    /**
     * @return revocation config
     */
    RevocationConfig getRevocationConfig ();


    /**
     * @return the trust manager factory
     * @throws CryptoException
     */
    TrustManagerFactory getTrustManagerFactory () throws CryptoException;

}
