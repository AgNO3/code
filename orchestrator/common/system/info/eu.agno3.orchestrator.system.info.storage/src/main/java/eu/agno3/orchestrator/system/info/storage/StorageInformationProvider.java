/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage;


import eu.agno3.orchestrator.system.info.SystemInformationProvider;


/**
 * @author mbechler
 * 
 */
public interface StorageInformationProvider extends SystemInformationProvider<StorageInformation> {

    /**
     * @return the storage information
     * @throws StorageInformationException
     */
    @Override
    StorageInformation getInformation () throws StorageInformationException;


    /**
     * Rescan partition tables
     * 
     * @throws StorageInformationException
     */
    void rescanPartitions () throws StorageInformationException;

}
