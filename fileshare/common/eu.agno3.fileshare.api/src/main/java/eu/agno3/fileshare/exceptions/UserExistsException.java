/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class UserExistsException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 6241730652285748723L;


    /**
     * 
     */
    public UserExistsException () {}


    /**
     * @param msg
     * @param t
     */
    public UserExistsException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public UserExistsException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public UserExistsException ( Throwable cause ) {
        super(cause);
    }

}
