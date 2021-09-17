/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server;


import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.system.update.jobs.UpdateCheckJob;
import eu.agno3.orchestrator.system.update.service.AgentUpdateService;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface UpdateServiceInternal extends AgentUpdateService {

    /**
     * @param inst
     * @param stream
     */
    void foundUpdates ( InstanceStructuralObject inst, String stream );


    /**
     * @param instance
     * @param updatedStream
     * @param updatedSequence
     * @param rebootIndicated
     */
    void updated ( InstanceStructuralObject instance, String updatedStream, long updatedSequence, boolean rebootIndicated );


    /**
     * @param instance
     * @param revertedToStream
     * @param revertedToSequence
     */
    void reverted ( @NonNull InstanceStructuralObject instance, String revertedToStream, long revertedToSequence );


    /**
     * @param extraStreams
     * @param owner
     * @return an update job
     */
    UpdateCheckJob createUpdateJob ( Set<String> extraStreams, UserPrincipal owner );

}
