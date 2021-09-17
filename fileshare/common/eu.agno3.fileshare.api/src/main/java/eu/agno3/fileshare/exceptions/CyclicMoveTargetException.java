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
public class CyclicMoveTargetException extends StructureException {

    /**
     * 
     */
    private static final long serialVersionUID = -4796387743704304932L;


    /**
     * 
     */
    public CyclicMoveTargetException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public CyclicMoveTargetException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public CyclicMoveTargetException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public CyclicMoveTargetException ( Throwable cause ) {
        super(cause);
    }

}
