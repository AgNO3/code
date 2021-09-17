/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.12.2014 by mbechler
 */
package eu.agno3.runtime.transaction;


/**
 * @author mbechler
 *
 */
public class InternalTransactionException extends RuntimeException {

    /**
     * 
     */
    public InternalTransactionException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public InternalTransactionException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public InternalTransactionException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public InternalTransactionException ( Throwable t ) {
        super(t);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8259620838302009450L;

}
