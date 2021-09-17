/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.cxf.annotations.EndpointProperty;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryFileInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = ResourceLibraryServiceDescriptor.NAMESPACE )
@EndpointProperty ( key = "motm-enabled", value = "true" )
public interface ResourceLibraryService extends SOAPWebService {

    /**
     * 
     * @param libraryId
     * @return the resource library
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    ResourceLibrary getById ( @WebParam ( name = "id" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID libraryId)
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param obj
     * @param type
     * @param name
     * @return the library, or null if it does not exist
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "resourceLibrary" )
    ResourceLibrary getByName ( @WebParam ( name = "anchor" ) StructuralObject obj, @WebParam ( name = "name" ) String name,
            @WebParam ( name = "type" ) String type) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param obj
     * @param name
     * @param type
     * @return the library, or null if it does not exist
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "resourceLibrary" )
    ResourceLibrary getClosestByName ( @WebParam ( name = "anchor" ) StructuralObject obj, @WebParam ( name = "name" ) String name,
            @WebParam ( name = "type" ) String type) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param obj
     * @return the resource libraries at anchor
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "resourceLibraries" )
    @XmlElementWrapper
    List<ResourceLibrary> getResourceLibraries ( @WebParam ( name = "anchor" ) StructuralObject obj)
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param obj
     * @param type
     * @param excludeAnchor
     * @return the resource libraries usable at anchor
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "usableResourceLibraries" )
    @XmlElementWrapper
    List<ResourceLibrary> getUsableResourceLibraries ( @WebParam ( name = "anchor" ) StructuralObject obj, @WebParam ( name = "type" ) String type,
            boolean excludeAnchor) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param id
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     */
    void delete ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "id" ) UUID id)
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException;


    /**
     * 
     * @param obj
     * @param parentId
     * @param name
     * @param type
     * @param builtIn
     * @return the created object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    @WebResult ( name = "created" )
    ResourceLibrary create ( @WebParam ( name = "anchor" ) StructuralObject obj,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "parentId" ) UUID parentId, String name, String type,
            boolean builtIn) throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    ModelObjectValidationException, ModelObjectConflictException;


    /**
     * @param resourceLibraryId
     * @return list of files
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "files" )
    @XmlElementWrapper
    List<ResourceLibraryFileInfo> getFiles (
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID resourceLibraryId)
                    throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param selectedLibraryId
     * @return the inherited files
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "files" )
    @XmlElementWrapper
    Set<ResourceLibraryFileInfo> getInheritedFiles (
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID selectedLibraryId)
                    throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param resourceLibraryId
     * @param path
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void removeFile ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID resourceLibraryId,
            @WebParam ( name = "path" ) String path) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param resourceLibraryId
     * @param path
     * @return the file data
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @XmlAttachmentRef
    @XmlMimeType ( "application/octet-stream" )
    DataHandler getFile ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID resourceLibraryId,
            @WebParam ( name = "path" ) String path) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param resourceLibraryId
     * @param create
     * @param path
     * @param data
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectConflictException
     */
    void putFile ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID resourceLibraryId,
            @WebParam ( name = "create" ) boolean create, @WebParam ( name = "path" ) String path,
            @XmlAttachmentRef @XmlMimeType ( "application/octet-stream" ) DataHandler data)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectConflictException;


    /**
     * @param selectedLibraryId
     * @param create
     * @param path
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     */
    void putEmptyFile ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID selectedLibraryId,
            @WebParam ( name = "create" ) boolean create, @WebParam ( name = "path" ) String path)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectConflictException;


    /**
     * 
     * @param selectedLibraryId
     * @param target
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    void synchronize ( @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "resourceLibrary" ) UUID selectedLibraryId,
            @WebParam ( name = "target" ) ServiceStructuralObject target) throws ModelServiceException, ModelObjectNotFoundException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;

}
