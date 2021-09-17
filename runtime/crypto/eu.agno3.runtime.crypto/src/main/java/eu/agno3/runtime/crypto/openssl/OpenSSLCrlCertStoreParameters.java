/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.File;
import java.security.cert.CertStoreParameters;


/**
 * @author mbechler
 *
 */
public class OpenSSLCrlCertStoreParameters implements CertStoreParameters {

    private File crlDir;


    /**
     * @param crlDir
     */
    public OpenSSLCrlCertStoreParameters ( File crlDir ) {
        this.crlDir = crlDir;
    }


    @Override
    public OpenSSLCrlCertStoreParameters clone () {
        return new OpenSSLCrlCertStoreParameters(this.getCRLDir());
    }


    /**
     * @return the CRL directory
     */
    public File getCRLDir () {
        return this.crlDir;
    }
}
