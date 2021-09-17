/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.crypto.tls.TLSContextProvider;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = TLSContextRegistration.class, immediate = true )
public class TLSContextRegistration implements ServiceTrackerCustomizer<DelegatingTLSConfiguration, ServiceRegistration<TLSContext>> {

    private static final Logger log = Logger.getLogger(TLSContextRegistration.class);
    private ComponentContext componentContext;
    private ServiceTracker<DelegatingTLSConfiguration, ServiceRegistration<TLSContext>> serviceTracker;

    private TLSContextProvider contextProvider;


    @Reference
    protected synchronized void setTLSContextProvider ( TLSContextProvider tcp ) {
        this.contextProvider = tcp;
    }


    protected synchronized void unsetTLSContextProvider ( TLSContextProvider tcp ) {
        if ( this.contextProvider == tcp ) {
            this.contextProvider = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        this.serviceTracker = new ServiceTracker<>(ctx.getBundleContext(), DelegatingTLSConfiguration.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.serviceTracker != null ) {
            this.serviceTracker.close();
            this.serviceTracker = null;
        }
        this.componentContext = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ServiceRegistration<TLSContext> addingService ( ServiceReference<DelegatingTLSConfiguration> ref ) {
        DelegatingTLSConfiguration cfg = this.componentContext.getBundleContext().getService(ref);
        if ( cfg == null ) {
            return null;
        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Registering TLS context " + cfg.getId()); //$NON-NLS-1$
            }
            Dictionary<String, Object> props = DelegatingTLSConfigurationRegistration.copyProperties(ref);
            TLSContext context = this.contextProvider.getContext(cfg);

            int prio = cfg.getPriority();
            props.put(Constants.SERVICE_RANKING, prio);
            return DsUtil.registerSafe(this.componentContext, TLSContext.class, context, props);
        }
        catch ( CryptoException e ) {
            log.warn("Failed to create TLS context " + cfg.getId(), e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<DelegatingTLSConfiguration> ref, ServiceRegistration<TLSContext> reg ) {

        if ( reg != null ) {
            DelegatingTLSConfiguration cfg = this.componentContext.getBundleContext().getService(ref);
            if ( cfg == null ) {
                return;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Updating TLS context " + cfg.getId()); //$NON-NLS-1$
            }
            TLSContext ctx = this.componentContext.getBundleContext().getService(reg.getReference());

            try {
                this.contextProvider.update(cfg, ctx);
            }
            catch ( CryptoException e ) {
                log.warn("Failed to update TLS context " + cfg.getId(), e); //$NON-NLS-1$
                return;
            }

            reg.setProperties(DelegatingTLSConfigurationRegistration.copyProperties(ref));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<DelegatingTLSConfiguration> ref, ServiceRegistration<TLSContext> reg ) {
        if ( reg != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Removing TLS context " + reg); //$NON-NLS-1$
            }
            DsUtil.unregisterSafe(this.componentContext, reg);
        }
    }

}
