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

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = DefaultsServiceDescriptor.NAMESPACE )
public interface DefaultsService extends SOAPWebService {

    /**
     * @param obj
     * @return the defaults attached to the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "defaults" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchDefaults ( @WebParam ( name = "obj" ) StructuralObject obj)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param obj
     * @param rootType
     * @return the defaults currently applied to the given object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> @Nullable T getAppliedDefaults ( @WebParam ( name = "obj" ) T obj, String rootType)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param obj
     * @param objType
     * @param rootType
     * @return the available applicable to the object type at the given structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "defaults" )
    @Nullable
    ConfigurationObject getDefaultsFor ( @WebParam ( name = "obj" ) StructuralObject obj, String objType, String rootType)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param anchor
     * @param object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void setDefault ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "config" ) ConfigurationObject object)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param anchor
     * @param object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    void unsetDefault ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "config" ) ConfigurationObject object)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param selectedObject
     * @return the inherited defaults applied to the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "inherited" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchInheritedDefaults ( @WebParam ( name = "anchor" ) StructuralObject selectedObject)
            throws ModelObjectNotFoundException, ModelServiceException;

}
