/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.monitor.internal;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.agent.connector.QueueingEventProducer;
import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.monitor.ServiceMonitor;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.orchestrator.system.monitor.msg.ServiceStatusEvent;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ServiceMonitorImpl.class, ServiceMonitor.class, SystemService.class
}, immediate = true )
@SystemServiceType ( ServiceMonitor.class )
public class ServiceMonitorImpl implements ServiceMonitor, Runnable {

    private static final Logger log = Logger.getLogger(ServiceMonitorImpl.class);

    private ServiceManager serviceManager;
    private ConfigRepository configRepository;

    private Map<UUID, ServiceRuntimeStatus> statusCache = new ConcurrentHashMap<>();

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private QueueingEventProducer eventProducer;


    @Reference
    protected synchronized void setEventProducer ( QueueingEventProducer evp ) {
        this.eventProducer = evp;
    }


    protected synchronized void unsetEventProducer ( QueueingEventProducer evp ) {
        if ( this.eventProducer == evp ) {
            this.eventProducer = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.executor.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        try {
            this.executor.shutdown();
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch ( InterruptedException e ) {
            log.error("Failed to stop service monitor", e); //$NON-NLS-1$
        }
        finally {
            this.executor = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepository = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepository == cr ) {
            this.configRepository = null;
        }
    }


    /**
     * Actively checks all services
     * 
     */
    public void checkAll () {
        try {
            log.debug("Checking services"); //$NON-NLS-1$
            for ( ServiceStructuralObject service : this.configRepository.getServices() ) {
                checkService(service);
            }
        }
        catch ( ConfigRepositoryException e ) {
            log.error("Failed to get services", e); //$NON-NLS-1$
        }
    }


    @Override
    public ServiceRuntimeStatus checkServiceActive ( StructuralObjectReference service, long timeout ) throws InterruptedException {
        long start = System.currentTimeMillis();
        long end = start + timeout;
        while ( System.currentTimeMillis() < end ) {
            ServiceRuntimeStatus s = checkServiceReference(service);
            if ( s != ServiceRuntimeStatus.TRANSIENT && s != ServiceRuntimeStatus.DISABLED ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Service status is " + s); //$NON-NLS-1$
                }
                return s;
            }
            try {
                Thread.sleep(2000);
            }
            catch ( InterruptedException e ) {
                return ServiceRuntimeStatus.DISABLED;
            }
        }
        log.warn("Timeout waiting for service " + service); //$NON-NLS-1$
        return ServiceRuntimeStatus.ERROR;
    }


    /**
     * @param service
     * @return
     */
    private ServiceRuntimeStatus checkService ( ServiceStructuralObject service ) {

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Checking service %s: %s", service.getServiceType(), service)); //$NON-NLS-1$
        }

        StructuralObjectReference reference = StructuralObjectReferenceImpl.fromObject(service);
        if ( service.getState() == ConfigurationState.UNCONFIGURED ) {
            haveStatus(reference, ServiceRuntimeStatus.DISABLED);
            return ServiceRuntimeStatus.DISABLED;
        }
        return checkServiceReference(reference);
    }


    /**
     * @param service
     * @return
     */
    private ServiceRuntimeStatus checkServiceReference ( StructuralObjectReference service ) {
        try {
            BaseServiceManager sm = this.serviceManager.getServiceManager(service, BaseServiceManager.class);
            ServiceRuntimeStatus runtimeStatus = sm.getRuntimeStatus(service.getId());
            if ( runtimeStatus == ServiceRuntimeStatus.DISABLED ) {
                haveStatus(service, ServiceRuntimeStatus.DISABLED);
                return ServiceRuntimeStatus.DISABLED;
            }

            if ( runtimeStatus == ServiceRuntimeStatus.TRANSIENT ) {
                return ServiceRuntimeStatus.TRANSIENT;
            }

            haveStatus(service, runtimeStatus);
            return runtimeStatus;
        }
        catch ( ServiceManagementException e ) {
            log.debug("Failed to get service status", e); //$NON-NLS-1$
            haveStatus(service, ServiceRuntimeStatus.ERROR);
            return ServiceRuntimeStatus.ERROR;
        }
    }


    /**
     * @param service
     * @param status
     */
    private void haveStatus ( StructuralObjectReference service, ServiceRuntimeStatus status ) {
        ServiceRuntimeStatus st = this.statusCache.get(service.getId());
        if ( st != status ) {
            if ( log.isInfoEnabled() ) {
                log.info(String.format("Service %s (%s) changed state to %s", service.getLocalType(), service.getId(), status)); //$NON-NLS-1$
            }
            ServiceRuntimeStatus oldStatus = this.statusCache.put(service.getId(), status);
            ServiceStatusEvent ev = new ServiceStatusEvent(this.eventProducer.getMessageSource());
            ev.setServiceId(service.getId());
            ev.setOldStatus(oldStatus);
            ev.setNewStatus(status);
            this.eventProducer.publish(ev);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        try {
            checkAll();
        }
        catch ( Throwable e ) {
            log.error("Exception running service checks", e); //$NON-NLS-1$
        }
    }
}
