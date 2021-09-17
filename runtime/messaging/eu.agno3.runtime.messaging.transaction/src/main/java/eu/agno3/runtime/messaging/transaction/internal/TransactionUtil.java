/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2015 by mbechler
 */
package eu.agno3.runtime.messaging.transaction.internal;


import javax.jms.JMSException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class TransactionUtil {

    private static final Logger log = Logger.getLogger(TransactionUtil.class);


    /**
    * 
    */
    private TransactionUtil () {}


    /**
     * @param tm
     * @param timeout
     * 
     */
    public static void setTransactionTimeout ( TransactionManager tm, int timeout ) {
        try {
            tm.setTransactionTimeout( ( timeout / 1000 ) + 1);
        }
        catch ( SystemException e ) {
            log.error("Failed to set transaction timeout:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param tm
     * @param commit
     * @throws SystemException
     */
    public static void endTransaction ( TransactionManager tm, boolean commit ) throws SystemException {
        try {
            if ( commit && tm.getStatus() == Status.STATUS_ACTIVE ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Committing transaction " + tm.getTransaction()); //$NON-NLS-1$
                }
                tm.commit();
            }
            else if ( tm.getStatus() != Status.STATUS_NO_TRANSACTION ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Rollback transaction " + tm.getTransaction()); //$NON-NLS-1$
                }
                tm.rollback();
            }
        }
        catch (
            RollbackException |
            HeuristicMixedException |
            HeuristicRollbackException e ) {
            log.warn("Recoverable error while ending transaction:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param tm
     * @throws SystemException
     */
    public static void checkTransactionManager ( TransactionManager tm ) throws SystemException {
        if ( tm.getTransaction() != null ) {
            throw new IllegalStateException("There is a pending transaction for the listener thread which has not been cleaned up"); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @return whether this is caused by an interrupted exception
     */
    public static boolean isInterrupted ( JMSException e ) {
        return e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof InterruptedException;
    }


    /**
     * @param e
     * @return whether this a fatal XA exception
     */
    public static boolean isFatalXAException ( JMSException e ) {
        if ( e.getCause() == null ) {
            return false;
        }

        if ( "com.atomikos.datasource.ResourceException".equals(e.getCause().getClass().getName()) ) { //$NON-NLS-1$
            return true;
        }

        if ( e.getCause().getCause() == null ) {
            return false;
        }
        return "com.atomikos.datasource.xa.session.InvalidSessionHandleStateException".equals(e.getCause().getCause().getClass().getName()); //$NON-NLS-1$
    }
}
