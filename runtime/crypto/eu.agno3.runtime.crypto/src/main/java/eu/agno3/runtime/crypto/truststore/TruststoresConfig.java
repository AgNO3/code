/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore;


import java.io.File;


/**
 * @author mbechler
 *
 */
public interface TruststoresConfig {

    /**
     * 
     */
    static final String PID = "truststores"; //$NON-NLS-1$


    /**
     * @return the keystore directory
     */
    File getTruststoreBaseDirectory ();
}
