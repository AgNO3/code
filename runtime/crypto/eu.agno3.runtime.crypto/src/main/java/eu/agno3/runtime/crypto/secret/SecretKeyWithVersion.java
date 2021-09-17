/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2015 by mbechler
 */
package eu.agno3.runtime.crypto.secret;


import javax.crypto.SecretKey;


/**
 * @author mbechler
 *
 */
public interface SecretKeyWithVersion extends SecretKey {

    /**
     * @param algo
     * @return a cloned key with the specified algo
     */
    SecretKeyWithVersion withAlgo ( String algo );


    /**
     * @return the key version
     */
    int getVersion ();

}
