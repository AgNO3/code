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
public class EntityException extends FileshareException {

    /**
     * 
     */
    public EntityException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public EntityException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public EntityException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public EntityException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -7261757172664405435L;

}
