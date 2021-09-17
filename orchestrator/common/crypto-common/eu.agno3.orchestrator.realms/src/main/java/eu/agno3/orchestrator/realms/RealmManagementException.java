/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


/**
 * @author mbechler
 *
 */
public class RealmManagementException extends Exception {

    /**
     * 
     */
    public RealmManagementException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public RealmManagementException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public RealmManagementException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public RealmManagementException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 9085232030570114398L;

}
