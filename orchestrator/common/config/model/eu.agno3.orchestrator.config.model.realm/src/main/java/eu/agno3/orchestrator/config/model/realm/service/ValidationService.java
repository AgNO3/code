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

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = ValidationServiceDescriptor.NAMESPACE )
public interface ValidationService extends SOAPWebService {

    /**
     * 
     * @param obj
     * @param anchor
     * @return violation entries
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectException
     * @throws ModelObjectValidationException
     */
    @WebResult ( name = "violations" )
    @XmlElementWrapper
    <T extends ConfigurationObject> List<ViolationEntry> validateObject ( @WebParam ( name = "obj" ) T obj,
            @WebParam ( name = "anchor" ) StructuralObject anchor ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectValidationException, ModelObjectException;

}
