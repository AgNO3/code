/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import java.io.IOException;
import java.security.PublicKey;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface CredentialWrapper {

    /**
     * @param creds
     * @param recipients
     * @return encrypted credentials
     * @throws CryptoException
     * @throws IOException
     */
    WrappedCredentials wrap ( UnwrappedCredentials creds, PublicKey... recipients ) throws CryptoException, IOException;

}
