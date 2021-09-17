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
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;


/**
 * @author mbechler
 *
 */
public interface CRLPathChecker {

    /**
     * @param holder
     * @param issuer
     * @param crlSelector
     * @param extraDps
     * @param paramsWithChain
     * @return whether CRL information is available
     * @throws CertPathValidatorException
     */
    public abstract boolean checkCRLInfo ( JcaX509CertificateHolder holder, X500Principal issuer, X509CRLSelector crlSelector, Set<URI> extraDps,
            PKIXParameters paramsWithChain ) throws CertPathValidatorException;


    /**
     * @param cert
     * @param crlSelector
     * @param extraDps
     * @param paramsWithChain
     * @throws CertPathValidatorException
     */
    public abstract void doCheckCRLs ( X509Certificate cert, X509CRLSelector crlSelector, Set<URI> extraDps, PKIXParameters paramsWithChain )
            throws CertPathValidatorException;

}