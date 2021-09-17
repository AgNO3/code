/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.versioning.VersionInfo;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = RevisionsServiceDescriptor.NAMESPACE )
public interface RevisionsService extends SOAPWebService {

    /**
     * 
     * @param obj
     * @return the revisions of the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "revisions" )
    @XmlElementWrapper
    List<VersionInfo> getRevisions ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param obj
     * @param revision
     * @return the configuration at the given revision
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "config" )
    <T extends ConfigurationObject> T getConfigAtRevision ( @WebParam ( name = "obj" ) T obj, @WebParam ( name = "revision" ) long revision )
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param obj
     * @param revision
     * @return the effective configuration at the revision
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "config" )
    <T extends ConfigurationObject> T getEffectiveAtRevision ( @WebParam ( name = "obj" ) T obj, @WebParam ( name = "revision" ) long revision )
            throws ModelServiceException, ModelObjectNotFoundException;

}
