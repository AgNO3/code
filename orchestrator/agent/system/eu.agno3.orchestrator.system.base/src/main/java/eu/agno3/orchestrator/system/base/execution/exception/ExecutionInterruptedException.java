/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.exception;


/**
 * @author mbechler
 * 
 */
public class ExecutionInterruptedException extends ExecutionException {

    /**
     * 
     */
    private static final long serialVersionUID = -7314700318604582317L;


    /**
     * 
     */
    public ExecutionInterruptedException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ExecutionInterruptedException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ExecutionInterruptedException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ExecutionInterruptedException ( Throwable t ) {
        super(t);
    }

}
