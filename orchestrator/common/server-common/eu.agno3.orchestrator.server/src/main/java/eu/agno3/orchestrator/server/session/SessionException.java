/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.session;


/**
 * @author mbechler
 *
 */
public class SessionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1046574652995151747L;


    /**
     * 
     */
    public SessionException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public SessionException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public SessionException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public SessionException ( Throwable t ) {
        super(t);
    }

}
