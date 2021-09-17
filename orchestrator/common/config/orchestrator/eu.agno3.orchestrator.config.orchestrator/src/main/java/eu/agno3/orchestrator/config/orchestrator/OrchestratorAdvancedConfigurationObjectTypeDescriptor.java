/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.i18n.OrchestratorConfigurationMessages;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class OrchestratorAdvancedConfigurationObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<OrchestratorAdvancedConfiguration, OrchestratorAdvancedConfigurationImpl> {

    /**
     * 
     */
    private static final String SYSTEM = "system"; //$NON-NLS-1$


    /**
     * 
     */
    public OrchestratorAdvancedConfigurationObjectTypeDescriptor () {
        super(OrchestratorAdvancedConfiguration.class, OrchestratorAdvancedConfigurationImpl.class, OrchestratorConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull OrchestratorAdvancedConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull OrchestratorAdvancedConfiguration getGlobalDefaults () {
        OrchestratorAdvancedConfigurationImpl oc = new OrchestratorAdvancedConfigurationImpl();
        oc.setRuntimeConfig(RuntimeConfigurationObjectTypeDescriptor.emptyInstance());
        oc.getRuntimeConfig().setAutoMemoryLimit(true);
        oc.getRuntimeConfig().setMemoryLimit(512L * 1024 * 1024);
        oc.setDataStorage(SYSTEM);
        oc.setTempStorage(SYSTEM);
        return oc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull OrchestratorAdvancedConfigurationMutable emptyInstance () {
        OrchestratorAdvancedConfigurationMutable oc = new OrchestratorAdvancedConfigurationImpl();
        oc.setRuntimeConfig(RuntimeConfigurationObjectTypeDescriptor.emptyInstance());
        return oc;
    }
}
