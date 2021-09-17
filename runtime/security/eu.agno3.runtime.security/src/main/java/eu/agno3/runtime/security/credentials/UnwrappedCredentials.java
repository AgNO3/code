/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import java.io.IOException;


/**
 * @author mbechler
 *
 */

public interface UnwrappedCredentials {

    /**
     * 
     * @return type of these credentials
     */
    CredentialType getType ();


    /**
     * 
     * @return byte representation, first byte has to be type code
     * @throws IOException
     */
    byte[] encode () throws IOException;
}
