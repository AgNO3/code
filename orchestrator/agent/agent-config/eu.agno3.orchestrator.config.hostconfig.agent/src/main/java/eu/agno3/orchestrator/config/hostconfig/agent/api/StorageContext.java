/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.api;


import java.nio.file.Path;


/**
 * @author mbechler
 *
 */
public class StorageContext {

    private String princ;
    private Path localStorage;
    private Path sharedStorage;


    /**
     * @param princ
     * @param storageBase
     * @param serviceLocalStorage
     * @param serviceSharedStorage
     */
    public StorageContext ( String princ, Path storageBase, Path serviceLocalStorage, Path serviceSharedStorage ) {
        this.princ = princ;
        this.localStorage = serviceLocalStorage;
        this.sharedStorage = serviceSharedStorage;
    }


    /**
     * 
     * @return the storage user
     */
    public String getStorageUser () {
        return this.princ;
    }


    /**
     * 
     * @return the storage group
     */
    public String getStorageGroup () {
        return this.princ;
    }


    /**
     * @return the localStorage
     */
    public Path getLocalStorage () {
        return this.localStorage;
    }


    /**
     * @return the sharedStorage
     */
    public Path getSharedStorage () {
        return this.sharedStorage;
    }

}
