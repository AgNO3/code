/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore;


import java.io.File;


/**
 * @author mbechler
 *
 */
public interface KeystoresConfig {

    /**
     * 
     */
    static final String PID = "keystores"; //$NON-NLS-1$


    /**
     * @return the keystore directory
     */
    File getKeystoreBaseDirectory ();

}
