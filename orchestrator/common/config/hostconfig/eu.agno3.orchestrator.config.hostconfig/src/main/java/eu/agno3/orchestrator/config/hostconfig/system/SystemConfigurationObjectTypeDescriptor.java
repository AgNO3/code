/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.system;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationImpl;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class SystemConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<SystemConfiguration, SystemConfigurationImpl> {

    /**
     * 
     */
    public SystemConfigurationObjectTypeDescriptor () {
        super(SystemConfiguration.class, SystemConfigurationImpl.class, HostConfigurationMessages.BASE_PACKAGE, "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }

    private static final UUID DEFAULT_AGENT_CONFIG_ID = UUID.fromString("8f5f298e-e7ca-46e5-b8bf-23618bea9263"); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull SystemConfiguration getGlobalDefaults () {
        SystemConfigurationMutable defaults = emptyInstance();
        defaults.setSwapiness(60);
        defaults.setEnableSshAccess(false);
        defaults.setSshKeyOnly(false);

        @NonNull
        RuntimeConfigurationImpl agentConfig = RuntimeConfigurationObjectTypeDescriptor.emptyInstance();
        agentConfig.setId(DEFAULT_AGENT_CONFIG_ID);
        agentConfig.setAutoMemoryLimit(false);
        agentConfig.setMemoryLimit(160L * 1024 * 1024);
        defaults.setAgentConfig(agentConfig);
        return defaults;
    }


    /**
     * @return an empty instance
     */
    public static SystemConfigurationMutable emptyInstance () {
        SystemConfigurationImpl sys = new SystemConfigurationImpl();
        sys.setAgentConfig(RuntimeConfigurationObjectTypeDescriptor.emptyInstance());
        return sys;
    }

}
