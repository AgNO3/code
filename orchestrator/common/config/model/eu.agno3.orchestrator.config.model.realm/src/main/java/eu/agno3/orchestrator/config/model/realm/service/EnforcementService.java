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
@WebService ( targetNamespace = EnforcementServiceDescriptor.NAMESPACE )
public interface EnforcementService extends SOAPWebService {

    /**
     * @param obj
     * @return the enforcmenets attached to the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "enforcements" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchEnforcements ( @WebParam ( name = "obj" ) StructuralObject obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @return the enforcements currently applied to the given object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> @Nullable T getAppliedEnforcement ( @WebParam ( name = "obj" ) T obj ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * 
     * @param obj
     * @param objType
     * @return the enforcments for the object type at the given structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    ConfigurationObject getEnforcementsFor ( @WebParam ( name = "obj" ) StructuralObject obj, String objType ) throws ModelObjectNotFoundException,
            ModelServiceException;


    /**
     * @param anchor
     * @param object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void setEnforcement ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "config" ) ConfigurationObject object )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param anchor
     * @param object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    void unsetEnforcement ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "config" ) ConfigurationObject object )
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param selectedObject
     * @return the inherited enforcments applied at the structural objects
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "inherited" )
    @XmlElementWrapper
    Set<ConfigurationObject> fetchInheritedEnforcements ( @WebParam ( name = "anchor" ) StructuralObject selectedObject )
            throws ModelObjectNotFoundException, ModelServiceException;

}
