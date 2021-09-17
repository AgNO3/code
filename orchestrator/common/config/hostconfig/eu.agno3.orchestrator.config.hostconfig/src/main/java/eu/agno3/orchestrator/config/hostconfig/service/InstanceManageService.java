/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.service;


import javax.jws.WebService;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = InstanceManageServiceDescriptor.NAMESPACE )
public interface InstanceManageService extends SOAPWebService {

    /**
     * @param host
     * @param oldCreds
     * @param newCreds
     * @return password set job info
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @NonNull
    JobInfo setAdministratorPassword ( InstanceStructuralObject host, WrappedCredentials oldCreds, WrappedCredentials newCreds )
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException;

}
