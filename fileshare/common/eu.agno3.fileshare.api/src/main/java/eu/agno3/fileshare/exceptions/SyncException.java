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
public class SyncException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -7343170841359535296L;


    /**
     * 
     */
    public SyncException () {}


    /**
     * @param msg
     * @param t
     */
    public SyncException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public SyncException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public SyncException ( Throwable cause ) {
        super(cause);
    }

}
