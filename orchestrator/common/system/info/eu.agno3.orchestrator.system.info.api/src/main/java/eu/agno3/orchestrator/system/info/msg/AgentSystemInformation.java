/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.msg;


import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;


/**
 * @author mbechler
 * 
 */
public class AgentSystemInformation {

    private PlatformInformation platformInformation;
    private NetworkInformation networkInformation;
    private StorageInformation storageInformation;


    /**
     * @return the platformInformation
     */
    public PlatformInformation getPlatformInformation () {
        return this.platformInformation;
    }


    /**
     * @param platformInformation
     *            the platformInformation to set
     */
    public void setPlatformInformation ( PlatformInformation platformInformation ) {
        this.platformInformation = platformInformation;
    }


    /**
     * @return the networkInformation
     */
    public NetworkInformation getNetworkInformation () {
        return this.networkInformation;
    }


    /**
     * @param networkInformation
     *            the networkInformation to set
     */
    public void setNetworkInformation ( NetworkInformation networkInformation ) {
        this.networkInformation = networkInformation;
    }


    /**
     * @return the storageInformation
     */
    public StorageInformation getStorageInformation () {
        return this.storageInformation;
    }


    /**
     * @param storageInformation
     *            the storageInformation to set
     */
    public void setStorageInformation ( StorageInformation storageInformation ) {
        this.storageInformation = storageInformation;
    }
}
