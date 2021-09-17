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
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;


/**
 * @author mbechler
 *
 */
public class CertificatePoliciesFormatter implements X509ExtensionFormatter {

    private static final Logger log = Logger.getLogger(CertificatePoliciesFormatter.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.crypto.X509ExtensionFormatter#format(byte[])
     */
    @Override
    public String format ( byte[] data ) {
        StringBuilder sb = new StringBuilder();
        sb.append("certificatePolicies:"); //$NON-NLS-1$
        CertificatePolicies policyExt;
        try {
            ASN1Primitive ext = JcaX509ExtensionUtils.parseExtensionValue(data);
            if ( ext instanceof ASN1Sequence && ( (ASN1Sequence) ext ).size() == 0 ) {
                sb.append("<empty>"); //$NON-NLS-1$
                return sb.toString();
            }
            policyExt = CertificatePolicies.getInstance(ext);
        }
        catch ( IOException e ) {
            log.warn("Failed to parse certificatePolicies", e); //$NON-NLS-1$
            return sb.toString();
        }
        sb.append(System.lineSeparator());

        for ( PolicyInformation info : policyExt.getPolicyInformation() ) {
            sb.append("  "); //$NON-NLS-1$
            sb.append(info.getPolicyIdentifier().getId());
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

}
