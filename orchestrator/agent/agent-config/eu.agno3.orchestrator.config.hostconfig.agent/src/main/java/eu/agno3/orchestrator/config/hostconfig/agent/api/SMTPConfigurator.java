/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.api;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfiguration;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public interface SMTPConfigurator {

    /**
     * @param b
     * @param ctx
     * @param mc
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    void setupSMTPClient ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, MailingConfiguration mc )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException;

}
