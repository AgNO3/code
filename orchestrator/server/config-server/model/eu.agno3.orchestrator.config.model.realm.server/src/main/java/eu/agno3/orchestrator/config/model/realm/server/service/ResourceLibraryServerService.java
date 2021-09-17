/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface ResourceLibraryServerService extends ResourceLibraryService {

    /**
     * @param em
     * @param service
     * @param actualCurrent
     * @param set
     * @param forceSync
     * @param owner
     * @return jobs for synchronization
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    List<Job> makeSynchronizationJob ( EntityManager em, ServiceStructuralObjectImpl service, Set<@NonNull ResourceLibraryReference> set,
            boolean forceSync, @Nullable UserPrincipal owner ) throws ModelServiceException, ModelObjectNotFoundException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * @param service
     * @param library
     * @param hint
     * @param output
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    void synchronizeServiceLibraries ( ServiceStructuralObject service, ResourceLibrary library, @Nullable String hint, JobOutputHandler output )
            throws ModelServiceException, ModelObjectNotFoundException, AgentCommunicationErrorException, AgentDetachedException,
            AgentOfflineException;


    /**
     * @param em
     * @param structureRoot
     * @return the resource libraries at the given anchor
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    List<ResourceLibrary> getResourceLibraries ( EntityManager em, StructuralObject structureRoot )
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param em
     * @param obj
     * @param parentId
     * @param name
     * @param type
     * @param builtin
     * @return the created library
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectConflictException
     */
    ResourceLibrary create ( EntityManager em, StructuralObject obj, @Nullable UUID parentId, String name, String type, boolean builtin )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectReferentialIntegrityException,
            ModelObjectConflictException;


    /**
     * @param em
     * @param lib
     * @param path
     * @param create
     * @param data
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     */
    void putFile ( EntityManager em, ResourceLibrary lib, String path, boolean create, DataHandler data )
            throws ModelServiceException, ModelObjectConflictException;


    /**
     * @param em
     * @param rl
     * @return the combined last modified date of the library
     */
    @Nullable
    DateTime getLastModified ( EntityManager em, ResourceLibrary rl );


    /**
     * 
     * @param anchor
     * @param oldLastMod
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void trackSynchronized ( StructuralObject anchor, DateTime oldLastMod ) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param em
     * @param anchor
     * @param name
     * @param type
     * @return resource library instance
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    ResourceLibrary getClosestByName ( EntityManager em, AbstractStructuralObjectImpl anchor, String name, String type )
            throws ModelObjectNotFoundException, ModelServiceException;

}
