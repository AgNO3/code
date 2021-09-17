/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.transaction.internal;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.atomikos.datasource.ResourceException;
import com.atomikos.jms.AtomikosConnectionFactoryBean;

import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class XAConnectionFactoryWrapper implements ServiceTrackerCustomizer<XAConnectionFactory, ServiceRegistration<ConnectionFactory>> {

    private static final Logger log = Logger.getLogger(XAConnectionFactoryWrapper.class);

    static final String[] COPY_PROPERTIES = new String[] {
        "type", //$NON-NLS-1$
        "url", //$NON-NLS-1$
        "user" //$NON-NLS-1$
    };

    private ServiceTracker<XAConnectionFactory, ServiceRegistration<ConnectionFactory>> tracker;

    private ComponentContext componentContext;
    private TransactionManager tm;


    @Reference
    protected synchronized void setTransactionManager ( TransactionManager t ) {
        this.tm = t;
    }


    protected synchronized void unsetTransactionManager ( TransactionManager t ) {
        if ( this.tm == t ) {
            this.tm = null;
        }
    }


    /**
     * @return the tm
     */
    public TransactionManager getTransationManager () {
        return this.tm;
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting ConnectionFactory wrapper for transactional ConnectionFactories"); //$NON-NLS-1$
        this.componentContext = context;
        this.tracker = new ServiceTracker<>(context.getBundleContext(), XAConnectionFactory.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.tracker = null;
        this.componentContext = null;
    }


    private static Dictionary<String, Object> copyProperties ( ServiceReference<XAConnectionFactory> reference ) {
        Dictionary<String, Object> dsProperties = new Hashtable<>();

        for ( String prop : COPY_PROPERTIES ) {
            if ( reference.getProperty(prop) != null ) {
                dsProperties.put(prop, reference.getProperty(prop));
            }
        }
        return dsProperties;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ServiceRegistration<ConnectionFactory> addingService ( ServiceReference<XAConnectionFactory> reference ) {

        String brokerUrl = (String) reference.getProperty("url"); //$NON-NLS-1$

        if ( brokerUrl == null ) {
            log.error("ConnectionFactory without an URL set, ignoring"); //$NON-NLS-1$
            return null;
        }

        XAConnectionFactory xaCf = this.componentContext.getBundleContext().getService(reference);

        AtomikosConnectionFactoryBean cf = new AtomikosConnectionFactoryBean();
        cf.setXaConnectionFactory(xaCf);
        String resName = (String) reference.getProperty("resourceName"); //$NON-NLS-1$
        if ( StringUtils.isBlank(resName) ) {
            resName = String.format("jms/%s", brokerUrl); //$NON-NLS-1$
            resName = resName.substring(0, Math.min(45, resName.length()));
        }
        cf.setUniqueResourceName(resName);
        cf.setMinPoolSize(1);

        int maxPoolSize = 30;
        Object maxPoolSizeProperty = reference.getProperty("maxPoolSize"); //$NON-NLS-1$
        if ( maxPoolSizeProperty != null ) {
            maxPoolSize = (int) maxPoolSizeProperty;
        }
        cf.setMaxPoolSize(maxPoolSize);

        int timeout = 10;
        Object borrowConnectionTimeoutProperty = reference.getProperty("borrowConnectionTimeoutProperty"); //$NON-NLS-1$
        if ( borrowConnectionTimeoutProperty != null ) {
            timeout = (int) borrowConnectionTimeoutProperty;
        }
        cf.setBorrowConnectionTimeout(timeout);

        if ( log.isDebugEnabled() ) {
            log.debug("Wrapping ConnectionFactory for broker " + brokerUrl); //$NON-NLS-1$
        }

        try {
            cf.init();
        }
        catch ( JMSException e ) {
            log.error("Failed to initialize XAConnectionFactory", e); //$NON-NLS-1$
        }

        return DsUtil.registerSafe(this.componentContext, ConnectionFactory.class, cf, copyProperties(reference));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<XAConnectionFactory> reference, ServiceRegistration<ConnectionFactory> service ) {
        if ( service != null ) {
            service.setProperties(copyProperties(reference));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<XAConnectionFactory> reference, ServiceRegistration<ConnectionFactory> reg ) {
        if ( reg != null ) {
            AtomikosConnectionFactoryBean cf = (AtomikosConnectionFactoryBean) this.componentContext.getBundleContext()
                    .getService(reg.getReference());
            if ( log.isDebugEnabled() ) {
                log.debug("Closing connection factory " + cf.getUniqueResourceName()); //$NON-NLS-1$
            }
            DsUtil.unregisterSafe(this.componentContext, reg);
            try {
                cf.close();
            }
            catch ( ResourceException e ) {
                log.debug("Already closed", e); //$NON-NLS-1$
            }
        }
    }

}
