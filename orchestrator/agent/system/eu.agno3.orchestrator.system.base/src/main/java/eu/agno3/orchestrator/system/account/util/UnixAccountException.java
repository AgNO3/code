/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.system.account.util;


/**
 * @author mbechler
 *
 */
public class UnixAccountException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7001015829940766500L;


    /**
     * 
     */
    public UnixAccountException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public UnixAccountException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public UnixAccountException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public UnixAccountException ( Throwable t ) {
        super(t);
    }

}
