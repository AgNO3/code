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


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class OrchestratorEventLogConfigurationObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<OrchestratorEventLogConfiguration, OrchestratorEventLogConfigurationImpl> {

    /**
     * 
     */
    public OrchestratorEventLogConfigurationObjectTypeDescriptor () {
        super(OrchestratorEventLogConfiguration.class, OrchestratorEventLogConfigurationImpl.class, OrchestratorConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull OrchestratorEventLogConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull OrchestratorEventLogConfiguration getGlobalDefaults () {
        OrchestratorEventLogConfigurationImpl cfg = new OrchestratorEventLogConfigurationImpl();
        cfg.setEventStorage("system"); //$NON-NLS-1$
        cfg.setDisableLogExpiration(false);
        cfg.setRetainDays(180L);
        cfg.setRetainIndexedDays(180L);
        cfg.setWriteLogFiles(true);
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull OrchestratorEventLogConfigurationMutable emptyInstance () {
        return new OrchestratorEventLogConfigurationImpl();
    }

}
