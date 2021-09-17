/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2013 by mbechler
 */
package eu.agno3.runtime.transaction.internal;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.transaction.TransactionService;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * Register the transaction manager for each available transaction service
 * 
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class TransactionManagerRegistration implements ServiceTrackerCustomizer<TransactionService, ServiceRegistration<TransactionManager>> {

    private static final Logger log = Logger.getLogger(TransactionManagerRegistration.class);

    ServiceTracker<TransactionService, ServiceRegistration<TransactionManager>> tracker;

    private ComponentContext componentContext;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        this.componentContext = context;
        this.tracker = new ServiceTracker<>(context.getBundleContext(), TransactionService.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.tracker = null;
        this.componentContext = null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ServiceRegistration<TransactionManager> addingService ( ServiceReference<TransactionService> reference ) {
        TransactionService ts = this.componentContext.getBundleContext().getService(reference);
        if ( log.isDebugEnabled() ) {
            log.debug("Registering TransactionManager for " + ts.getClass().getName()); //$NON-NLS-1$
        }
        TransactionManager tm = ts.getTransactionManager();
        Dictionary<String, Object> tmProperties = new Hashtable<>();
        tmProperties.put("transaction.service.class", ts.getClass().getName()); //$NON-NLS-1$
        return DsUtil.registerSafe(this.componentContext, TransactionManager.class, tm, tmProperties);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<TransactionService> reference, ServiceRegistration<TransactionManager> service ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<TransactionService> reference, ServiceRegistration<TransactionManager> reg ) {
        if ( reg != null ) {
            DsUtil.unregisterSafe(this.componentContext, reg);
        }
    }

}
