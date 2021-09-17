/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.04.2016 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class InvalidSyncTokenException extends SyncException {

    /**
     * 
     */
    private static final long serialVersionUID = 6724190461147874135L;


    /**
     * 
     */
    public InvalidSyncTokenException () {}


    /**
     * @param msg
     * @param t
     */
    public InvalidSyncTokenException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public InvalidSyncTokenException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public InvalidSyncTokenException ( Throwable cause ) {
        super(cause);
    }

}
