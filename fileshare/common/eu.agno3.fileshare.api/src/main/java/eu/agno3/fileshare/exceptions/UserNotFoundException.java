/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class UserNotFoundException extends SubjectNotFoundException {

    /**
     * 
     */
    private static final long serialVersionUID = 2676455720023020504L;


    /**
     * 
     */
    public UserNotFoundException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public UserNotFoundException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public UserNotFoundException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public UserNotFoundException ( Throwable cause ) {
        super(cause);
    }

}
