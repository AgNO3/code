/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jdt.annotation.NonNull;


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
     * 
     * @param cert
     * @return canonical truststore library certificate path
     */
    @SuppressWarnings ( "null" )
    public static @NonNull String makeCertificatePath ( X509Certificate cert ) {
        String dn = cert.getSubjectX500Principal().getName(X500Principal.RFC2253).replace('/', ' ');
        return String.format(
            "%s-%s.crt", //$NON-NLS-1$
            dn.substring(0, Math.min(120, dn.length())),
            Hex.encodeHexString(cert.getSerialNumber().toByteArray()));
    }
}
