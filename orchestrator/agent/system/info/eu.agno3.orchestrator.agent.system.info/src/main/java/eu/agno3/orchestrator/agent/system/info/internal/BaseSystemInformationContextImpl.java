/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInformationProvider;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformationProvider;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformationException;
import eu.agno3.orchestrator.system.info.storage.StorageInformationProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = BaseSystemInformationContext.class )
public class BaseSystemInformationContextImpl implements BaseSystemInformationContext {

    private PlatformInformationProvider platformInfoProvider;
    private StorageInformationProvider storageInfoProvider;
    private NetworkInformationProvider networkInfoProvider;


    @Reference
    protected synchronized void setPlatformInformationProvider ( PlatformInformationProvider pip ) {
        this.platformInfoProvider = pip;
    }


    protected synchronized void unsetPlatformInformationProvider ( PlatformInformationProvider pip ) {
        if ( this.platformInfoProvider == pip ) {
            this.platformInfoProvider = null;
        }
    }


    @Reference
    protected synchronized void setStorageInformationProvider ( StorageInformationProvider sip ) {
        this.storageInfoProvider = sip;
    }


    protected synchronized void unsetStorageInformationProvider ( StorageInformationProvider sip ) {
        if ( this.storageInfoProvider == sip ) {
            this.storageInfoProvider = null;
        }
    }


    @Reference
    protected synchronized void setNetworkInformationProvider ( NetworkInformationProvider nip ) {
        this.networkInfoProvider = nip;
    }


    protected synchronized void unsetNetworkInformationProvider ( NetworkInformationProvider nip ) {
        if ( this.networkInfoProvider == nip ) {
            this.networkInfoProvider = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext#getNetworkInformation()
     */
    @Override
    public NetworkInformation getNetworkInformation () throws SystemInformationException {
        return this.networkInfoProvider.getInformation();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext#getStorageInformation()
     */
    @Override
    public StorageInformation getStorageInformation () throws StorageInformationException {
        return this.storageInfoProvider.getInformation();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.system.info.BaseSystemInformationContext#getPlatformInformation()
     */
    @Override
    public PlatformInformation getPlatformInformation () throws SystemInformationException {
        return this.platformInfoProvider.getInformation();
    }

}
