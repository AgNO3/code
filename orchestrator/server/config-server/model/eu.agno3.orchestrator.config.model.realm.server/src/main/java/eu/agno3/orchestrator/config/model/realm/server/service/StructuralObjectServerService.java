/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.Set;

import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectState;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;


/**
 * @author mbechler
 *
 */
public interface StructuralObjectServerService extends StructuralObjectService {

    /**
     * @param em
     * @return the structure root
     * @throws ModelServiceException
     */
    StructuralObjectMutable getStructureRoot ( EntityManager em ) throws ModelServiceException;


    /**
     * @param em
     * @param parent
     * @param toCreate
     * @param skipRequirements
     * @return the newly created object
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     */
    <@NonNull T extends StructuralObject> T create ( EntityManager em, StructuralObject parent, T toCreate, boolean skipRequirements )
            throws ModelServiceException, ModelObjectConflictException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
            ModelObjectValidationException;


    /**
     * @param em
     * @param parent
     * @param toCreate
     * @param skipRequirements
     * @return the newly created object
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     */
    <@NonNull T extends StructuralObject> T createWithId ( EntityManager em, StructuralObject parent, T toCreate, boolean skipRequirements )
            throws ModelServiceException, ModelObjectConflictException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
            ModelObjectValidationException;


    /**
     * @param em
     * @param toUpdate
     * @return the updated object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     */
    <@NonNull T extends StructuralObject> T update ( EntityManager em, T toUpdate )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException;


    /**
     * @param em
     * @param instance
     * @param bootstrapping
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void setObjectState ( EntityManager em, InstanceStructuralObject instance, StructuralObjectState bootstrapping )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param em
     * @param instance
     * @param ignoreServicesTypes
     * @return the updated object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     */
    <T extends StructuralObject> T createRequirements ( EntityManager em, @NonNull T instance, Set<String> ignoreServicesTypes )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException, ModelObjectValidationException;

}
