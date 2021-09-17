/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = UsageServiceDescriptor.NAMESPACE )
public interface UsageService extends SOAPWebService {

    /**
     * 
     * @param obj
     * @return the objects that directly use the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "usedBy" )
    @XmlElementWrapper
    Set<ConfigurationObject> getUsedBy ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects that transitivly use the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "usedBy" )
    @XmlElementWrapper
    Set<ConfigurationObject> getUsedByClosure ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects that are directly used by the given object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "uses" )
    @XmlElementWrapper
    Set<ConfigurationObject> getUses ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects that are transitively used by the given object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "uses" )
    @XmlElementWrapper
    Set<ConfigurationObject> getUsesClosure ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects that inherit from the given object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "inheritedBy" )
    @XmlElementWrapper
    Set<ConfigurationObject> getInheritedBy ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects that the given object inherits from
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "inherits" )
    @XmlElementWrapper
    Set<ConfigurationObject> getInherits ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects for which this object acts as default
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "defaultFor" )
    @XmlElementWrapper
    Set<ConfigurationObject> getDefaultFor ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the object for which this object acts as enforcement
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "enforcedFor" )
    @XmlElementWrapper
    Set<ConfigurationObject> getEnforcedFor ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return all objects that might be affected by changes to the object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "affected" )
    @XmlElementWrapper
    Set<ConfigurationObject> getAffects ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * @param obj
     * @return all objects that might affect the given object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "affected" )
    @XmlElementWrapper
    Set<ConfigurationObject> getAffectedBy ( @WebParam ( name = "obj" ) ConfigurationObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * @param obj
     * @return the services that might be affected by changed to the object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "affectedService" )
    @XmlElementWrapper
    Set<ServiceStructuralObject> getAffectsServices ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param obj
     * @return the objects for which this object acts as default
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "defaultFor" )
    @XmlElementWrapper
    Set<? extends StructuralObject> getDefaultForStructure ( @WebParam ( name = "obj" ) ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param obj
     * @return the object for which this object acts as enforcement
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "enforcedFor" )
    @XmlElementWrapper
    Set<? extends StructuralObject> getEnforcedForStructure ( @WebParam ( name = "obj" ) ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException;

}
