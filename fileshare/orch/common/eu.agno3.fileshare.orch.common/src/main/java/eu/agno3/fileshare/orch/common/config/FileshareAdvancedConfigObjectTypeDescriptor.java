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
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareAdvancedConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareAdvancedConfig, FileshareAdvancedConfigImpl> {

    /**
     * 
     */
    public FileshareAdvancedConfigObjectTypeDescriptor () {
        super(FileshareAdvancedConfig.class, FileshareAdvancedConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull FileshareAdvancedConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareAdvancedConfig getGlobalDefaults () {
        FileshareAdvancedConfigImpl fc = new FileshareAdvancedConfigImpl();
        fc.setRuntimeConfiguration(RuntimeConfigurationObjectTypeDescriptor.emptyInstance());
        fc.getRuntimeConfiguration().setAutoMemoryLimit(true);
        fc.getRuntimeConfiguration().setMemoryLimit(512L * 1024 * 1024);
        return fc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareAdvancedConfigMutable emptyInstance () {
        FileshareAdvancedConfigImpl fac = new FileshareAdvancedConfigImpl();
        fac.setRuntimeConfiguration(RuntimeConfigurationObjectTypeDescriptor.emptyInstance());
        return fac;
    }

}
