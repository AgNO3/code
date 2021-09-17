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
public class GroupCyclicException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 1835661030410299039L;


    /**
     * 
     */
    public GroupCyclicException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public GroupCyclicException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public GroupCyclicException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public GroupCyclicException ( Throwable cause ) {
        super(cause);
    }

}
