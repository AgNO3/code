/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.09.2014 by mbechler
 */
package eu.agno3.runtime.update;


import java.net.URI;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author mbechler
 *
 */
public interface UpdateManager extends LocalUpdateManagerMBean, AutoCloseable {

    /**
     * @param repositories
     * @param monitor
     * @throws UpdateException
     */
    void installAllUpdates ( Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException;


    /**
     * @param updates
     * @param repositories
     * @param monitor
     * @throws UpdateException
     */
    void installUpdates ( Set<Feature> updates, Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException;


    /**
     * @param monitor
     * @return the IUs which can be updated
     * @throws UpdateException
     */
    Set<FeatureUpdate> checkForUpdates ( IProgressMonitor monitor ) throws UpdateException;


    /**
     * @param spec
     * @param monitor
     * @throws UpdateException
     */
    void installFeature ( String spec, IProgressMonitor monitor ) throws UpdateException;


    /**
     * @param spec
     * @param monitor
     * @throws UpdateException
     */
    void removeFeature ( String spec, IProgressMonitor monitor ) throws UpdateException;


    /**
     * Resolved the desired update and downloads the necessary artifacts
     * 
     * @param repositories
     * @param targets
     * @param monitor
     * @return whether any changes are required
     * @throws UpdateException
     */
    boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets, IProgressMonitor monitor ) throws UpdateException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close ();

}