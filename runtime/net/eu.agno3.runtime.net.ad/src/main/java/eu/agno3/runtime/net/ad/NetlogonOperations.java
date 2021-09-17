/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import jcifs.ntlmssp.Type3Message;
import jcifs.pac.Pac;


/**
 * @author mbechler
 *
 */
public interface NetlogonOperations {

    /**
     * @param serverChallenge
     * @param auth
     * @return the user info
     * @throws ADException
     */
    ADUserInfo ntlmValidate ( byte[] serverChallenge, Type3Message auth ) throws ADException;


    /**
     * @param pac
     * @return the user info
     * @throws ADException
     */
    ADUserInfo pacValidate ( Pac pac ) throws ADException;

}
