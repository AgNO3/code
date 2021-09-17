/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;


/**
 * @author mbechler
 *
 */
@Component ( service = ExecutionConfigProperties.class, configurationPid = ExecutionConfigPropertiesImpl.PID )
public class ExecutionConfigPropertiesImpl implements ExecutionConfigProperties {

    private static final String PREFIX_PROPERTY = "prefix"; //$NON-NLS-1$
    private static final String NO_VERIFY_ENV_PROPERTY = "noVerifyEnv"; //$NON-NLS-1$
    private static final String ALWAYS_CREATE_TARGETS_PROPERTY = "alwaysCreateTargets"; //$NON-NLS-1$
    private static final String DRY_RUN_PROPERTY = "dryRun"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String ROOT = "/"; //$NON-NLS-1$

    /**
     * Configuration PID
     */
    public static final String PID = "system.config"; //$NON-NLS-1$

    private boolean dryRun = false;
    private boolean alwaysCreateTargets = false;
    private boolean noVerifyEnv = false;
    private Path prefix = Paths.get(ROOT);


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.configureFromProperties(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        this.configureFromProperties(ctx.getProperties());
    }


    /**
     * @param properties
     */
    private void configureFromProperties ( Dictionary<String, Object> properties ) {

        String dryRunSpec = (String) properties.get(DRY_RUN_PROPERTY);
        if ( dryRunSpec != null ) {
            this.dryRun = Boolean.parseBoolean(dryRunSpec);
        }

        String createTargetsSpec = (String) properties.get(ALWAYS_CREATE_TARGETS_PROPERTY);
        if ( createTargetsSpec != null ) {
            this.alwaysCreateTargets = Boolean.parseBoolean(createTargetsSpec);
        }

        String noVerifyEnvSpec = (String) properties.get(NO_VERIFY_ENV_PROPERTY);
        if ( noVerifyEnvSpec != null ) {
            this.noVerifyEnv = Boolean.parseBoolean(noVerifyEnvSpec);
        }

        String prefixSpec = (String) properties.get(PREFIX_PROPERTY);
        if ( prefixSpec != null ) {
            this.prefix = Paths.get(prefixSpec);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#isDryRun()
     */
    @Override
    public boolean isDryRun () {
        return this.dryRun;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#getPrefix()
     */
    @Override
    public Path getPrefix () {
        return this.prefix;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#isAlwaysCreateTargets()
     */
    @Override
    public boolean isAlwaysCreateTargets () {
        return this.alwaysCreateTargets;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#isNoVerifyEnv()
     */
    @Override
    public boolean isNoVerifyEnv () {
        return this.noVerifyEnv;
    }
}
