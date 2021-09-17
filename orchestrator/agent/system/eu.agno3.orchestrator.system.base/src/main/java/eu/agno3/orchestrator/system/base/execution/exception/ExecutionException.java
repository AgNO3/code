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
public class ExecutionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4450881042233719516L;


    /**
     * 
     */
    public ExecutionException () {}


    /**
     * @param msg
     */
    public ExecutionException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ExecutionException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public ExecutionException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
