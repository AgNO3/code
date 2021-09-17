/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.ntlm;


import eu.agno3.runtime.net.ad.ADUserInfo;


/**
 * @author mbechler
 *
 */
public interface NTLMContext {

    /**
     * @return the connection identifier
     */
    long getConnectionId ();


    /**
     * @return whether the context is established
     */
    boolean isComplete ();


    /**
     * @return the user information
     */
    ADUserInfo getUserInfo ();

}
