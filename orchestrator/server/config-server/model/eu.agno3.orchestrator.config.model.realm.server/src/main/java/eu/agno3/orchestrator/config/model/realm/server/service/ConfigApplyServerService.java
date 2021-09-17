/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.events.ServiceConfigAppliedEvent;
import eu.agno3.orchestrator.config.model.events.ServiceConfigFailedEvent;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService;
import eu.agno3.orchestrator.jobs.JobInfo;


/**
 * @author mbechler
 *
 */
public interface ConfigApplyServerService extends ConfigApplyService {

    /**
     * @param em
     * @param service
     * @param revision
     * @param info
     * @return job info for the configuration job
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    JobInfo applyConfiguration ( EntityManager em, ServiceStructuralObject service, Long revision, ConfigApplyInfo info )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException;


    /**
     * @param event
     */
    void handleConfigApplied ( ServiceConfigAppliedEvent event );


    /**
     * @param event
     */
    void handleConfigFailed ( ServiceConfigFailedEvent event );


    /**
     * @param em
     * @param persistent
     * @param appRev
     * @param updateInstanceState
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    void setAppliedRevision ( EntityManager em, ServiceStructuralObject persistent, boolean updateInstanceState, @Nullable Long appRev )
            throws ModelObjectNotFoundException, ModelServiceException;

}
