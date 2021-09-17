/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.agent;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface AuthenticationBackendConfigurator <T extends AuthenticatorConfig> {

    /**
     * 
     * @param b
     * @param ctx
     * @param config
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     * @throws InvalidParameterException
     */
    void configure ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, T config )
            throws JobBuilderException, UnitInitializationFailedException, InvalidParameterException, ServiceManagementException;


    /**
     * @return the type configured by this backend
     */
    Class<T> getConfigType ();

}
