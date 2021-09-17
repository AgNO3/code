/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info.internal;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.agent.system.info.SystemInformationRefresher;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.info.msg.AgentSystemInformation;
import eu.agno3.orchestrator.system.info.msg.SystemInformationUpdatedEvent;
import eu.agno3.orchestrator.system.info.network.NetworkInformationProvider;
import eu.agno3.orchestrator.system.info.platform.PlatformInformationProvider;
import eu.agno3.orchestrator.system.info.storage.StorageInformationException;
import eu.agno3.orchestrator.system.info.storage.StorageInformationProvider;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    SystemInformationRefresher.class, SystemService.class
} )
@SystemServiceType ( SystemInformationRefresher.class )
public class SystemInformationRefresherImpl implements SystemInformationRefresher {

    private static final Logger log = Logger.getLogger(RefreshRequestEndpoint.class);

    private PlatformInformationProvider platformInfoProvider;
    private NetworkInformationProvider networkInfoProvider;
    private StorageInformationProvider storageInfoProvider;

    private MessagingClient<AgentMessageSource> messageClient;

    private Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread ( Runnable r ) {
            return new Thread(r, "SystemInformationRefresher"); //$NON-NLS-1$
        }
    });


    @Reference
    protected synchronized void setPlatformInfoProvider ( PlatformInformationProvider provider ) {
        this.platformInfoProvider = provider;
    }


    protected synchronized void unsetPlatformInfoProvider ( PlatformInformationProvider provider ) {
        if ( this.platformInfoProvider == provider ) {
            this.platformInfoProvider = null;
        }
    }


    @Reference
    protected synchronized void setNetworkInformationProvider ( NetworkInformationProvider provider ) {
        this.networkInfoProvider = provider;
    }


    protected synchronized void unsetNetworkInformationProvider ( NetworkInformationProvider provider ) {
        if ( this.networkInfoProvider == provider ) {
            this.networkInfoProvider = null;
        }
    }


    @Reference
    protected synchronized void setStorageInformationProvider ( StorageInformationProvider provider ) {
        this.storageInfoProvider = provider;
    }


    protected synchronized void unsetStorageInformationProvider ( StorageInformationProvider provider ) {
        if ( this.storageInfoProvider == provider ) {
            this.storageInfoProvider = null;
        }
    }


    @Reference
    protected synchronized void setMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        this.messageClient = mc;
    }


    protected synchronized void unsetMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        if ( this.messageClient == mc ) {
            this.messageClient = null;
        }
    }


    /**
     * @return the messageClient
     */
    public MessagingClient<AgentMessageSource> getMessageClient () {
        return this.messageClient;
    }


    /**
     * @return the networkInfoProvider
     */
    public NetworkInformationProvider getNetworkInfoProvider () {
        return this.networkInfoProvider;
    }


    /**
     * @return the storageInfoProvider
     */
    public StorageInformationProvider getStorageInfoProvider () {
        return this.storageInfoProvider;
    }


    /**
     * @return the platformInfoProvider
     */
    public PlatformInformationProvider getPlatformInfoProvider () {
        return this.platformInfoProvider;
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * 
     */
    @Override
    public void triggerRefresh ( boolean rescanPartitions ) {
        log.debug("Request to refresh system information"); //$NON-NLS-1$
        SystemInformationUpdatedEvent ev = new SystemInformationUpdatedEvent(this.messageClient.getMessageSource());
        this.executor.execute(new UpdateRunnable(ev, rescanPartitions));
    }

    private class UpdateRunnable implements Runnable {

        @NonNull
        private SystemInformationUpdatedEvent ev;
        private boolean rescanPartitions;


        /**
         * @param ev
         * @param rescanPartitions
         */
        public UpdateRunnable ( @NonNull SystemInformationUpdatedEvent ev, boolean rescanPartitions ) {
            this.ev = ev;
            this.rescanPartitions = rescanPartitions;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            try {
                if ( this.rescanPartitions ) {
                    getStorageInfoProvider().rescanPartitions();
                }
                AgentSystemInformation sysInfo = new AgentSystemInformation();
                synchronized ( SystemInformationRefresherImpl.this ) {
                    MessagingClient<AgentMessageSource> mc = getMessageClient();
                    if ( mc != null ) {
                        sysInfo.setPlatformInformation(getPlatformInfoProvider().getInformation());
                        sysInfo.setNetworkInformation(getNetworkInfoProvider().getInformation());
                        sysInfo.setStorageInformation(getStorageInfoProvider().getInformation());
                        this.ev.setSystemInfo(sysInfo);
                        getLog().debug("Publishing updated system information"); //$NON-NLS-1$
                        mc.publishEvent(this.ev);
                    }

                }
            }
            catch (
                StorageInformationException |
                MessagingException |
                InterruptedException e ) {
                getLog().warn("Failed to update system information", e); //$NON-NLS-1$
            }
        }
    }
}
