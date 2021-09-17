/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return default system configuration impl
     */
    public StorageConfiguration createStorageConfiguration () {
        return new StorageConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public LocalMountEntry createLocalMountEntry () {
        return new LocalMountEntryImpl();
    }


    /**
     * 
     * @return default impl
     */
    public CIFSMountEntry createCIFSMountEntry () {
        return new CIFSMountEntryImpl();
    }


    /**
     * 
     * @return default impl
     */
    public NFSMountEntry createNFSMountEntry () {
        return new NFSMountEntryImpl();
    }
}
