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
public class ShareLifetimeInvalidException extends ShareException {

    /**
     * 
     */
    private static final long serialVersionUID = 3168417754743705997L;


    /**
     * 
     */
    public ShareLifetimeInvalidException () {}


    /**
     * @param msg
     * @param t
     */
    public ShareLifetimeInvalidException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ShareLifetimeInvalidException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public ShareLifetimeInvalidException ( Throwable cause ) {
        super(cause);
    }

}
