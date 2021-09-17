/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


import java.net.URI;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Set;

import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;


/**
 * @author mbechler
 *
 */
public interface OCSPPathChecker {

    /**
     * @param cert
     * @param issuerCert
     * @param ocspUris
     * @param paramsWithChain
     * @return whether OCSP has been checked
     * @throws CertPathValidatorException
     */
    public abstract boolean doCheckOCSP ( X509Certificate cert, X509Certificate issuerCert, Set<URI> ocspUris, PKIXParameters paramsWithChain )
            throws CertPathValidatorException;


    /**
     * @param holder
     * @param ocspUris
     * @param paramsWithChain
     * @return whether ocsp responders are present in authority info access
     */
    public abstract boolean hasOCSPResponders ( JcaX509CertificateHolder holder, Set<URI> ocspUris, PKIXParameters paramsWithChain );

}