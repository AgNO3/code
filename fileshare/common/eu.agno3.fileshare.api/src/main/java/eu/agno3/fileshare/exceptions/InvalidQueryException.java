/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class InvalidQueryException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -5071846505760353991L;


    /**
     * 
     */
    public InvalidQueryException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public InvalidQueryException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public InvalidQueryException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public InvalidQueryException ( Throwable cause ) {
        super(cause);
    }

}
