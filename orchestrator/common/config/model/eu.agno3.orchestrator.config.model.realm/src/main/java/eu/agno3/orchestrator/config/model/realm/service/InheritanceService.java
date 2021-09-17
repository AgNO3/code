/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = InheritanceServiceDescriptor.NAMESPACE )
public interface InheritanceService extends SOAPWebService {

    /**
     * 
     * @param obj
     * @param rootObjectType
     * @return the effective inherited values currently applied to the given object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "inherited" )
    <T extends ConfigurationObject> T getInherited ( @WebParam ( name = "obj" ) T obj, String rootObjectType)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param obj
     * @param rootObjectType
     * @return the effective values for the obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "effective" )
    <T extends ConfigurationObject> T getEffective ( @WebParam ( name = "obj" ) T obj, String rootObjectType)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param anchor
     * @param objType
     * @param filter
     * @return the templates of objType that are usable for inheritance at the given location
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "templates" )
    @XmlElementWrapper
    List<ConfigurationObjectReference> getEligibleTemplates ( @WebParam ( name = "anchor" ) StructuralObject anchor,
            @WebParam ( name = "objType" ) String objType, @WebParam ( name = "filter" ) String filter)
                    throws ModelObjectNotFoundException, ModelServiceException;

}
