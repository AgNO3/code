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
public class StructureException extends EntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 9054776710469663841L;


    /**
     * 
     */
    public StructureException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public StructureException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public StructureException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public StructureException ( Throwable cause ) {
        super(cause);
    }

}
