/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service;


import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceState;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.runtime.configloader.jmx.ConfigLoaderMXBean;
import eu.agno3.runtime.jmx.JMXClient;
import eu.agno3.runtime.jmx.JMXClientFactory;
import eu.agno3.runtime.jmx.JMXConnectionPool;
import eu.agno3.runtime.jmx.JMXException;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateMXBean;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
public abstract class AbstractSingletonRuntimeServiceManager implements RuntimeServiceManager {

    private static final Logger log = Logger.getLogger(AbstractSingletonRuntimeServiceManager.class);
    private ServiceSystem serviceSystem;
    private JMXClientFactory jmxClientFactory;
    private Path configFilesPath;
    private String serviceName;
    private boolean disableSystemServiceChecks;
    private String jmxSocketPath;
    private Set<String> ignorePIDRemoval;

    private String installLocation;

    private JMXConnectionPool jmxPool;
    private volatile boolean isTransient;


    /**
     * 
     */
    public AbstractSingletonRuntimeServiceManager () {
        super();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.jmxPool != null ) {
            this.jmxPool.close();
            this.jmxPool = null;
        }
    }


    /**
     * @param properties
     */
    protected void parseConfig ( Dictionary<String, Object> cfg ) {
        this.configFilesPath = Paths.get(ConfigUtil.parseString(cfg, "configFilePath", getDefaultConfigFilePath())); //$NON-NLS-1$
        this.serviceName = ConfigUtil.parseString(cfg, "systemServiceName", getDefaultSystemServiceName()); //$NON-NLS-1$
        this.disableSystemServiceChecks = ConfigUtil.parseBoolean(cfg, "disableSystemServiceChecks", false); //$NON-NLS-1$
        this.jmxSocketPath = ConfigUtil.parseString(cfg, "jmxSocketPath", getDefaultJMXSocketPath()); //$NON-NLS-1$
        this.ignorePIDRemoval = ConfigUtil.parseStringSet(cfg, "ignorePIDRemoval", Collections.EMPTY_SET); //$NON-NLS-1$
        this.installLocation = ConfigUtil.parseString(cfg, "installLocation", getDefaultInstallLocation()); //$NON-NLS-1$
    }


    /**
     * @return
     */
    protected String getDefaultJMXSocketPath () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#getNoRemovePIDs()
     */
    @Override
    public Set<String> getNoRemovePIDs () {
        return Collections.unmodifiableSet(this.ignorePIDRemoval);
    }


    /**
     * @return the default install location
     */
    protected abstract String getDefaultInstallLocation ();


    /**
     * @return the default system service name
     */
    protected abstract String getDefaultSystemServiceName ();


    /**
     * @return the default config dir path
     */
    protected abstract String getDefaultConfigFilePath ();


    @Reference
    protected synchronized void setServiceSystem ( ServiceSystem ss ) {
        this.serviceSystem = ss;
    }


    protected synchronized void unsetServiceSystem ( ServiceSystem ss ) {
        if ( this.serviceSystem == ss ) {
            this.serviceSystem = null;
        }
    }


    @Reference
    protected synchronized void setJMXClientFactory ( JMXClientFactory jcf ) {
        this.jmxClientFactory = jcf;
    }


    protected synchronized void unsetJMXClientFactory ( JMXClientFactory jcf ) {
        if ( this.jmxClientFactory == jcf ) {
            this.jmxClientFactory = null;
        }
    }


    @Override
    public String getSystemServiceName () {
        return this.serviceName;
    }


    /**
     * @return the user as which the service is running
     * @throws ServiceManagementException
     */
    @Override
    public UserPrincipal getServicePrincipal () throws ServiceManagementException {
        if ( FileSecurityUtils.isRunningAsRoot() ) {
            try {
                String servicePrincipalName = this.getServicePrincipalName();
                if ( !StringUtils.isBlank(servicePrincipalName) ) {
                    return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(servicePrincipalName);
                }
            }
            catch ( IOException e ) {
                throw new ServiceManagementException("Failed to lookup service principal", e); //$NON-NLS-1$
            }
        }
        return null;
    }


    /**
     * @return the user name as which the service is running
     */
    protected abstract String getServicePrincipalName ();


    /**
     * 
     * @return a group principal as which the service is running
     * @throws ServiceManagementException
     */
    @Override
    public GroupPrincipal getGroupPrincipal () throws ServiceManagementException {
        if ( FileSecurityUtils.isRunningAsRoot() ) {
            try {
                String serviceGroupPrincipalName = this.getServiceGroupPrincipalName();
                if ( !StringUtils.isBlank(serviceGroupPrincipalName) ) {
                    return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(serviceGroupPrincipalName);
                }
            }
            catch ( IOException e ) {
                throw new ServiceManagementException("Failed to lookup service group principal", e); //$NON-NLS-1$
            }
        }
        return null;
    }


    /**
     * @return a group name as which the service is running
     */
    protected abstract String getServiceGroupPrincipalName ();


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#getConfigFilesPath()
     */
    @Override
    public Path getConfigFilesPath () {
        return this.configFilesPath;
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#getJMXConnection()
     */
    @Override
    public JMXClient getJMXConnection () throws ServiceManagementException {
        try {
            return getJMXConnectionPool().getConnection();
        }
        catch (
            URISyntaxException |
            JMXException e ) {
            throw new ServiceManagementException("Failed to get jmx connection", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#getUnpooledJMXConnection()
     */
    @Override
    public JMXClient getUnpooledJMXConnection () throws ServiceManagementException {
        try {
            return this.jmxClientFactory.getConnection(this.getJMXUri(), null);
        }
        catch (
            IOException |
            NotBoundException |
            URISyntaxException e ) {
            throw new ServiceManagementException("Failed to get jmx connection", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws ServiceManagementException
     * @throws URISyntaxException
     */
    private synchronized JMXConnectionPool getJMXConnectionPool () throws URISyntaxException, ServiceManagementException {
        if ( this.jmxPool != null ) {
            return this.jmxPool;
        }

        this.jmxPool = this.jmxClientFactory.createConnectionPool(this.getJMXUri(), null);
        return this.jmxPool;
    }


    /**
     * @return
     * @throws URISyntaxException
     * @throws ServiceManagementException
     */
    private URI getJMXUri () throws URISyntaxException, ServiceManagementException {
        if ( StringUtils.isBlank(this.jmxSocketPath) ) {
            throw new ServiceManagementException("Service is not JMX capable"); //$NON-NLS-1$
        }
        return new URI("socket://" + this.jmxSocketPath); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#enable(java.util.UUID)
     */
    @Override
    public void enable ( UUID instanceId ) throws ServiceManagementException {
        if ( this.disableSystemServiceChecks ) {
            return;
        }
        try {
            try {
                this.isTransient = true;
                this.serviceSystem.getService(getSystemServiceName()).enableOnBoot();
                this.serviceSystem.getService(getSystemServiceName()).start();
            }
            finally {
                this.isTransient = false;
            }
        }
        catch ( ServiceException e ) {
            throw new ServiceManagementException("Failed to get init service", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#disable(java.util.UUID)
     */
    @Override
    public void disable ( UUID instanceId ) throws ServiceManagementException {
        if ( this.disableSystemServiceChecks ) {
            return;
        }
        try {
            try {
                this.isTransient = true;
                this.serviceSystem.getService(getSystemServiceName()).disableOnBoot();
                this.serviceSystem.getService(getSystemServiceName()).stop();
            }
            finally {
                this.isTransient = false;
            }
        }
        catch ( ServiceException e ) {
            throw new ServiceManagementException("Failed to get init service", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#restart(java.util.UUID)
     */
    @Override
    public void restart ( UUID instanceId ) throws ServiceManagementException {
        if ( this.disableSystemServiceChecks ) {
            return;
        }
        try {
            Service service = this.serviceSystem.getService(getSystemServiceName());
            try {
                this.isTransient = true;
                service.enableOnBoot();
                if ( service.getState() == ServiceState.INACTIVE ) {
                    service.start();
                }
                else {
                    service.restart();
                }
            }
            finally {
                this.isTransient = false;
            }
        }
        catch ( ServiceException e ) {
            throw new ServiceManagementException("Failed to get init service", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#stop(java.util.UUID)
     */
    @Override
    public void stop ( UUID instanceId ) throws ServiceManagementException {
        if ( this.disableSystemServiceChecks ) {
            return;
        }
        try {
            try {
                this.isTransient = true;
                this.serviceSystem.getService(getSystemServiceName()).stop();
            }
            finally {
                this.isTransient = false;
            }
        }
        catch ( ServiceException e ) {
            throw new ServiceManagementException("Failed to get init service", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#start(java.util.UUID)
     */
    @Override
    public void start ( UUID instanceId ) throws ServiceManagementException {
        if ( this.disableSystemServiceChecks ) {
            return;
        }
        try {
            try {
                this.isTransient = true;
                this.serviceSystem.getService(getSystemServiceName()).start();
            }
            finally {
                this.isTransient = false;
            }
        }
        catch ( ServiceException e ) {
            throw new ServiceManagementException("Failed to get init service", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#reconfigure(java.util.Set)
     */
    @Override
    public void reconfigure ( Set<String> pids ) throws ServiceManagementException {
        try ( JMXClient jmxConnection = getJMXConnection() ) {
            ObjectName configLoaderName = new ObjectName("eu.agno3.runtime.configloader:type=ConfigLoader"); //$NON-NLS-1$
            ConfigLoaderMXBean configLoader = JMX.newMXBeanProxy(jmxConnection, configLoaderName, ConfigLoaderMXBean.class);
            try {
                this.isTransient = true;
                if ( !configLoader.reloadConfig(pids) ) {
                    throw new ServiceManagementException("Failed to reload config"); //$NON-NLS-1$
                }
            }
            finally {
                this.isTransient = false;
            }
        }
        catch (
            UndeclaredThrowableException |
            MalformedObjectNameException |
            IOException e ) {
            throw new ServiceManagementException("Failed to get configloader service", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#isOnlineReconfigurable(java.util.Set)
     */
    @Override
    public boolean isOnlineReconfigurable ( Set<String> modifiedPids ) {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#getRuntimeStatus(java.util.UUID)
     */
    @Override
    public ServiceRuntimeStatus getRuntimeStatus ( UUID instanceId ) throws ServiceManagementException {
        ServiceRuntimeStatus st = getSystemServiceStatus();
        if ( st != ServiceRuntimeStatus.ACTIVE ) {
            if ( this.isTransient && st == ServiceRuntimeStatus.ERROR ) {
                return ServiceRuntimeStatus.TRANSIENT;
            }
            return st;
        }

        return getOnlineServiceStatus();
    }


    /**
     * @return
     */
    protected ServiceRuntimeStatus getOnlineServiceStatus () {

        try ( JMXClient jmxConnection = this.getJMXConnection() ) {

            PlatformStateMXBean stateBean = JMX.newMBeanProxy(
                jmxConnection,
                new ObjectName("eu.agno3.runtime.update:type=PlatformState"), //$NON-NLS-1$
                PlatformStateMXBean.class);
            PlatformState ps = PlatformState.valueOf(stateBean.getState());
            if ( log.isTraceEnabled() ) {
                log.trace("Service runtime state is " + ps); //$NON-NLS-1$
            }
            return mapRuntimeStatus(ps);
        }
        catch (
            ServiceManagementException |
            UndeclaredThrowableException |
            IOException |
            IllegalArgumentException |
            MalformedObjectNameException e ) {
            log.trace("Caught exception", e); //$NON-NLS-1$
            if ( this.isTransient || e instanceof UndeclaredThrowableException && e.getCause() instanceof InstanceNotFoundException ) {
                return ServiceRuntimeStatus.TRANSIENT;
            }
            log.warn("Exception in service check", e); //$NON-NLS-1$
            return ServiceRuntimeStatus.ERROR;
        }
    }


    /**
     * @param state
     * @return
     */
    protected static ServiceRuntimeStatus mapRuntimeStatus ( PlatformState state ) {
        switch ( state ) {
        case BOOTING:
        case RECONFIGURE:
        case STOPPING:
        case UPDATING:
            return ServiceRuntimeStatus.TRANSIENT;
        case FAILED:
            return ServiceRuntimeStatus.ERROR;
        case WARNING:
            return ServiceRuntimeStatus.WARNING;
        case STARTED:
            return ServiceRuntimeStatus.ACTIVE;
        default:
            return ServiceRuntimeStatus.UNKNOWN;
        }
    }


    /**
     * @return
     */
    private ServiceRuntimeStatus getSystemServiceStatus () {
        if ( this.disableSystemServiceChecks ) {
            return checkActive();
        }
        try {
            Service service = this.serviceSystem.getService(getSystemServiceName());
            ServiceState state = service.getState();

            if ( log.isTraceEnabled() ) {
                log.trace("Service state is " + state); //$NON-NLS-1$
            }

            switch ( state ) {
            case ACTIVATING:
            case DEACTIVATING:
                return ServiceRuntimeStatus.TRANSIENT;
            case ACTIVE:
                return checkActive();
            case INACTIVE:
                return ServiceRuntimeStatus.DISABLED;
            case FAILED:
            case UNKNOWN:
            default:
                return ServiceRuntimeStatus.ERROR;
            }
        }
        catch ( ServiceException e ) {
            log.debug("Failed to get service status for " + getSystemServiceName(), e); //$NON-NLS-1$
            return ServiceRuntimeStatus.ERROR;
        }
    }


    /**
     * @return
     */
    private ServiceRuntimeStatus checkActive () {
        if ( !StringUtils.isBlank(this.jmxSocketPath) && !Files.exists(Paths.get(this.jmxSocketPath, "100001.rmi")) ) { //$NON-NLS-1$
            return ServiceRuntimeStatus.DISABLED;
        }

        try ( JMXClient cl = getJMXConnection() ) {

        }
        catch ( Exception e ) {
            if ( e instanceof ServiceManagementException && e.getCause() instanceof JMXException
                    && e.getCause().getCause() instanceof ConnectIOException ) {
                log.trace("Cannot connect to JMX server", e); //$NON-NLS-1$
                return ServiceRuntimeStatus.DISABLED;
            }
            if ( this.isTransient || e instanceof ServiceManagementException && e.getCause() instanceof JMXException
                    && e.getCause().getCause() instanceof NotBoundException ) {
                log.trace("Cannot connect to JMX server", e); //$NON-NLS-1$
                return ServiceRuntimeStatus.TRANSIENT;
            }
            log.warn("Failed to get JMX connection", e); //$NON-NLS-1$
            return ServiceRuntimeStatus.ERROR;
        }

        return ServiceRuntimeStatus.ACTIVE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager#getP2InstallLocation()
     */
    @Override
    public URI getP2InstallLocation () {
        return Paths.get(this.installLocation, "p2").toUri(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#runMonitoring()
     */
    @Override
    public void runMonitoring () {
        // TODO Auto-generated method stub

    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#forceReloadAll(java.lang.String)
     */
    @Override
    public void forceReloadAll ( String string ) throws ServiceManagementException {
        if ( getSystemServiceStatus() != ServiceRuntimeStatus.ACTIVE && getSystemServiceStatus() != ServiceRuntimeStatus.WARNING ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Force reloading config " + string); //$NON-NLS-1$
        }

        try {
            this.isTransient = true;
            try ( JMXClient jmxConnection = getJMXConnection() ) {
                ObjectName configLoaderName = new ObjectName("eu.agno3.runtime.configloader:type=ConfigLoader"); //$NON-NLS-1$
                ConfigLoaderMXBean configLoader = JMX.newMXBeanProxy(jmxConnection, configLoaderName, ConfigLoaderMXBean.class);
                if ( !configLoader.forceReloadConfig(string) ) {
                    throw new ServiceManagementException("Failed to reload config"); //$NON-NLS-1$
                }
            }
            catch (
                UndeclaredThrowableException |
                MalformedObjectNameException |
                IOException e ) {
                throw new ServiceManagementException("Failed to get configloader service", e); //$NON-NLS-1$
            }
        }
        finally {
            this.isTransient = false;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws BackupException
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.AbstractSingletonRuntimeServiceManager#getBackupUnits(eu.agno3.orchestrator.config.model.realm.ConfigurationInstance)
     */
    @Override
    public List<BackupUnit> getBackupUnits ( @NonNull ConfigurationInstance cfg ) throws BackupException {
        return new ArrayList<>();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager#getOverrideStoragePath(java.lang.String)
     */
    @Override
    public Path getOverrideStoragePath ( String storageAlias ) {
        if ( "dev".equals(storageAlias) ) { //$NON-NLS-1$
            return Paths.get("/tmp/").resolve(this.getSystemServiceName()); //$NON-NLS-1$
        }
        else if ( "system".equals(storageAlias) ) { //$NON-NLS-1$
            return Paths.get("/srv/").resolve(this.getSystemServiceName()); //$NON-NLS-1$
        }
        return null;
    }

}