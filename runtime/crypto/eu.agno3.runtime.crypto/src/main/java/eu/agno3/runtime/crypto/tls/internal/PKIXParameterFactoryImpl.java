/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.truststore.revocation.CRLPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.DistributionPointCache;
import eu.agno3.runtime.crypto.truststore.revocation.NoDistributionPointCache;
import eu.agno3.runtime.crypto.truststore.revocation.OCSPCache;
import eu.agno3.runtime.crypto.truststore.revocation.OCSPPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.crypto.truststore.revocation.internal.CRLPathCheckerImpl;
import eu.agno3.runtime.crypto.truststore.revocation.internal.DistributionPointCacheImpl;
import eu.agno3.runtime.crypto.truststore.revocation.internal.OCSPCacheImpl;
import eu.agno3.runtime.crypto.truststore.revocation.internal.OCSPPathCheckerImpl;
import eu.agno3.runtime.crypto.truststore.revocation.internal.RevocationPathCheckerFactory;
import eu.agno3.runtime.crypto.truststore.revocation.internal.ThreadedRevocationPathChecker;


/**
 * @author mbechler
 *
 */
@Component ( service = PKIXParameterFactory.class )
public class PKIXParameterFactoryImpl implements PKIXParameterFactory {

    private SecureRandom random;
    private SecureRandomProvider randProv;


    /**
     * 
     */
    public PKIXParameterFactoryImpl () {}


    /**
     * @param secureRandom
     */
    public PKIXParameterFactoryImpl ( SecureRandom secureRandom ) {
        this.random = secureRandom;
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randProv = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randProv == srp ) {
            this.randProv = null;
        }
    }


    /**
     * @return the random
     */
    public SecureRandom getRandom () {
        if ( this.random == null ) {
            this.random = this.randProv.getSecureRandom();
        }
        return this.random;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.PKIXParameterFactory#makePKIXParameters(java.security.KeyStore,
     *      eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig, java.security.cert.CertStore[],
     *      java.security.cert.PKIXCertPathChecker[], java.security.cert.CertSelector)
     */
    @Override
    public PKIXBuilderParameters makePKIXParameters ( KeyStore trustStore, RevocationConfig revConfig, CertStore[] extraCertStores,
            PKIXCertPathChecker[] extraCheckers, CertSelector constraint ) throws KeyStoreException, InvalidAlgorithmParameterException,
            CryptoException {
        PKIXBuilderParameters params = new PKIXBuilderParameters(trustStore, null);
        params.setRevocationEnabled(false);
        if ( extraCertStores != null ) {
            for ( CertStore store : extraCertStores ) {
                params.addCertStore(store);
            }
        }

        if ( extraCheckers != null ) {
            for ( PKIXCertPathChecker checker : extraCheckers ) {
                params.addCertPathChecker(checker);
            }
        }

        if ( constraint != null ) {
            params.setTargetCertConstraints(constraint);
        }

        if ( revConfig != null ) {
            // setupRevocationChecker(params, cfg.getRevocationConfig());
            params.addCertPathChecker(new ThreadedRevocationPathChecker(new RevocationPathCheckerFactory(revConfig, (PKIXBuilderParameters) params
                    .clone(), makeCRLChecker(revConfig, getRandom()), makeOCSPChecker(revConfig, getRandom()))));
        }

        return params;
    }


    // /**
    // * @param params
    // * @param revocationConfig
    // * @throws CryptoException
    // */
    // private static void setupRevocationChecker ( PKIXBuilderParameters params, RevocationConfig revocationConfig )
    // throws CryptoException {
    // try {
    // PKIXRevocationChecker checker = (PKIXRevocationChecker) CertPathBuilder.getInstance(PKIX).getRevocationChecker();
    // checker.setOptions(makeOptions(revocationConfig));
    // params.addCertPathChecker(checker);
    // }
    // catch ( NoSuchAlgorithmException e ) {
    //            throw new CryptoException("Failed to setup revocation checker", e); //$NON-NLS-1$
    // }
    // }
    //
    //
    // /**
    // * @param revocationConfig
    // * @return
    // */
    // private static Set<Option> makeOptions ( RevocationConfig revocationConfig ) {
    //
    // Set<Option> opt = new HashSet<>();
    //
    // if ( revocationConfig.isCheckOnlyEndEntityCerts() ) {
    // opt.add(Option.ONLY_END_ENTITY);
    // }
    //
    // if ( !revocationConfig.isCheckOCSP() ) {
    // opt.add(Option.PREFER_CRLS);
    // opt.add(Option.NO_FALLBACK);
    // }
    // else if ( !revocationConfig.isCheckCRL() ) {
    // opt.add(Option.NO_FALLBACK);
    // }
    //
    // return null;
    // }

    /**
     * @param revocationConfig
     * @return
     */
    private static OCSPPathChecker makeOCSPChecker ( RevocationConfig revocationConfig, SecureRandom random ) {
        OCSPCache cache;
        if ( revocationConfig.getOcspCacheSize() <= 0 ) {
            cache = new NoOCSPCache();
        }
        else {
            cache = new OCSPCacheImpl(revocationConfig);
        }
        return new OCSPPathCheckerImpl(revocationConfig, cache, random);
    }


    /**
     * @param revocationConfig
     * @return
     */
    private static CRLPathChecker makeCRLChecker ( RevocationConfig revocationConfig, SecureRandom random ) {
        DistributionPointCache cache;
        if ( revocationConfig.getCrlCacheSize() <= 0 ) {
            cache = new NoDistributionPointCache();
        }
        else {
            cache = new DistributionPointCacheImpl(revocationConfig, random);
        }
        return new CRLPathCheckerImpl(revocationConfig, cache);
    }

}
