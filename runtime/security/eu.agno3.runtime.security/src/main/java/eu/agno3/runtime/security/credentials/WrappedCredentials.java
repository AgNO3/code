/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import eu.agno3.runtime.crypto.wrap.CryptBlob;


/**
 * @author mbechler
 *
 */
public class WrappedCredentials extends AdaptedCryptBlob {

    /**
     * 
     */
    private static final long serialVersionUID = -8601965765184054710L;


    /**
     * 
     * @param cb
     * @return adapted crypto blob
     */
    public static WrappedCredentials fromCryptBlob ( CryptBlob cb ) {
        WrappedCredentials ad = new WrappedCredentials();
        fromBlob(cb, ad);
        return ad;
    }
}
