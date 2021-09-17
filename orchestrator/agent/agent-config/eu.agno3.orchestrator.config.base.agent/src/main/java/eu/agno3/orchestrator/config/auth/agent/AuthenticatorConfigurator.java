/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.agent;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.AuthenticatorsConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfig;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public interface AuthenticatorConfigurator {

    /**
     * @param b
     * @param ctx
     * @param ac
     * @throws JobBuilderException
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    <T extends AuthenticatorConfig> void setupAuthenticator ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, T ac )
            throws JobBuilderException, InvalidParameterException, UnitInitializationFailedException, ServiceManagementException;


    /**
     * @param b
     * @param ctx
     * @param acs
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws JobBuilderException
     */
    void setupAuthenticators ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, AuthenticatorsConfig acs )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException, JobBuilderException;


    /**
     * @param b
     * @param ctx
     * @param rcs
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    void setupRoles ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, StaticRolesConfig rcs )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException;

}