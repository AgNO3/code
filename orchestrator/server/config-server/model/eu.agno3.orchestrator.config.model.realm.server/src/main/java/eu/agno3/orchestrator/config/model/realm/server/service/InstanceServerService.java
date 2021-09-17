/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.UUID;

import javax.persistence.EntityManager;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.service.InstanceService;


/**
 * @author mbechler
 * 
 */
public interface InstanceServerService extends InstanceService {

    /**
     * 
     * @param agentId
     * @return the instance that this agent is attached to
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    InstanceStructuralObject getInstanceForAgent ( UUID agentId ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param host
     * @return the attached agent id
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    UUID getAgentId ( InstanceStructuralObject host ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param em
     * @param agentId
     * @return the instance for the agent
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    InstanceStructuralObjectImpl getInstanceForAgent ( EntityManager em, UUID agentId ) throws ModelObjectNotFoundException, ModelServiceException;

}
