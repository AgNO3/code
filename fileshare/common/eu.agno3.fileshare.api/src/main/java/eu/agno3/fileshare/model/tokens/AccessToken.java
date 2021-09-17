/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.tokens;


import java.io.Externalizable;
import java.io.Serializable;

import eu.agno3.runtime.util.serialization.SafeSerializable;


/**
 * @author mbechler
 *
 */
public interface AccessToken extends SafeSerializable, Externalizable {

    /**
     * @return whether the user has explicitly requested the operation
     */
    boolean isWithIntent ();


    /**
     * @return the session id that accepted the operation
     */
    Serializable getWithIntentSessionId ();

}
