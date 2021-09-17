/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


/**
 * @author mbechler
 * 
 */
public class BaseSystemException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8439730263016341604L;


    /**
     * 
     */
    public BaseSystemException () {}


    /**
     * @param m
     */
    public BaseSystemException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public BaseSystemException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public BaseSystemException ( String m, Throwable t ) {
        super(m, t);
    }

}
