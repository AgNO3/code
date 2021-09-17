/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.logger.IPLogAnonymizationType;
import eu.agno3.orchestrator.config.logger.LoggerConfigurationImpl;
import eu.agno3.orchestrator.config.logger.LoggerConfigurationMutable;
import eu.agno3.orchestrator.config.logger.LoggerConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareLoggerConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareLoggerConfig, FileshareLoggerConfigImpl> {

    /**
     * 
     */
    public FileshareLoggerConfigObjectTypeDescriptor () {
        super(FileshareLoggerConfig.class, FileshareLoggerConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareLoggerConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareLoggerConfig getGlobalDefaults () {
        FileshareLoggerConfigImpl lc = new FileshareLoggerConfigImpl();
        LoggerConfigurationMutable def = new LoggerConfigurationImpl();
        def.setRetentionDays(365);
        def.setIpAnonymizationType(IPLogAnonymizationType.NONE);
        lc.setDefaultLoggerConfig(def);

        LoggerConfigurationMutable unauth = new LoggerConfigurationImpl();
        unauth.setRetentionDays(7);
        unauth.setIpAnonymizationType(IPLogAnonymizationType.MASK);
        lc.setUnauthLoggerConfig(unauth);
        return lc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareLoggerConfigMutable emptyInstance () {
        FileshareLoggerConfigImpl lc = new FileshareLoggerConfigImpl();
        lc.setUnauthLoggerConfig(LoggerConfigurationObjectTypeDescriptor.emptyInstance());
        lc.setDefaultLoggerConfig(LoggerConfigurationObjectTypeDescriptor.emptyInstance());
        return lc;
    }

}
