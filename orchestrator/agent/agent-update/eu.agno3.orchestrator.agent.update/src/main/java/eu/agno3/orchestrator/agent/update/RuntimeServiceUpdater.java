/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import java.net.URI;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import eu.agno3.orchestrator.agent.update.units.JobOutputProgressMonitor;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.UpdateException;


/**
 * @author mbechler
 *
 */
public interface RuntimeServiceUpdater extends SystemService {

    /**
     * @param service
     * @param monitor
     * @return all available updates
     * @throws ServiceManagementException
     * @throws UpdateException
     */
    Set<FeatureUpdate> getAllUpdates ( StructuralObjectReference service, IProgressMonitor monitor ) throws ServiceManagementException,
            UpdateException;


    /**
     * @param service
     * @param repositories
     * @param monitor
     * @throws ServiceManagementException
     * @throws UpdateException
     */
    void installAllUpdates ( StructuralObjectReference service, Set<URI> repositories, IProgressMonitor monitor ) throws ServiceManagementException,
            UpdateException;


    /**
     * @param service
     * @param repositories
     * @param updates
     * @param monitor
     * @throws ServiceManagementException
     * @throws UpdateException
     */
    void installUpdates ( StructuralObjectReference service, Set<URI> repositories, Set<Feature> updates, IProgressMonitor monitor )
            throws ServiceManagementException, UpdateException;


    /**
     * @param service
     * @throws ServiceManagementException
     * @throws UpdateException
     */
    void runGarbageCollection ( StructuralObjectReference service ) throws ServiceManagementException, UpdateException;


    /**
     * @param service
     * @return the currently installed features
     * @throws UpdateException
     * @throws ServiceManagementException
     */
    Set<Feature> getInstalled ( StructuralObjectReference service ) throws ServiceManagementException, UpdateException;


    /**
     * @param targetService
     * @param repositories
     * @param targets
     * @param jobOutputProgressMonitor
     * @return whether this should be followed by an install
     * @throws ServiceManagementException
     * @throws UpdateException
     */
    boolean prepareUpdate ( StructuralObjectReference targetService, Set<URI> repositories, Set<Feature> targets,
            JobOutputProgressMonitor jobOutputProgressMonitor ) throws ServiceManagementException, UpdateException;


    /**
     * @param targetService
     * @throws ServiceManagementException
     * @throws UpdateException
     * 
     */
    void doApplyUpdates ( StructuralObjectReference targetService ) throws ServiceManagementException, UpdateException;


    /**
     * @param targetService
     * @throws ServiceManagementException
     * @throws UpdateException
     */
    void doApplyOffline ( StructuralObjectReference targetService ) throws ServiceManagementException, UpdateException;

}
