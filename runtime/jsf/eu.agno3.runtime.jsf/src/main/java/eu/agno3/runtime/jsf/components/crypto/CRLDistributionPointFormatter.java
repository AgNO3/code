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
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;


/**
 * @author mbechler
 *
 */
public class CRLDistributionPointFormatter implements X509ExtensionFormatter {

    private static final Logger log = Logger.getLogger(CRLDistributionPointFormatter.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.crypto.X509ExtensionFormatter#format(byte[])
     */
    @Override
    public String format ( byte[] data ) {

        StringBuilder sb = new StringBuilder();
        sb.append("crlDistributionPoints:"); //$NON-NLS-1$
        CRLDistPoint dps;
        try {
            ASN1Primitive ext = JcaX509ExtensionUtils.parseExtensionValue(data);
            if ( ext instanceof ASN1Sequence && ( (ASN1Sequence) ext ).size() == 0 ) {
                sb.append("<empty>"); //$NON-NLS-1$
                return sb.toString();
            }
            dps = CRLDistPoint.getInstance(ext);
        }
        catch ( IOException e ) {
            log.warn("Failed to parse CRLDps", e); //$NON-NLS-1$
            return sb.toString();
        }
        sb.append(System.lineSeparator());

        boolean firstDp = true;

        for ( DistributionPoint dp : dps.getDistributionPoints() ) {

            if ( firstDp ) {
                firstDp = false;
            }
            else {
                sb.append(System.lineSeparator());
            }

            DistributionPointName dpName = dp.getDistributionPoint();
            GeneralNames names = GeneralNames.getInstance(dpName.getName());

            for ( GeneralName gn : names.getNames() ) {
                sb.append("  "); //$NON-NLS-1$
                if ( dpName.getType() == DistributionPointName.FULL_NAME ) {
                    sb.append(CertificateUtil.formatGeneralName(gn));
                }
                else {
                    sb.append(String.format("%d:%s", dpName.getType(), gn.getName())); //$NON-NLS-1$
                }
                sb.append(System.lineSeparator());
            }

            if ( dp.getCRLIssuer() != null ) {
                sb.append("  issuers=["); //$NON-NLS-1$
                boolean firstName = true;
                for ( GeneralName n : dp.getCRLIssuer().getNames() ) {
                    if ( firstName ) {
                        firstName = false;
                    }
                    else {
                        sb.append("; "); //$NON-NLS-1$
                    }
                    sb.append(CertificateUtil.formatGeneralName(n));
                }
                sb.append("]"); //$NON-NLS-1$
            }

            if ( dp.getReasons() != null ) {
                sb.append("  reasons="); //$NON-NLS-1$
                sb.append(dp.getReasons().intValue());
            }

        }
        return sb.toString();
    }
}
