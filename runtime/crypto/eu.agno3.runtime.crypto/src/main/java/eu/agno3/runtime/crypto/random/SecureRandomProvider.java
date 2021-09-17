/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.random;


import java.security.SecureRandom;


/**
 * @author mbechler
 *
 */
public interface SecureRandomProvider {

    /**
     * @return a secure random instance
     */
    public SecureRandom getSecureRandom ();

}
