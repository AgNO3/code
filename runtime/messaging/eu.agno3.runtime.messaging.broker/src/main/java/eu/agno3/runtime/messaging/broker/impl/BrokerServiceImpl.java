/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.impl;


import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.MBeanServer;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.messaging.broker.BrokerConfiguration;
import eu.agno3.runtime.messaging.broker.BrokerConfigurationException;
import eu.agno3.runtime.messaging.broker.EmbeddedBrokerService;
import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;
import eu.agno3.runtime.messaging.broker.auth.BrokerAuthenticationPlugin;
import eu.agno3.runtime.messaging.broker.auth.BrokerAuthorizationPlugin;
import eu.agno3.runtime.messaging.broker.transport.TransportFactory;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    EmbeddedBrokerService.class
}, immediate = true, configurationPid = BrokerConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class BrokerServiceImpl extends BrokerService implements EmbeddedBrokerService {

    private static final Logger log = Logger.getLogger(BrokerServiceImpl.class);

    private final Object brokerLock = new Object();

    private final Set<TransportFactory> transportServers = new HashSet<>();
    private final Map<TransportFactory, TransportConnector> transportConnectors = new HashMap<>();
    private final SortedSet<PrioritizedBrokerPlugin> plugins = new TreeSet<>(new PrioritizedBrokerPluginComparator());
    private MBeanServer mbeanServer;

    boolean started = false;


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting message broker"); //$NON-NLS-1$

        synchronized ( this.brokerLock ) {
            try {
                configureBroker(context);
            }
            catch ( BrokerConfigurationException e ) {
                log.error("Failed to configure message broker:", e); //$NON-NLS-1$
            }

            try {
                this.start();
            }
            catch ( Exception e ) {
                log.error("Failed to start message broker:", e); //$NON-NLS-1$
            }

            this.waitUntilStarted();

            this.started = true;
        }

    }


    /**
     * @param context
     * @throws BrokerConfigurationException
     */
    protected void configureBroker ( ComponentContext context ) throws BrokerConfigurationException {
        configureJMX(context.getProperties());
        configureBroker(this, context.getProperties());

        if ( log.isDebugEnabled() ) {
            for ( PrioritizedBrokerPlugin plugin : this.plugins ) {
                log.debug(String.format("Plugin %s priority %d", plugin.getClass().getName(), plugin.getPriority())); //$NON-NLS-1$
            }
        }

        try {
            this.setDestinationFactory(new TemporaryExpiringDestinationFactory(this, getTaskRunnerFactory(), getPersistenceAdapter()));
        }
        catch ( IOException e1 ) {
            log.error("Failed to setup broker", e1); //$NON-NLS-1$
            return;
        }

        PolicyEntry defaultPolicy = this.getDestinationPolicy().getDefaultEntry();
        defaultPolicy.setGcInactiveDestinations(true);

        setPlugins(this.plugins.toArray(new BrokerPlugin[] {}));

        for ( TransportFactory tf : this.transportServers ) {
            try {
                TransportConnector tc = new TransportConnector(tf.createTransport());
                this.addConnector(tc);
                this.transportConnectors.put(tf, tc);
            }
            catch ( Exception e ) {
                throw new BrokerConfigurationException("Failed to add transport:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     */
    private void configureJMX ( Dictionary<String, Object> props ) {
        if ( ConfigUtil.parseBoolean(props, "enableJMX", false) && this.mbeanServer != null ) { //$NON-NLS-1$
            log.debug("Enabling JMX"); //$NON-NLS-1$
            this.setManagementContext(new ManagementContext(this.mbeanServer));
            this.getManagementContext().setCreateConnector(false);
            this.getManagementContext().setUseMBeanServer(false);
            this.getManagementContext().setCreateMBeanServer(false);
            this.getManagementContext()
                    .setSuppressMBean("endpoint=dynamicProducer,connectionName=*,destinationType=TempQueue,destinationName=ActiveMQ.Advisory.*"); //$NON-NLS-1$
            // this.getManagementContext().setSuppressMBean(
            // "endpoint=dynamicProducer,endpoint=Consumer,endpoint=Producer,connectionName=*,destinationType=TempQueue,destinationName=ActiveMQ.Advisory.*");
            // //$NON-NLS-1$
            this.setUseJmx(true);
        }
        else {
            this.setUseJmx(false);
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        log.debug("Stopping message broker"); //$NON-NLS-1$
        synchronized ( this.brokerLock ) {
            this.started = false;
            try {
                if ( !isStopped() && !isStopping() ) {
                    this.stop();
                }
                log.debug("Waiting"); //$NON-NLS-1$
                this.waitUntilStopped();
                log.debug("Stopped"); //$NON-NLS-1$
            }
            catch ( Exception e ) {
                log.error("Failed to stop message broker:", e); //$NON-NLS-1$
            }
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setMBeanServer ( MBeanServer mbs ) {
        this.mbeanServer = mbs;
    }


    protected synchronized void unsetMBeanServer ( MBeanServer mbs ) {
        if ( this.mbeanServer == mbs ) {
            this.mbeanServer = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected void bindTransport ( TransportFactory tf ) {

        if ( log.isDebugEnabled() ) {
            log.debug("Binding transport " + tf.getClass().getName()); //$NON-NLS-1$
        }

        synchronized ( this.brokerLock ) {
            if ( this.started ) {
                try {
                    TransportServer c = tf.createTransport();
                    TransportConnector tc = new TransportConnector(c);
                    tc.setBrokerService(this);
                    this.addConnector(tc);
                    this.transportConnectors.put(tf, tc);
                    this.startTransportConnector(tc);

                }
                catch ( Exception e ) {
                    log.error("Failed to start connector:", e); //$NON-NLS-1$
                }
            }

            this.transportServers.add(tf);
        }

    }


    protected void unbindTransport ( TransportFactory tf ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding transport " + tf.getClass().getName()); //$NON-NLS-1$
        }

        synchronized ( this.brokerLock ) {
            this.transportServers.remove(tf);

            if ( this.started ) {
                try {
                    TransportConnector tc = this.transportConnectors.get(tf);
                    if ( tc != null ) {
                        tc.stop();
                        this.transportConnectors.remove(tf);
                        this.removeConnector(tc);
                    }
                }
                catch ( Exception e ) {
                    log.error("Failed to remove connector:", e); //$NON-NLS-1$
                }
            }
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected void bindPlugin ( PrioritizedBrokerPlugin plugin ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Binding plugin " + plugin.getClass().getName()); //$NON-NLS-1$
        }
        synchronized ( this.brokerLock ) {
            this.plugins.add(plugin);
        }
    }


    protected void unbindPlugin ( PrioritizedBrokerPlugin plugin ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding plugin " + plugin.getClass().getName()); //$NON-NLS-1$
        }
        synchronized ( this.brokerLock ) {
            this.plugins.remove(plugin);
        }
    }


    @Reference
    protected void setAuthenticationPlugin ( BrokerAuthenticationPlugin plugin ) {}


    protected void unsetAuthenticationPlugin ( BrokerAuthenticationPlugin plugin ) {}


    @Reference
    protected void setAuthorizationPlugin ( BrokerAuthorizationPlugin plugin ) {}


    protected void unsetAuthorizationPlugin ( BrokerAuthorizationPlugin plugin ) {}


    private static void configureBroker ( BrokerService bs, Dictionary<String, Object> properties ) throws BrokerConfigurationException {

        String brokerNameSpec = (String) properties.get(BrokerConfiguration.BROKER_NAME_ATTR);

        if ( brokerNameSpec != null ) {
            bs.setBrokerName(brokerNameSpec);
        }
        else {
            throw new BrokerConfigurationException("Message broker name has to be set"); //$NON-NLS-1$
        }

        String enableStatsSpec = (String) properties.get(BrokerConfiguration.ENABLE_STATS_ATTR);
        if ( enableStatsSpec != null && enableStatsSpec.equals(Boolean.TRUE.toString()) ) {
            bs.setEnableStatistics(true);
        }
        else {
            bs.setEnableStatistics(false);
        }

        configureBrokerStorage(bs, properties);

        String clearOnStartSpec = (String) properties.get(BrokerConfiguration.CLEAR_ON_START_ATTR);

        if ( clearOnStartSpec != null && clearOnStartSpec.equals(Boolean.TRUE.toString()) ) {
            bs.setDeleteAllMessagesOnStartup(true);
        }
        else {
            bs.setDeleteAllMessagesOnStartup(false);
        }

        configureBrokerLimits(bs, properties);

        bs.setPopulateJMSXUserID(true);
        bs.setUseAuthenticatedPrincipalForJMSXUserID(true);

        Duration inactiveDuration = ConfigUtil.parseDuration(properties, "inactiveTimeout", Duration.standardMinutes(30)); //$NON-NLS-1$
        bs.setDestinationPolicy(new PolicyMap());
        PolicyEntry defaultEntry = new PolicyEntry();
        defaultEntry.setGcInactiveDestinations(true);
        defaultEntry.setInactiveTimeoutBeforeGC(inactiveDuration.getMillis());
        bs.setSchedulePeriodForDestinationPurge(10000);
        bs.getDestinationPolicy().setDefaultEntry(defaultEntry);
    }


    /**
     * @param bs
     * @param properties
     * @throws BrokerConfigurationException
     */
    private static void configureBrokerStorage ( BrokerService bs, Dictionary<String, Object> properties ) throws BrokerConfigurationException {
        String disablePersistenceSpec = (String) properties.get(BrokerConfiguration.PERSISTENT_ATTR);
        if ( disablePersistenceSpec != null && disablePersistenceSpec.equals(Boolean.TRUE.toString()) ) {
            bs.setPersistent(false);
        }
        else {
            bs.setPersistent(true);
        }

        String dataDirSpec = (String) properties.get(BrokerConfiguration.DATA_DIRECTORY_ATTR);
        if ( dataDirSpec != null ) {
            bs.setDataDirectoryFile(new File(dataDirSpec));
        }

        if ( bs.isPersistent() && dataDirSpec != null ) {
            try {
                bs.getPersistenceAdapter().setDirectory(bs.getDataDirectoryFile());
            }
            catch ( IOException e ) {
                throw new BrokerConfigurationException("Failed to set persistence data directory", e); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                try {
                    log.debug("Using persistence adapter " + bs.getPersistenceAdapter().getClass().getName()); //$NON-NLS-1$
                }
                catch ( IOException e ) {
                    log.warn("Failed to get persistence adapter", e); //$NON-NLS-1$
                }
            }
        }

        String tmpDirSpec = (String) properties.get(BrokerConfiguration.TMP_DIRECTORY_ATTR);
        if ( tmpDirSpec != null ) {
            bs.setTmpDataDirectory(new File(tmpDirSpec));
        }
    }


    /**
     * @param bs
     * @param properties
     */
    private static void configureBrokerLimits ( BrokerService bs, Dictionary<String, Object> properties ) {
        SystemUsage resLimit = new SystemUsage();
        MemoryUsage memoryLimit = new MemoryUsage();
        StoreUsage storeLimit = new StoreUsage();
        TempUsage tempStoreLimit = new TempUsage();

        String memLimitSpec = (String) properties.get(BrokerConfiguration.MEMORY_LIMIT_ATTR);

        if ( memLimitSpec != null ) {
            memoryLimit.setLimit(Long.parseLong(memLimitSpec) * 1024 * 1024);
        }

        String storeLimitSpec = (String) properties.get(BrokerConfiguration.DATA_SIZE_LIMIT_ATTR);

        if ( storeLimitSpec != null ) {
            storeLimit.setLimit(Long.parseLong(storeLimitSpec) * 1024 * 1024);
        }

        String tempStoreLimitSpec = (String) properties.get(BrokerConfiguration.TMP_SIZE_LIMIT_ATTR);
        if ( tempStoreLimitSpec != null ) {
            tempStoreLimit.setLimit(Long.parseLong(tempStoreLimitSpec) * 1024 * 1024);
        }

        resLimit.setMemoryUsage(memoryLimit);
        resLimit.setStoreUsage(storeLimit);
        resLimit.setTempUsage(tempStoreLimit);

        resLimit.setSendFailIfNoSpace(true);

        bs.setSystemUsage(resLimit);
    }
}
