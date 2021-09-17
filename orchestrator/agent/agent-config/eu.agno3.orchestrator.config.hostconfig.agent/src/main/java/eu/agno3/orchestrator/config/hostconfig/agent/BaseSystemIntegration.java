/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.MatcherException;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;


/**
 * @author mbechler
 * 
 */
public interface BaseSystemIntegration extends SystemService {

    /**
     * @param b
     * @param ctx
     * @param hostName
     * @throws BaseSystemException
     * @throws MatcherException
     */
    void setHostName ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) throws BaseSystemException,
            MatcherException;


    /**
     * @param b
     * @param ctx
     * @param tzName
     * @throws BaseSystemException
     * @throws MatcherException
     */
    void setTimezone ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) throws BaseSystemException,
            MatcherException;


    /**
     * 
     * @param b
     * @param ctx
     * @param hwClockUTC
     * @throws BaseSystemException
     * @throws MatcherException
     */
    void setHwClockUTC ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) throws BaseSystemException,
            MatcherException;


    /**
     * 
     * @param service
     * @return a mapped service name
     */
    String mapServiceName ( String service );


    /**
     * @param b
     * @param delaySecs
     * @return the configurator
     * @throws BaseSystemException
     * @throws UnitInitializationFailedException
     */
    AbstractConfigurator<?, ?, ?> reboot ( JobBuilder b, int delaySecs ) throws BaseSystemException, UnitInitializationFailedException;


    /**
     * 
     * @param b
     * @param delaySecs
     * @return the configurator
     * @throws BaseSystemException
     * @throws UnitInitializationFailedException
     */
    AbstractConfigurator<?, ?, ?> shutdown ( JobBuilder b, int delaySecs ) throws BaseSystemException, UnitInitializationFailedException;
}
