/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public interface PKIXParameterFactory {

    /**
     * @param trustStore
     * @param revConfig
     * @param extraCertStores
     * @param extraCheckers
     * @param constraint
     * @return pkix checking parameters
     * @throws KeyStoreException
     * @throws InvalidAlgorithmParameterException
     * @throws CryptoException
     */
    PKIXBuilderParameters makePKIXParameters ( KeyStore trustStore, RevocationConfig revConfig, CertStore[] extraCertStores,
            PKIXCertPathChecker[] extraCheckers, CertSelector constraint ) throws KeyStoreException, InvalidAlgorithmParameterException,
            CryptoException;

}