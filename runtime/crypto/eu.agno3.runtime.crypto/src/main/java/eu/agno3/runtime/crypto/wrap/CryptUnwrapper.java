/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.wrap;


import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface CryptUnwrapper {

    /**
     * @param blob
     * @return decrypted data
     * @throws CryptoException
     */
    byte[] unwrap ( CryptBlob blob ) throws CryptoException;

}
