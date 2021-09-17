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
public class AccessDeniedException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 9014865276582599525L;


    /**
     * 
     */
    public AccessDeniedException () {}


    /**
     * @param msg
     * @param t
     */
    public AccessDeniedException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AccessDeniedException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public AccessDeniedException ( Throwable cause ) {
        super(cause);
    }

}
