/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service;


/**
 * @author mbechler
 *
 */
public class ServiceManagementException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4183928092275252217L;


    /**
     * 
     */
    public ServiceManagementException () {}


    /**
     * @param message
     */
    public ServiceManagementException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ServiceManagementException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public ServiceManagementException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
