/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import java.io.IOException;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface CredentialUnwrapper {

    /**
     * @param cr
     * @return unwrapped credentials
     * @throws CryptoException
     * @throws IOException
     */
    UnwrappedCredentials unwrap ( WrappedCredentials cr ) throws CryptoException, IOException;

}
