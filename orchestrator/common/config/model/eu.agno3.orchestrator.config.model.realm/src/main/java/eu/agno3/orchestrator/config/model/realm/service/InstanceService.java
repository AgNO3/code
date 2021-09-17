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

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = InstanceServiceDescriptor.NAMESPACE )
public interface InstanceService extends SOAPWebService {

    /**
     * 
     * @param host
     * @return the services on this instance
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "services" )
    @XmlElementWrapper
    Set<ServiceStructuralObject> getServices ( @WebParam ( name = "host" ) InstanceStructuralObject host)
            throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * 
     * @return agents known to the system that are not attached to an instance
     */
    @WebResult ( name = "agents" )
    @XmlElementWrapper
    Set<AgentInfo> getDetachedAgents ();


    /**
     * @return the available image types
     */
    @WebResult ( name = "imageType" )
    @XmlElementWrapper
    Set<String> getAvailableImageTypes ();


    /**
     * @param host
     * @return the connection state of the host's agent
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "componentState" )
    String getAgentState ( @WebParam ( name = "host" ) InstanceStructuralObject host) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param host
     * @return information about the agent for the host
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "agentInfo" )
    AgentInfo getAgentInfo ( @WebParam ( name = "host" ) InstanceStructuralObject host) throws ModelServiceException, ModelObjectNotFoundException;

}
