/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.runtime.licensing.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pkcs7.PKCS7Verifier;
import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicenseParser;
import eu.agno3.runtime.update.LicensingException;
import eu.agno3.runtime.update.UpdateTrustConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = LicenseParser.class )
public class LicenseParserImpl implements LicenseParser {

    private static final Logger log = Logger.getLogger(LicenseParserImpl.class);

    private static final boolean[] EXPECT_KEY_USAGE = new boolean[] {
        true, false, false, false, false, false, false, false, false
    };

    private static final Set<String> EXPECT_EKU = new HashSet<>(Arrays.asList("1.3.6.1.4.1.44756.2.1")); //$NON-NLS-1$

    private UpdateTrustConfiguration updateTrust;
    private PKCS7Verifier pkcs7verifier;


    @Reference
    protected synchronized void setUpdateTrustConfig ( UpdateTrustConfiguration utc ) {
        this.updateTrust = utc;
    }


    protected synchronized void unsetUpdateTrustConfig ( UpdateTrustConfiguration utc ) {
        if ( this.updateTrust == utc ) {
            this.updateTrust = null;
        }
    }


    @Reference
    protected synchronized void setPKCS7Verifier ( PKCS7Verifier p7 ) {
        this.pkcs7verifier = p7;
    }


    protected synchronized void unsetPKCS7Verifier ( PKCS7Verifier p7 ) {
        if ( this.pkcs7verifier == p7 ) {
            this.pkcs7verifier = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicenseParser#parseLicense(java.lang.String)
     */
    @Override
    public License parseLicense ( String licData ) throws LicensingException {
        return parseLicense(Base64.getDecoder().decode(licData));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicenseParser#parseLicense(byte[])
     */
    @Override
    public License parseLicense ( byte[] licDataBytes ) throws LicensingException {
        return parseLicense(new ByteArrayInputStream(licDataBytes));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicenseParser#parseLicense(java.io.InputStream)
     */
    @Override
    public License parseLicense ( InputStream in ) throws LicensingException {
        try {
            CMSSignedData signedData = new CMSSignedData(in);
            Object content = signedData.getSignedContent().getContent();

            Properties props = new Properties();
            if ( content instanceof String ) {
                props.load(new StringReader((String) content));
            }
            else if ( content instanceof byte[] ) {
                props.load(new ByteArrayInputStream((byte[]) content));
            }

            LicenseImpl lic = new LicenseImpl();
            lic.fromProperties(props, signedData.getEncoded());

            this.pkcs7verifier.verify(this.updateTrust, signedData, lic.getIssueDate(), EXPECT_KEY_USAGE, EXPECT_EKU);

            if ( log.isDebugEnabled() ) {
                for ( SignerInformation signerInformation : signedData.getSignerInfos().getSigners() ) {
                    Collection<X509CertificateHolder> matches = signedData.getCertificates().getMatches(signerInformation.getSID());
                    for ( X509CertificateHolder match : matches ) {
                        log.debug(String.format(
                            "Valid signature by %s [serial: %s] %s", //$NON-NLS-1$
                            match.getSubject(),
                            Hex.encodeHexString(match.getSerialNumber().toByteArray()),
                            match.getIssuer()));
                    }
                }
            }

            return lic;
        }
        catch (
            CryptoException |
            IOException |
            CMSException e ) {
            throw new LicensingException("Invalid license", e); //$NON-NLS-1$
        }
    }
}
