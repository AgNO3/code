/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class NotificationException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -8732200611290118435L;


    /**
     * 
     */
    public NotificationException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public NotificationException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public NotificationException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public NotificationException ( Throwable cause ) {
        super(cause);
    }

}