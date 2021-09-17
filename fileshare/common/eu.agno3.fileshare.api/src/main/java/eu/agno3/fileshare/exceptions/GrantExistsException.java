/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class GrantExistsException extends EntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 1909645086937367029L;


    /**
     * 
     */
    public GrantExistsException () {}


    /**
     * @param msg
     * @param t
     */
    public GrantExistsException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public GrantExistsException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public GrantExistsException ( Throwable cause ) {
        super(cause);
    }

}
