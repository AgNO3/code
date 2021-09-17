/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.security.Provider;


/**
 * @author mbechler
 *
 */
public class OpenSSLProvider extends Provider {

    /**
     * 
     */
    private static final String PROVIDER_NAME = "OpenSSL"; //$NON-NLS-1$


    /**
     * @param name
     * @param version
     * @param info
     */
    protected OpenSSLProvider () {
        super(PROVIDER_NAME, 0.1, "OpenSSL integration"); //$NON-NLS-1$
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7051717562993654948L;

}
