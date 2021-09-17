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
public class UnitInitializationFailedException extends ExecutionException {

    /**
     * 
     */
    private static final long serialVersionUID = 4645130099269351214L;


    /**
     * 
     */
    public UnitInitializationFailedException () {}


    /**
     * @param msg
     */
    public UnitInitializationFailedException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public UnitInitializationFailedException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public UnitInitializationFailedException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
