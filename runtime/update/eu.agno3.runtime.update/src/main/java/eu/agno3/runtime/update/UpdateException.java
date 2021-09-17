/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2014 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public class UpdateException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1962254981599354433L;


    /**
     * 
     */
    public UpdateException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public UpdateException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public UpdateException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public UpdateException ( Throwable t ) {
        super(t);
    }

}
