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
public class RequiredParameterMissingException extends InvalidUnitConfigurationException {

    /**
     * 
     */
    private static final long serialVersionUID = 1365129912674869574L;


    /**
     * 
     */
    public RequiredParameterMissingException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public RequiredParameterMissingException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public RequiredParameterMissingException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public RequiredParameterMissingException ( Throwable t ) {
        super(t);
    }

}
