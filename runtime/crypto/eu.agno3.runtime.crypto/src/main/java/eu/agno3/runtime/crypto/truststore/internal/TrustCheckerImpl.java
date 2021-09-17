/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustChecker.class )
public class TrustCheckerImpl implements TrustChecker {

    private static final String EXTENDED_KEY_USAGE_EXT_OID = "2.5.29.37"; //$NON-NLS-1$
    private static final String NS_CERT_TYPE_EXT_OID = "2.16.840.1.113730.1.1"; //$NON-NLS-1$
    private PKIXParameterFactory pkixParameterFactory;


    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory pf ) {
        this.pkixParameterFactory = pf;
    }


    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory pf ) {
        if ( this.pkixParameterFactory == pf ) {
            this.pkixParameterFactory = null;
        }
    }


    @Override
    public PKIXCertPathBuilderResult validateChain ( TrustConfiguration cfg, List<Certificate> chain, Date date, boolean[] keyUsage,
            Set<String> ekus ) throws CryptoException {
        Certificate ee = chain.get(0);
        Set<Certificate> others = new HashSet<>(chain);
        others.remove(ee);
        return validate(cfg, ee, others, date, keyUsage, ekus);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustChecker#validate(eu.agno3.runtime.crypto.tls.TrustConfiguration,
     *      java.security.cert.Certificate, java.util.Collection, java.util.Date, boolean[], java.util.Set)
     */
    @Override
    public PKIXCertPathBuilderResult validate ( TrustConfiguration cfg, Certificate ee, Collection<Certificate> other, Date date, boolean[] keyUsage,
            Set<String> ekus ) throws CryptoException {
        try {
            X509CertSelector selector = new X509CertSelector();
            X509Certificate cert = (X509Certificate) ee;
            selector.setCertificate(cert);
            if ( ekus != null ) {
                selector.setExtendedKeyUsage(ekus);
            }
            if ( keyUsage != null ) {
                selector.setKeyUsage(keyUsage);
            }

            Set<Certificate> all = new HashSet<>(other);
            all.add(ee);

            if ( cfg.getTrustStore() == null || cfg.getTrustStore().size() == 0 ) {
                return null;
            }

            PKIXBuilderParameters pkixParams = this.pkixParameterFactory.makePKIXParameters(
                cfg.getTrustStore(),
                cfg.isCheckRevocation() ? cfg.getRevocationConfig() : null,
                cfg.getExtraCertStores(),
                cfg.getExtraCertPathCheckers(),
                selector);
            pkixParams.setDate(date);

            CertStore intermediateCertStore = CertStore.getInstance(
                "Collection", //$NON-NLS-1$
                new CollectionCertStoreParameters(all));
            pkixParams.addCertStore(intermediateCertStore);
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX"); //$NON-NLS-1$
            PKIXCertPathBuilderResult r = (PKIXCertPathBuilderResult) builder.build(pkixParams);

            if ( r != null ) {
                checkCertificate(ekus, r);
            }

            return r;
        }
        catch (
            IllegalArgumentException |
            InvalidAlgorithmParameterException |
            NoSuchAlgorithmException |
            CertPathBuilderException |
            KeyStoreException |
            IOException e ) {
            throw new CryptoException("Certificate is not trusted " + ( (X509Certificate) ee ).getSubjectX500Principal().toString(), e); //$NON-NLS-1$
        }
    }


    /**
     * @param ekus
     * @param r
     * @throws CryptoException
     */
    protected void checkCertificate ( Set<String> ekus, PKIXCertPathBuilderResult r ) throws CryptoException {
        List<? extends Certificate> vcerts = r.getCertPath().getCertificates();
        if ( vcerts.isEmpty() ) {
            throw new CryptoException("Certification path is empty"); //$NON-NLS-1$
        }

        Certificate vcert = vcerts.get(0);
        if ( ! ( vcert instanceof X509Certificate ) ) {
            throw new CryptoException("Unknown end entity certificate"); //$NON-NLS-1$
        }

        // CertPathBuilder does not care when no EKU is present but one requested
        if ( ekus != null && !ekus.isEmpty() ) {
            X509Certificate xcert = (X509Certificate) vcert;
            byte[] extensionValue = xcert.getExtensionValue(EXTENDED_KEY_USAGE_EXT_OID); // $NON-NLS-1$
            if ( extensionValue == null ) {
                // try netscape NetscapeCertType instead
                try {
                    DERBitString parsedValue = (DERBitString) ( new JcaX509CertificateHolder(xcert) )
                            .getExtension(new ASN1ObjectIdentifier(NS_CERT_TYPE_EXT_OID)).getParsedValue();

                    if ( parsedValue != null ) {
                        NetscapeCertType t = new NetscapeCertType(parsedValue);
                        if ( !checkNetscapeCERTTypeUsages(ekus, t.intValue()) ) {
                            throw new CryptoException("Missing extended key usages " + ekus); //$NON-NLS-1$
                        }
                    }

                }
                catch (
                    CertificateEncodingException |
                    IOException e ) {
                    throw new CryptoException("Failed to read netscape cert type extension", e); //$NON-NLS-1$
                }

            }
        }
    }


    /**
     * @param ekus
     * @return
     * @throws CryptoException
     * @throws IOException
     */
    private static boolean checkNetscapeCERTTypeUsages ( Set<String> ekus, int i ) throws CryptoException, IOException {
        for ( String eku : ekus ) {
            switch ( eku ) {
            // serverAuth
            case "1.3.6.1.5.5.7.3.1": //$NON-NLS-1$
                if ( ( i & NetscapeCertType.sslServer ) == 0 ) {
                    return false;
                }
                break;
            // clientAuth
            case "1.3.6.1.5.5.7.3.2": //$NON-NLS-1$
                if ( ( i & NetscapeCertType.sslClient ) == 0 ) {
                    return false;
                }
                break;
            // codeSigning
            case "1.3.6.1.5.5.7.3.3": //$NON-NLS-1$
                if ( ( i & NetscapeCertType.objectSigning ) == 0 ) {
                    return false;
                }
                break;
            default:
                throw new CryptoException("Missing extended key usage " + eku); //$NON-NLS-1$
            }
        }
        return true;
    }
}
