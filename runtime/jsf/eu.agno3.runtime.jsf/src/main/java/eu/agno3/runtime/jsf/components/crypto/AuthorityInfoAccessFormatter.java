/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;


/**
 * @author mbechler
 *
 */
public class AuthorityInfoAccessFormatter implements X509ExtensionFormatter {

    private static final Logger log = Logger.getLogger(AuthorityInfoAccessFormatter.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.crypto.X509ExtensionFormatter#format(byte[])
     */
    @Override
    public String format ( byte[] data ) {
        StringBuilder sb = new StringBuilder();
        sb.append("authorityInformationAccess:"); //$NON-NLS-1$
        AuthorityInformationAccess aia;
        try {
            ASN1Primitive ext = JcaX509ExtensionUtils.parseExtensionValue(data);
            if ( ext instanceof ASN1Sequence && ( (ASN1Sequence) ext ).size() == 0 ) {
                sb.append("<empty>"); //$NON-NLS-1$
                return sb.toString();
            }

            aia = AuthorityInformationAccess.getInstance(ext);
        }
        catch ( IOException e ) {
            log.warn("Failed to parse AIA", e); //$NON-NLS-1$
            return sb.toString();
        }

        sb.append(System.lineSeparator());
        for ( AccessDescription desc : aia.getAccessDescriptions() ) {
            sb.append("  "); //$NON-NLS-1$
            if ( AccessDescription.id_ad_caIssuers.equals(desc.getAccessMethod()) ) {
                sb.append("Certs: "); //$NON-NLS-1$
            }
            else if ( AccessDescription.id_ad_ocsp.equals(desc.getAccessMethod()) ) {
                sb.append("OCSP: "); //$NON-NLS-1$
            }
            else {
                sb.append(desc.getAccessMethod());
            }
            sb.append(desc.getAccessLocation().getName());
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}
