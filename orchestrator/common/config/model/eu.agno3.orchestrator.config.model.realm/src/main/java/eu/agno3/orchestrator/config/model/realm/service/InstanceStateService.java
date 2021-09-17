/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStatus;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = InstanceStateServiceDescriptor.NAMESPACE )
public interface InstanceStateService extends SOAPWebService {

    /**
     * @param host
     * @return instance state
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "state" )
    InstanceStatus getState ( @WebParam ( name = "host" ) InstanceStructuralObject host) throws ModelServiceException, ModelObjectNotFoundException;

}
