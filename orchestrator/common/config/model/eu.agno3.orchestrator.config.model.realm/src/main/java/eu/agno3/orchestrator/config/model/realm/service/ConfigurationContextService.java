/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = ConfigurationContextServiceDescriptor.NAMESPACE )
public interface ConfigurationContextService extends SOAPWebService {

    /**
     * @param anchor
     * @param objectType
     * @return an edit context
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "context" )
    ConfigurationEditContext<@NonNull ConfigurationObject, ConfigurationObject> newForEditing (
            @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "objectType" ) String objectType )
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param id
     * @return an edit context
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "context" )
    ConfigurationEditContext<@NonNull ConfigurationObject, ConfigurationObject> getForEditing (
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) @WebParam ( name = "id" ) UUID id ) throws ModelServiceException,
            ModelObjectNotFoundException;
}
