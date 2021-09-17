/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class TransactionServiceJtaPlatform extends org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform {

    private static final Logger log = Logger.getLogger(TransactionServiceJtaPlatform.class);

    /**
     * 
     */
    private static final long serialVersionUID = -1980412793533876054L;
    private static TransactionService ts;


    @Reference
    protected synchronized void setTransactionService ( TransactionService t ) {
        log.debug("Transaction service came up"); //$NON-NLS-1$
        ts = t;
    }


    protected synchronized void unsetTransactionService ( TransactionService t ) {
        if ( ts == t ) {
            ts = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.service.jta.platform.internal.AbstractJtaPlatform#locateTransactionManager()
     */
    @Override
    protected TransactionManager locateTransactionManager () {
        if ( ts == null ) {
            throw new HibernateException("Transaction service not initialized"); //$NON-NLS-1$
        }
        return ts.getTransactionManager();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.service.jta.platform.internal.AbstractJtaPlatform#locateUserTransaction()
     */
    @Override
    protected UserTransaction locateUserTransaction () {
        return ts.createUserTransaction();
    }
}
