/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto;


import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.joda.time.Duration;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface InternalCAConfig {

    /**
     * 
     */
    public static final String PID = "internalCA"; //$NON-NLS-1$


    /**
     * @return the caKeyAlias
     * @throws CryptoException
     */
    String getCaKeyAlias () throws CryptoException;


    /**
     * @return the caKeystoreName
     * @throws CryptoException
     */
    String getCaKeystoreName () throws CryptoException;


    /**
     * 
     * @return extra intermediate certificates to add for this ca
     * @throws CryptoException
     */
    X509Certificate[] getCaExtraChain () throws CryptoException;


    /**
     * @return the allowed key usage mask
     */
    int getKeyUsageMask ();


    /**
     * @param eku
     * @return whether the given extened key usage should be allowed
     */
    boolean isExtendedKeyUsageAllowed ( KeyPurposeId eku );


    /**
     * @return the maximum certificate lifetime to allow
     */
    Duration getMaximumLifetime ();


    /**
     * @return the ca certificate
     */
    X509Certificate getCaCertificate ();

}