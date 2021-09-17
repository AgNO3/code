/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentCommunicationErrorFault;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.jobs.JobInfo;


/**
 * @author mbechler
 *
 */
public interface AgentServerService {

    /**
     * @param instance
     * @return the id of the instnace's agent
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     */
    UUID getAgentID ( @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException;


    /**
     * @param instance
     * @return the message target for the instance's agent
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     */
    AgentMessageTarget getMessageTarget ( @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException;


    /**
     * @param instance
     * @return the agents message target
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    AgentMessageTarget ensureAgentOnline ( @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentOfflineException;


    /**
     * @param persistent
     * @return whether the agent is believed to be online
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    boolean isAgentOnline ( @Nullable InstanceStructuralObject persistent ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param cause
     * @param instance
     * @return a comm fault object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     */
    AgentCommunicationErrorFault handleCommFault ( @Nullable Throwable cause, @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException;


    /**
     * @param instance
     * @param j
     * @return the job info for the submitted job
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    JobInfo submitJob ( @Nullable InstanceStructuralObject instance, JobImpl j )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentCommunicationErrorException;

}
