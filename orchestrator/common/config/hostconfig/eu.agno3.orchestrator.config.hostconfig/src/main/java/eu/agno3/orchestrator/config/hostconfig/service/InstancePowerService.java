/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.service;


import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = InstancePowerServiceDescriptor.NAMESPACE )
public interface InstancePowerService extends SOAPWebService {

    /**
     * 
     * @param instance
     * @return the reboot job
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public JobInfo reboot ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @param instance
     * @return the shutdown job
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public JobInfo shutdown ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException;
}