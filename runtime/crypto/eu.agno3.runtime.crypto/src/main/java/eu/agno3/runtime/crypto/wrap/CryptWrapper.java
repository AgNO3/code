/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.wrap;


import java.security.PublicKey;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface CryptWrapper {

    /**
     * 
     * @param data
     * @param recipients
     * @return wrapped blob
     * @throws CryptoException
     */
    CryptBlob wrap ( byte[] data, PublicKey... recipients ) throws CryptoException;
}
