/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing;


import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * 
 */
public interface MessageSource extends @NonNull Serializable {

    /**
     * @return the message source encoded as a string
     */
    String encode ();


    /**
     * @param encoded
     */
    void parse ( String encoded );
}
