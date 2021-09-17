/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;


/**
 * @author mbechler
 *
 */
public final class TruststoreUtil {

    /**
     * 
     */
    private TruststoreUtil () {}


    /**
     * @param cert
     * @return
     * @throws TruststoreManagerException
     * @throws KeyStoreException
     * @throws CertificateEncodingException
     */
    static String deriveCertAlias ( X509Certificate cert, KeyStore ks ) throws TruststoreManagerException, KeyStoreException,
            CertificateEncodingException {
        X500Name dn = new JcaX509CertificateHolder(cert).getSubject();
        RDN[] cns = dn.getRDNs(BCStyle.CN);
        RDN[] ous = dn.getRDNs(BCStyle.OU);
        RDN[] os = dn.getRDNs(BCStyle.O);

        String toUse = null;

        if ( isValidRDN(cns) ) {
            toUse = cns[ 0 ].getFirst().getValue().toString();
        }
        else if ( isValidRDN(ous) ) {
            toUse = ous[ 0 ].getFirst().getValue().toString();
        }
        else if ( isValidRDN(os) ) {
            toUse = os[ 0 ].getFirst().getValue().toString();
        }
        else {
            throw new TruststoreManagerException("Failed to get name for alias generation"); //$NON-NLS-1$
        }

        toUse = StringUtils.replaceChars(toUse, ' ', '_');
        toUse = toUse.replaceAll("[^a-zA-Z0-9_]+", StringUtils.EMPTY); //$NON-NLS-1$
        toUse = toUse.substring(0, Math.min(32, toUse.length()));

        for ( int i = 0; i < 255; i++ ) {
            String alias = String.format("%s.%d", toUse, i); //$NON-NLS-1$

            if ( !ks.containsAlias(alias) ) {
                return alias;
            }
        }

        throw new TruststoreManagerException("Too many entries for alias " + toUse); //$NON-NLS-1$
    }


    private static boolean isValidRDN ( RDN[] cns ) {
        return cns != null && cns.length == 1 && !cns[ 0 ].isMultiValued();
    }

}
