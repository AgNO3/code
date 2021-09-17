/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.util.Set;

import eu.agno3.runtime.crypto.truststore.revocation.CRLPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.OCSPPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class RevocationPathCheckerFactory implements PKIXCertPathCheckerFactory {

    private RevocationConfig revocationConfig;
    private PKIXBuilderParameters pkixParameters;
    private OCSPPathChecker ocspChecker;
    private CRLPathChecker crlChecker;


    /**
     * @param config
     * @param pkixParameters
     * @param crlChecker
     * @param ocspChecker
     * 
     */
    public RevocationPathCheckerFactory ( RevocationConfig config, PKIXBuilderParameters pkixParameters, CRLPathChecker crlChecker,
            OCSPPathChecker ocspChecker ) {
        this.revocationConfig = config;
        this.pkixParameters = pkixParameters;
        this.crlChecker = crlChecker;
        this.ocspChecker = ocspChecker;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.internal.PKIXCertPathCheckerFactory#isForwardCheckingSupported()
     */
    @Override
    public boolean isForwardCheckingSupported () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.internal.PKIXCertPathCheckerFactory#getSupportedExtensions()
     */
    @Override
    public Set<String> getSupportedExtensions () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.internal.PKIXCertPathCheckerFactory#createInstance()
     */
    @Override
    public PKIXCertPathChecker createInstance () {
        return new RevocationPathChecker(this.revocationConfig, (PKIXParameters) this.pkixParameters.clone(), this.crlChecker, this.ocspChecker);
    }

}
