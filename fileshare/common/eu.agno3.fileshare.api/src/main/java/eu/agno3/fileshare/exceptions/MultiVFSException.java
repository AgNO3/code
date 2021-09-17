/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class MultiVFSException extends EntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 7015162524079129661L;


    /**
     * 
     */
    public MultiVFSException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public MultiVFSException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public MultiVFSException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public MultiVFSException ( Throwable cause ) {
        super(cause);
    }

}
