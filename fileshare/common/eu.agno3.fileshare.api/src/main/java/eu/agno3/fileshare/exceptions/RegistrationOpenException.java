/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class RegistrationOpenException extends FileshareException {

    /**
     * 
     */
    public RegistrationOpenException () {}


    /**
     * @param msg
     * @param t
     */
    public RegistrationOpenException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public RegistrationOpenException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public RegistrationOpenException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6974000678545798013L;

}
