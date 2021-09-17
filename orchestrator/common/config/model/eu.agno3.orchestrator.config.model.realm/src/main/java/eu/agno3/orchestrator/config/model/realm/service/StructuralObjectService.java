/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.Set;
import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = StructuralObjectServiceDescriptor.NAMESPACE )
public interface StructuralObjectService extends SOAPWebService {

    /**
     * 
     * @param refresh
     * @return a refreshed version of the same object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "refreshed" )
    <T extends StructuralObject> T refresh ( @WebParam ( name = "refresh" ) T refresh ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param id
     * @return the structural object specified by id
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "obj" )
    StructuralObject fetchById ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "id" ) UUID id )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @return the structural root
     * @throws ModelServiceException
     */
    @WebResult ( name = "root" )
    StructuralObjectMutable getStructureRoot () throws ModelServiceException;


    /**
     * @param parent
     * @param toCreate
     * @return the created object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     */
    @WebResult ( name = "created" )
    <T extends StructuralObject> T create ( @WebParam ( name = "parent" ) StructuralObject parent, @WebParam ( name = "new" ) T toCreate )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectConflictException, ModelObjectReferentialIntegrityException,
            ModelObjectValidationException;


    /**
     * 
     * @param toUpdate
     * @return the updated object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     */
    @WebResult ( name = "updated" )
    <T extends StructuralObject> T update ( @WebParam ( name = "update" ) T toUpdate ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectValidationException;


    /**
     * 
     * @param obj
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void delete ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param obj
     * @return the objects structural children
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "children" )
    @XmlElementWrapper
    Set<StructuralObjectMutable> fetchChildren ( @WebParam ( name = "obj" ) StructuralObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the parent of object, null if this is the structural root
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "parent" )
    StructuralObjectMutable fetchParent ( @WebParam ( name = "obj" ) StructuralObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * @param obj
     * @return the configuration objects attached to the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "obj" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchAttachedObjects ( @WebParam ( name = "obj" ) StructuralObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * @param obj
     * @return the defaults attached to the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "defaults" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchDefaults ( @WebParam ( name = "obj" ) StructuralObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * @param obj
     * @return the enforments attached to the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "enforcements" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchEnforcements ( @WebParam ( name = "obj" ) StructuralObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;

}
