/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.exception;


/**
 * @author mbechler
 * 
 */
public class InvalidUnitConfigurationException extends ExecutionException {

    /**
     * 
     */
    private static final long serialVersionUID = 4675923484745994901L;


    /**
     * 
     */
    public InvalidUnitConfigurationException () {}


    /**
     * @param msg
     */
    public InvalidUnitConfigurationException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public InvalidUnitConfigurationException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public InvalidUnitConfigurationException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
