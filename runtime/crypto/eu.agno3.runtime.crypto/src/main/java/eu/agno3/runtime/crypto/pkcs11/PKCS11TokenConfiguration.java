/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs11;


import java.security.AuthProvider;


/**
 * @author mbechler
 *
 */
public interface PKCS11TokenConfiguration {

    /**
     * 
     */
    static final String PID = "pkcs11.token"; //$NON-NLS-1$


    /**
     * 
     * @return the instance identifier
     */
    String getInstanceId ();


    /**
     * 
     * @return the PKCS11 library used to access the token
     */
    String getLibrary ();


    /**
     * 
     * @return the token PIN
     */
    String getPIN ();


    /**
     * 
     * @return the slot index (only used if slot id is not set, default 0)
     */
    int getSlotIndex ();


    /**
     * 
     * @return the slot id, or null if slot index should be used
     */
    String getSlotId ();


    /**
     * 
     * @return extra configuration in java pkcs11 config format, appended to basic config
     */
    String getExtraConfig ();


    /**
     * @return a PKCS11 provider for this token
     */
    AuthProvider getProvider ();


    /**
     * @return arguments passed via pReserved
     */
    String getInitArgs ();
}
