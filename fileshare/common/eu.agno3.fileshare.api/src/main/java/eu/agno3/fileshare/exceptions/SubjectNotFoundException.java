/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class SubjectNotFoundException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -8562871039941294612L;


    /**
     * 
     */
    public SubjectNotFoundException () {}


    /**
     * @param msg
     * @param t
     */
    public SubjectNotFoundException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public SubjectNotFoundException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public SubjectNotFoundException ( Throwable cause ) {
        super(cause);
    }

}
