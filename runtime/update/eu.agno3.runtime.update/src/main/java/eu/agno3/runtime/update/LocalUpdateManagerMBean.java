/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.runtime.update;


import java.net.URI;
import java.util.Set;

import javax.management.NotificationEmitter;


/**
 * @author mbechler
 *
 */
public interface LocalUpdateManagerMBean extends NotificationEmitter {

    /**
     * @return the repositories which are used for updates
     * @throws UpdateException
     */
    URI[] listRepositories () throws UpdateException;


    /**
     * @return the installed features
     * @throws UpdateException
     * 
     */
    Set<Feature> listInstalledFeatures () throws UpdateException;


    /**
     * @param updates
     * @param repositories
     * @throws UpdateException
     */
    void installUpdates ( Set<Feature> updates, Set<URI> repositories ) throws UpdateException;


    /**
     * @param repositories
     * @param monitor
     * @throws UpdateException
     */
    void installAllUpdates ( Set<URI> repositories ) throws UpdateException;


    /**
     * @param monitor
     * @return the IUs which can be updated
     * @throws UpdateException
     */
    Set<FeatureUpdate> checkForUpdates () throws UpdateException;


    /**
     * @throws UpdateException
     */
    void runGarbageCollection () throws UpdateException;


    /**
     * @return the not locally installed features available in the repositories
     * @throws UpdateException
     */
    Set<Feature> getInstallableFeatures () throws UpdateException;


    /**
     * @param spec
     * @throws UpdateException
     */
    void installFeature ( String spec ) throws UpdateException;


    /**
     * @param spec
     * @throws UpdateException
     */
    void removeFeature ( String spec ) throws UpdateException;


    /**
     * @param repositories
     * @param targets
     * @return whether any changes are required
     * @throws UpdateException
     */
    boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets ) throws UpdateException;


    /**
     * Applies the bundle changes for an external update
     * 
     * @throws UpdateException
     */
    void doApplyUpdate () throws UpdateException;
}
