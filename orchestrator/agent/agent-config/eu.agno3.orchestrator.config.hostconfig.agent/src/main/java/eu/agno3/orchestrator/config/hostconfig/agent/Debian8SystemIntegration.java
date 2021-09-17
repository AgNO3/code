/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.MatcherException;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.units.exec.Exec;


/**
 * @author mbechler
 * 
 */
@Component ( service = BaseSystemIntegration.class )
@SystemServiceType ( BaseSystemIntegration.class )
public class Debian8SystemIntegration implements BaseSystemIntegration {

    private static final String SBIN_SHUTDOWN_FORKED = "/sbin/shutdown.forked"; //$NON-NLS-1$

    private static final String HOSTNAMECTL_COMMAND = "/usr/bin/hostnamectl"; //$NON-NLS-1$
    private static final String TIMEDATECTL = "/usr/bin/timedatectl"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @throws MatcherException
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration#setHostName(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext)
     */
    @Override
    public void setHostName ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws BaseSystemException, MatcherException {
        try {
            boolean changedHostname = ctx.changed(ctx.match().getHostIdentification().getHostName());
            b.add(Exec.class).cmd(HOSTNAMECTL_COMMAND).args("set-hostname", ctx.cfg().getHostIdentification().getHostName()).runIf(changedHostname); //$NON-NLS-1$
        }
        catch ( UnitInitializationFailedException e ) {
            throw new BaseSystemException("Failed to configure hostname update", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws MatcherException
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration#setTimezone(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext)
     */
    @Override
    public void setTimezone ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws BaseSystemException, MatcherException {
        try {
            boolean changedTimezone = ctx.changed(ctx.match().getDateTimeConfiguration().getTimezone());
            b.add(Exec.class).cmd(TIMEDATECTL).args("set-timezone", ctx.cfg().getDateTimeConfiguration().getTimezone().toString()) //$NON-NLS-1$
                    .runIf(changedTimezone);
        }
        catch ( UnitInitializationFailedException e ) {
            throw new BaseSystemException("Failed to configure /etc/timezone update", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws MatcherException
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration#setHwClockUTC(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext)
     */
    @Override
    public void setHwClockUTC ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws BaseSystemException, MatcherException {
        try {
            b.add(Exec.class).cmd(TIMEDATECTL).args("set-local-rtc", String.valueOf(!ctx.cfg().getDateTimeConfiguration().getHwClockUTC())); //$NON-NLS-1$
        }
        catch ( UnitInitializationFailedException e ) {
            throw new BaseSystemException("Failed to configure /etc/default/rcS update", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration#reboot(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      int)
     */
    @Override
    public AbstractConfigurator<?, ?, ?> reboot ( JobBuilder b, int delaySecs ) throws BaseSystemException, UnitInitializationFailedException {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(delaySecs));
        args.add("-r"); //$NON-NLS-1$
        return b.add(Exec.class).cmd(SBIN_SHUTDOWN_FORKED).args(args);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration#shutdown(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      int)
     */
    @Override
    public AbstractConfigurator<?, ?, ?> shutdown ( JobBuilder b, int delaySecs ) throws BaseSystemException, UnitInitializationFailedException {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(delaySecs));
        args.add("-P"); //$NON-NLS-1$
        return b.add(Exec.class).cmd(SBIN_SHUTDOWN_FORKED).args(args);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration#mapServiceName(java.lang.String)
     */
    @Override
    public String mapServiceName ( String service ) {
        return service;
    }

}
