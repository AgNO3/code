/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import eu.agno3.runtime.crypto.truststore.revocation.OCSPCache;
import eu.agno3.runtime.crypto.truststore.revocation.OCSPPathChecker;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class OCSPPathCheckerImpl implements OCSPPathChecker {

    private static final Logger log = Logger.getLogger(OCSPPathCheckerImpl.class);

    private static final String HTTP = "http"; //$NON-NLS-1$
    private static final String HTTPS = "https"; //$NON-NLS-1$
    private static final int NONCE_SIZE = 16;

    private RevocationConfig revocationConfig;
    private OCSPCache cache;

    private SecureRandom random;


    /**
     * @param revocationConfig
     * @param cache
     * @param random
     */
    public OCSPPathCheckerImpl ( RevocationConfig revocationConfig, OCSPCache cache, SecureRandom random ) {
        this.revocationConfig = revocationConfig;
        this.cache = cache;
        this.random = random;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.OCSPPathChecker#doCheckOCSP(java.security.cert.X509Certificate,
     *      java.security.cert.X509Certificate, java.util.Set, java.security.cert.PKIXParameters)
     */
    @Override
    public boolean doCheckOCSP ( X509Certificate cert, X509Certificate issuerCert, Set<URI> ocspUris, PKIXParameters paramsWithChain )
            throws CertPathValidatorException {

        CertificateID certId;
        try {
            certId = getRequestCertID(cert, issuerCert);
        }
        catch (
            CertificateEncodingException |
            OperatorCreationException |
            OCSPException e ) {
            throw new CertPathValidatorException("Failed to generate cert id", e); //$NON-NLS-1$
        }

        // check cached
        SingleResp cached = this.cache.getCached(certId);
        if ( checkCached(cached) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found cached GOOD response for " + cert.getSubjectDN().getName()); //$NON-NLS-1$
            }
            return true;
        }

        byte[] nonce = new byte[NONCE_SIZE];
        this.random.nextBytes(nonce);
        OCSPReq req = generateRequest(certId, nonce);

        URI overrideOCSPUri = this.revocationConfig.getSystemOCSPUri();
        if ( this.revocationConfig.isCheckAllUsingSystemOCSP() || ( ocspUris.isEmpty() && overrideOCSPUri != null ) ) {
            // cert is checked against the system ocsp
            X509Certificate systemTrustCert = this.revocationConfig.getSystemOCSPTrustCert();
            return this.checkOCSPUrls(certId, req, ocspUris, systemTrustCert, makeSystemParams(paramsWithChain, systemTrustCert), nonce);
        }
        else if ( !ocspUris.isEmpty() ) {
            // check using ocsps in certificate
            X509Certificate validationCert = issuerCert;
            return this.checkOCSPUrls(certId, req, ocspUris, validationCert, paramsWithChain, nonce);

        }

        return false;
    }


    /**
     * @param paramsWithChain
     * @param systemTrustCert
     * @return
     * @throws CertPathValidatorException
     */
    private static PKIXParameters makeSystemParams ( PKIXParameters paramsWithChain, X509Certificate systemTrustCert )
            throws CertPathValidatorException {
        PKIXParameters params = (PKIXParameters) paramsWithChain.clone();
        Set<TrustAnchor> anchors = new HashSet<>(paramsWithChain.getTrustAnchors());
        anchors.add(new TrustAnchor(systemTrustCert, null));
        try {
            params.setTrustAnchors(anchors);
        }
        catch ( InvalidAlgorithmParameterException e ) {
            throw new CertPathValidatorException("Failed to add trusted ocsp responder cert", e); //$NON-NLS-1$
        }
        return params;
    }


    /**
     * @param cached
     * @return
     * @throws CertPathValidatorException
     */
    private static boolean checkCached ( SingleResp cached ) throws CertPathValidatorException {

        if ( cached == null ) {
            return false;
        }

        if ( cached.getNextUpdate().before(new Date()) ) {
            return false;
        }

        if ( cached.getCertStatus() == CertificateStatus.GOOD ) {
            return true;
        }

        throw new CertPathValidatorException("Certificate is revoked by OCSP (cached)", null, null, -1, BasicReason.REVOKED); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.revocation.OCSPPathChecker#hasOCSPResponders(org.bouncycastle.cert.jcajce.JcaX509CertificateHolder,
     *      java.util.Set, java.security.cert.PKIXParameters)
     */
    @Override
    public boolean hasOCSPResponders ( JcaX509CertificateHolder holder, Set<URI> ocspUris, PKIXParameters paramsWithChain ) {
        Extension extension = holder.getExtension(Extension.authorityInfoAccess);
        if ( extension != null ) {
            AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(extension.getParsedValue());
            AccessDescription[] accessDescriptions = aia.getAccessDescriptions();
            if ( accessDescriptions != null ) {
                getOCSPRespondersFromAIA(accessDescriptions, ocspUris);
            }
        }

        return !ocspUris.isEmpty();
    }


    /**
     * @param aiaExt
     * @param ocspUris
     */
    private static void getOCSPRespondersFromAIA ( AccessDescription[] accessDescriptions, Set<URI> ocspUris ) {
        for ( AccessDescription desc : accessDescriptions ) {
            if ( AccessDescription.id_ad_ocsp.equals(desc.getAccessMethod())
                    && desc.getAccessLocation().getTagNo() == GeneralName.uniformResourceIdentifier ) {
                processOCSPURL(ocspUris, desc);
            }
        }
    }


    /**
     * @param ocspUris
     * @param desc
     */
    protected static void processOCSPURL ( Set<URI> ocspUris, AccessDescription desc ) {
        URI accessUri;
        try {
            accessUri = new URI(desc.getAccessLocation().getName().toString());
        }
        catch ( URISyntaxException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to parse OCSP URL " + desc.getAccessLocation().getName(), e); //$NON-NLS-1$
            }
            return;
        }

        if ( !HTTP.equals(accessUri.getScheme()) && !HTTPS.equals(accessUri.getScheme()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not a HTTP OCSP URL " + accessUri); //$NON-NLS-1$
            }
            return;
        }
        ocspUris.add(accessUri);
    }


    /**
     * @param req
     * @param ocspUris
     * @param validationCert
     * @param paramsWithChain
     * @param paramsWithChain
     * @return
     * @throws CertPathValidatorException
     */
    private boolean checkOCSPUrls ( CertificateID requestedCertId, OCSPReq req, Set<URI> ocspUris, X509Certificate validationCert,
            PKIXParameters paramsWithChain, byte[] nonce ) throws CertPathValidatorException {
        for ( URI ocspUri : ocspUris ) {
            try {
                HttpURLConnection httpConn = configureHttpClient(paramsWithChain, ocspUri);
                sendHTTPOCSPRequest(req, httpConn);
                OCSPResp resp = readOCSPResponse(httpConn);
                if ( resp.getStatus() != OCSPResp.SUCCESSFUL ) {
                    throw new OCSPException("OCSP request failed with response status " + resp.getStatus()); //$NON-NLS-1$
                }

                if ( ! ( resp.getResponseObject() instanceof BasicOCSPResp ) ) {
                    throw new OCSPException("Parsed OCSP response is not BasicOCSPResp"); //$NON-NLS-1$
                }
                BasicOCSPResp ocspResp = (BasicOCSPResp) resp.getResponseObject();
                validateOCSPResponse(validationCert, nonce, ocspResp);
                log.debug("OCSP request succeeded"); //$NON-NLS-1$
                if ( getStatusFromResponse(requestedCertId, ocspResp) ) {
                    return true;
                }

            }
            catch (
                IOException |
                OCSPException |
                OperatorCreationException |
                CertificateException |
                CertException e ) {
                log.warn("OCSP request failed", e); //$NON-NLS-1$
            }
        }

        return false;

    }


    /**
     * @param validationCert
     * @param nonce
     * @param ocspResp
     * @throws OCSPException
     * @throws IOException
     * @throws CertException
     * @throws OperatorCreationException
     * @throws CertificateException
     */
    protected void validateOCSPResponse ( X509Certificate validationCert, byte[] nonce, BasicOCSPResp ocspResp ) throws OCSPException, IOException,
            CertException, OperatorCreationException, CertificateException {
        Set<String> criticalExtensionOIDs = new HashSet<>(ocspResp.getCriticalExtensionOIDs());
        criticalExtensionOIDs.remove(OCSPObjectIdentifiers.id_pkix_ocsp_nonce.toString());

        if ( !criticalExtensionOIDs.isEmpty() ) {
            throw new OCSPException("Unsupported critical extensions in OCSP response " + criticalExtensionOIDs); //$NON-NLS-1$
        }

        Extension extension = ocspResp.getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);
        if ( extension != null ) {
            checkNonce(nonce, extension);
        }
        else {
            log.warn("No nonce extension present"); //$NON-NLS-1$
        }

        validateOCSPResponseSignature(validationCert, ocspResp);
    }


    /**
     * @param nonce
     * @param extension
     * @throws IOException
     * @throws OCSPException
     */
    protected void checkNonce ( byte[] nonce, Extension extension ) throws IOException, OCSPException {
        byte[] got = ASN1OctetString.getInstance(extension.getExtnValue()).getOctets();
        if ( !Arrays.equals(got, nonce) ) {
            throw new OCSPException(String.format("OCSP Nonce does not match, expected %s got %s",//$NON-NLS-1$
                Arrays.toString(nonce),
                Arrays.toString(got)));
        }
    }


    /**
     * @param requestedCertId
     * @param ocspResp
     * @return
     * @throws CertPathValidatorException
     */
    protected boolean getStatusFromResponse ( CertificateID requestedCertId, BasicOCSPResp ocspResp ) throws CertPathValidatorException {
        SingleResp res = getResponseEntry(requestedCertId, ocspResp);
        if ( res == null ) {
            return false;
        }

        this.cache.updateCache(res);
        if ( res.getCertStatus() == CertificateStatus.GOOD ) {
            return true;
        }
        throw new CertPathValidatorException("Certificate is revoked by OCSP", null, null, -1, BasicReason.REVOKED); //$NON-NLS-1$
    }


    /**
     * @param requestedCertId
     * @param ocspResp
     * @return
     */
    protected SingleResp getResponseEntry ( CertificateID requestedCertId, BasicOCSPResp ocspResp ) {
        for ( SingleResp respEntry : ocspResp.getResponses() ) {
            CertificateID certID = respEntry.getCertID();
            if ( certID.equals(requestedCertId) ) {
                log.debug("Found entry for requested cert id"); //$NON-NLS-1$
                return respEntry;
            }
        }
        return null;
    }


    /**
     * @param validationCert
     * @param ocspResp
     * @throws OCSPException
     * @throws CertException
     * @throws OperatorCreationException
     * @throws CertificateException
     */
    protected void validateOCSPResponseSignature ( X509Certificate validationCert, BasicOCSPResp ocspResp ) throws OCSPException, CertException,
            OperatorCreationException, CertificateException {
        JcaContentVerifierProviderBuilder cfpb = new JcaContentVerifierProviderBuilder();
        ContentVerifierProvider cfp;
        X509CertificateHolder[] certs = ocspResp.getCerts();
        if ( certs != null && certs.length > 0 ) {

            if ( !certs[ 0 ].isValidOn(new Date()) ) {
                throw new OCSPException("OCSP signigng certificate has expired or is not yet valid"); //$NON-NLS-1$
            }

            if ( !certs[ 0 ].isSignatureValid(new JcaContentVerifierProviderBuilder().build(validationCert)) ) {
                throw new OCSPException("OCSP signing certificate is not signed by trusted issuer " + validationCert.getSubjectDN().getName()); //$NON-NLS-1$
            }

            ExtendedKeyUsage eku = ExtendedKeyUsage.fromExtensions(certs[ 0 ].getExtensions());
            if ( eku == null || !eku.hasKeyPurposeId(KeyPurposeId.id_kp_OCSPSigning) ) {
                throw new OCSPException("OCSP signing certificate does not have OCSPSigning EKU"); //$NON-NLS-1$
            }

            cfp = cfpb.build(certs[ 0 ]);
        }
        else {
            cfp = cfpb.build(validationCert);
        }
        if ( !ocspResp.isSignatureValid(cfp) ) {
            throw new OCSPException("OCSP response signature is not valid"); //$NON-NLS-1$
        }
    }


    /**
     * @param httpConn
     * @return
     * @throws IOException
     */
    protected OCSPResp readOCSPResponse ( HttpURLConnection httpConn ) throws IOException {
        if ( httpConn.getResponseCode() != 200 ) {

        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( InputStream is = httpConn.getInputStream() ) {
            int read = -1;
            while ( ( read = is.read() ) >= 0 ) {
                bos.write(read);
            }
        }

        return new OCSPResp(bos.toByteArray());
    }


    /**
     * @param req
     * @param httpConn
     * @throws IOException
     */
    protected void sendHTTPOCSPRequest ( OCSPReq req, HttpURLConnection httpConn ) throws IOException {
        httpConn.setDoOutput(true);
        httpConn.setUseCaches(false);
        httpConn.setConnectTimeout(this.revocationConfig.getConnectTimeout());
        httpConn.setReadTimeout(this.revocationConfig.getReadTimeout());
        httpConn.setRequestMethod("POST"); //$NON-NLS-1$
        httpConn.setRequestProperty("Content-Type", //$NON-NLS-1$
            "application/ocsp-request"); //$NON-NLS-1$

        try ( OutputStream os = httpConn.getOutputStream() ) {
            os.write(req.getEncoded());
        }

    }


    /**
     * @param httpParams
     * @param ocspUri
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws OCSPException
     */
    protected HttpURLConnection configureHttpClient ( PKIXParameters httpParams, URI ocspUri ) throws MalformedURLException, IOException,
            OCSPException {
        URL ocspUrl = ocspUri.toURL();
        URLConnection urlConn = ocspUrl.openConnection();

        if ( ! ( urlConn instanceof HttpURLConnection ) ) {
            throw new OCSPException("Not a HTTP client"); //$NON-NLS-1$
        }

        if ( urlConn instanceof HttpsURLConnection ) {
            setupSSLClient((HttpsURLConnection) urlConn, httpParams);
        }

        return (HttpURLConnection) urlConn;
    }


    /**
     * @param urlConn
     * @param tms
     * @throws OCSPException
     */
    private void setupSSLClient ( HttpsURLConnection urlConn, PKIXParameters params ) throws OCSPException {

        try {
            SSLContext ctx = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX"); //$NON-NLS-1$
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(params.getTrustAnchors(), params.getTargetCertConstraints());
            // this could otherwise lead to an infinite loop
            // not a very good thing to do
            parameters.setRevocationEnabled(false);
            tmf.init(new CertPathTrustManagerParameters(parameters));
            ctx.init(null, tmf.getTrustManagers(), this.random);
            urlConn.setSSLSocketFactory(ctx.getSocketFactory());
        }
        catch ( GeneralSecurityException e ) {
            throw new OCSPException("Failed to setup ssl ocsp client", e); //$NON-NLS-1$
        }
    }


    /**
     * @param cert
     * @param issuerCert
     * @param nonce
     * @return
     * @throws OperatorCreationException
     * @throws OCSPException
     * @throws CertificateEncodingException
     * @throws CertPathValidatorException
     */
    protected OCSPReq generateRequest ( CertificateID certId, byte[] nonce ) throws CertPathValidatorException {
        try {
            OCSPReqBuilder ocspReqBuilder = new OCSPReqBuilder();
            ocspReqBuilder.setRequestExtensions(new Extensions(new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false, new DEROctetString(
                nonce))));

            ocspReqBuilder.addRequest(certId);
            return ocspReqBuilder.build();
        }
        catch ( OCSPException e ) {
            log.debug("Failed to build OCSP request", e); //$NON-NLS-1$
            throw new CertPathValidatorException("Could not determine revocation status, failed to build OCSP request", //$NON-NLS-1$
                null,
                null,
                -1,
                BasicReason.UNDETERMINED_REVOCATION_STATUS);
        }
    }


    /**
     * @param cert
     * @param issuerCert
     * @return
     * @throws OperatorCreationException
     * @throws OCSPException
     * @throws CertificateEncodingException
     */
    protected CertificateID getRequestCertID ( X509Certificate cert, X509Certificate issuerCert ) throws OperatorCreationException, OCSPException,
            CertificateEncodingException {
        DigestCalculatorProvider dgst = new JcaDigestCalculatorProviderBuilder().build();
        DigestCalculator digestCalculator = dgst.get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE));
        return new CertificateID(digestCalculator, new JcaX509CertificateHolder(issuerCert), cert.getSerialNumber());
    }

}
