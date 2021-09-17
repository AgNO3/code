/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class EntityNotFoundException extends InvalidEntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 3047057251535087115L;


    /**
     * 
     */
    public EntityNotFoundException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public EntityNotFoundException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public EntityNotFoundException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public EntityNotFoundException ( Throwable cause ) {
        super(cause);
    }

}
