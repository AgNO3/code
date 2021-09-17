/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.net.ssl.TrustManagerFactory;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;

import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustManagerRegistration.class, immediate = true )
public class TrustManagerRegistration extends AbstractTrustManagerRegistration<TrustConfiguration> {

    private ComponentContext componentContext;
    private ServiceTracker<TrustConfiguration, ServiceRegistration<TrustManagerFactory>> serviceTracker;


    @Override
    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory pf ) {
        super.setPKIXParameterFactory(pf);
    }


    @Override
    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory pf ) {
        super.unsetPKIXParameterFactory(pf);
    }


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        this.serviceTracker = new ServiceTracker<>(ctx.getBundleContext(), TrustConfiguration.class, this);
        this.serviceTracker.open();
    }


    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.serviceTracker.close();
        this.serviceTracker = null;
        this.componentContext = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ServiceRegistration<TrustManagerFactory> addingService ( ServiceReference<TrustConfiguration> cfgRef ) {
        return registerTrustManagerFactory(this.componentContext, this.componentContext.getBundleContext().getService(cfgRef));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.internal.AbstractTrustManagerRegistration#modifiedService(org.osgi.framework.ServiceReference,
     *      org.osgi.framework.ServiceRegistration)
     */
    @Override
    public void modifiedService ( ServiceReference<TrustConfiguration> cfgRef, ServiceRegistration<TrustManagerFactory> ref ) {
        TrustConfiguration cfg = this.componentContext.getBundleContext().getService(cfgRef);
        if ( cfg == null ) {
            return;
        }
        // trigger refresh
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("instanceId", cfg.getId()); //$NON-NLS-1$
        ref.setProperties(props);
    }
}
