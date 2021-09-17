/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;


/**
 * @author mbechler
 *
 */
public class OpenSSLCRLCertStore extends CertStore {

    /**
     * @param storeSpi
     * @param provider
     * @param type
     * @param params
     * @throws InvalidAlgorithmParameterException
     */
    protected OpenSSLCRLCertStore ( CertStoreParameters params ) throws InvalidAlgorithmParameterException {
        super(new OpenSSLCRLCertStoreSPI(params), new OpenSSLProvider(), "OpenSSL", params); //$NON-NLS-1$
    }


    /**
     * @param file
     * @throws InvalidAlgorithmParameterException
     */
    public OpenSSLCRLCertStore ( File file ) throws InvalidAlgorithmParameterException {
        this(new OpenSSLCrlCertStoreParameters(file));
    }
}
