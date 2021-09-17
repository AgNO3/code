/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm;


import javax.persistence.PersistenceException;


/**
 * @author mbechler
 *
 */
public class EntityTransactionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4987556904943113461L;


    /**
     * 
     */
    public EntityTransactionException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public EntityTransactionException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public EntityTransactionException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public EntityTransactionException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @return runtime exception
     */
    public RuntimeException runtime () {
        return new PersistenceException("Transaction failure", this); //$NON-NLS-1$
    }

}
