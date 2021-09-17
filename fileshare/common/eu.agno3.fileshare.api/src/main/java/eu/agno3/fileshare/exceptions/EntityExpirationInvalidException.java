/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class EntityExpirationInvalidException extends InvalidEntityException {

    /**
     * 
     */
    private static final long serialVersionUID = -7303300189738065157L;


    /**
     * 
     */
    public EntityExpirationInvalidException () {}


    /**
     * @param msg
     * @param t
     */
    public EntityExpirationInvalidException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public EntityExpirationInvalidException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public EntityExpirationInvalidException ( Throwable cause ) {
        super(cause);
    }

}
