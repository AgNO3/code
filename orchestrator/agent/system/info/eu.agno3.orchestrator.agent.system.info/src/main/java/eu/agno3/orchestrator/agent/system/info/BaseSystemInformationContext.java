/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info;


import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformationException;


/**
 * @author mbechler
 *
 */
public interface BaseSystemInformationContext {

    /**
     * @return the network information
     * @throws SystemInformationException
     */
    NetworkInformation getNetworkInformation () throws SystemInformationException;


    /**
     * @return the storage information
     * @throws StorageInformationException
     */
    StorageInformation getStorageInformation () throws StorageInformationException;


    /**
     * @return the platform information
     * @throws SystemInformationException
     */
    PlatformInformation getPlatformInformation () throws SystemInformationException;

}