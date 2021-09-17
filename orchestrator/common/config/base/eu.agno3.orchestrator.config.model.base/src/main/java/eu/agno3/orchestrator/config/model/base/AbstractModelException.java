/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractModelException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1119135028205281326L;


    /**
     * 
     */
    public AbstractModelException () {}


    /**
     * @param msg
     * @param t
     */
    public AbstractModelException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AbstractModelException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public AbstractModelException ( Throwable t ) {
        super(t);
    }

}
