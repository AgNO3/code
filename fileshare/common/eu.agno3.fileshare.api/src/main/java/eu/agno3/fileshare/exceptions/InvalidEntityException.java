/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class InvalidEntityException extends EntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 8921138376763303354L;


    /**
     * 
     */
    public InvalidEntityException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public InvalidEntityException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public InvalidEntityException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public InvalidEntityException ( Throwable cause ) {
        super(cause);
    }

}
