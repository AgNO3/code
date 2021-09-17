/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.InstanceStateService;
import eu.agno3.runtime.messaging.MessagingException;


/**
 * @author mbechler
 *
 */
public interface InstanceStateServerService extends InstanceStateService {

    /**
     * @param instance
     */
    void handleInstanceConfigApplied ( InstanceStructuralObject instance );


    /**
     * @param instance
     * @throws MessagingException
     * @throws InterruptedException
     */
    void refreshInstanceStatus ( InstanceStructuralObject instance ) throws MessagingException, InterruptedException;

}
