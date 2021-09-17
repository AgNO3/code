/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.service;


/**
 * @author mbechler
 * 
 */
public class ServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4094384538011939344L;


    /**
     * 
     */
    public ServiceException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public ServiceException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public ServiceException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public ServiceException ( Throwable t ) {
        super(t);
    }

}
