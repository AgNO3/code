/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.server.service;


import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;


/**
 * @author mbechler
 *
 */
public interface BootstrapServerService {

    /**
     * @param context
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void afterApplyConfig ( BootstrapContext context ) throws ModelObjectNotFoundException, ModelServiceException;

}
