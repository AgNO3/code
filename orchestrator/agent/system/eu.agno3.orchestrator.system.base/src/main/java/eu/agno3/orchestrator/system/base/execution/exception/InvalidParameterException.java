/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.exception;


/**
 * @author mbechler
 * 
 */
public class InvalidParameterException extends InvalidUnitConfigurationException {

    /**
     * 
     */
    private static final long serialVersionUID = 5629826460923056048L;


    /**
     * 
     */
    public InvalidParameterException () {}


    /**
     * @param msg
     */
    public InvalidParameterException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public InvalidParameterException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public InvalidParameterException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
