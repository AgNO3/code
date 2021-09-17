/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return the default storage information impl
     */
    public StorageInformation createStorageInformation () {
        return new StorageInformationImpl();
    }

}
