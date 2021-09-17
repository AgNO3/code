/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.List;
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
import eu.agno3.orchestrator.config.model.realm.ObjectTypeTreeNode;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = ConfigurationServiceDescriptor.NAMESPACE )
public interface ConfigurationService extends SOAPWebService {

    /**
     * 
     * @param refresh
     * @return a refreshed version of the same object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "refreshed" )
    <T extends ConfigurationObject> T refresh ( @WebParam ( name = "refresh" ) T refresh ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param id
     * @return the configuration object specified by id
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "obj" )
    ConfigurationObject fetchById ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "id" ) UUID id )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * Creates a configuration object attached to the given anchor
     * 
     * If the given object is anonymous (service local) and contains references to not yet existant anyonymous objects
     * these will be created too. They will be attached to the same anchor and also be anonymous.
     * 
     * @param anchor
     * @param create
     * @param info
     * @return the updated object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     */
    @WebResult ( name = "updated" )
    <T extends ConfigurationObject> T create ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "create" ) T create,
            @WebParam ( name = "info" ) ConfigUpdateInfo info ) throws ModelObjectNotFoundException, ModelServiceException,
            ModelObjectValidationException;


    /**
     * Updates a configuration object
     * 
     * If the object to update is anonymous and does contain not yet existant anonymous references these will be created
     * locally (anonymous).
     * 
     * If the object contains references to local anonymous objects these will be also updated using the given values.
     * 
     * @param anchor
     * @param update
     * @param info
     * @return the updated object
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectConflictException
     */
    @WebResult ( name = "updated" )
    <T extends ConfigurationObject> T update ( @WebParam ( name = "update" ) T update, @WebParam ( name = "info" ) ConfigUpdateInfo info )
            throws ModelServiceException, ModelObjectValidationException, ModelObjectNotFoundException, ModelObjectConflictException;


    /**
     * 
     * @param obj
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     *             if other objects still depend on the object
     */
    void delete ( @WebParam ( name = "delete" ) ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException,
            ModelObjectReferentialIntegrityException;


    /**
     * @param obj
     * @return the configuration objects that reference the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "usedBy" )
    @XmlElementWrapper
    Set<ConfigurationObject> getUsedBy ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelServiceException,
            ModelObjectNotFoundException;


    /**
     * @param obj
     * @return the configuration objects that are reference by the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "uses" )
    @XmlElementWrapper
    Set<ConfigurationObject> getUses ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelServiceException,
            ModelObjectNotFoundException;


    /**
     * 
     * @return the known object type names
     * @throws ModelServiceException
     */
    @WebResult ( name = "types" )
    @XmlElementWrapper
    Set<String> getObjectTypes () throws ModelServiceException;


    /**
     * 
     * @param objectType
     * @return the types that can be used as a object of objectType
     * @throws ModelServiceException
     */
    @WebResult ( name = "types" )
    @XmlElementWrapper
    Set<String> getApplicableTypes ( @WebParam ( name = "objectType" ) String objectType ) throws ModelServiceException;


    /**
     * @param objectType
     * @return an empty object of the given type
     * @throws ModelServiceException
     */
    @WebResult ( name = "obj" )
    ConfigurationObject getEmpty ( @WebParam ( name = "objectType" ) String objectType ) throws ModelServiceException;


    /**
     * 
     * @param obj
     * @return the anchor for the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "anchor" )
    StructuralObject getAnchor ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @return the object type trees for all root types
     * @throws ModelServiceException
     */
    @WebResult ( name = "types" )
    List<ObjectTypeTreeNode> getObjectTypeTrees () throws ModelServiceException;

}
