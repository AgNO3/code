/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage;

import eu.agno3.orchestrator.system.info.SystemInformationException;


/**
 * @author mbechler
 * 
 */
public class StorageInformationException extends SystemInformationException {

    /**
     * 
     */
    private static final long serialVersionUID = 694765893553878591L;


    /**
     * 
     */
    public StorageInformationException () {}


    /**
     * @param message
     */
    public StorageInformationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public StorageInformationException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public StorageInformationException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
