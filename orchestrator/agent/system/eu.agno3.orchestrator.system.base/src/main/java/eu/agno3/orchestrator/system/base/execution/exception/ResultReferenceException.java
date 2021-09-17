/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.exception;


/**
 * @author mbechler
 * 
 */
public class ResultReferenceException extends ExecutionException {

    /**
     * 
     */
    private static final long serialVersionUID = 6800355809216508612L;


    /**
     * 
     */
    public ResultReferenceException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ResultReferenceException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ResultReferenceException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ResultReferenceException ( Throwable t ) {
        super(t);
    }

}
