/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class SecurityException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -1141340823144383351L;


    /**
     * 
     */
    public SecurityException () {}


    /**
     * @param msg
     * @param t
     */
    public SecurityException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public SecurityException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public SecurityException ( Throwable cause ) {
        super(cause);
    }

}
