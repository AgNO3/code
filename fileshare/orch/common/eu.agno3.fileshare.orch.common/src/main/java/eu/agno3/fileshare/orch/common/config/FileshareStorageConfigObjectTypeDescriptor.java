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


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareStorageConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareStorageConfig, FileshareStorageConfigImpl> {

    /**
     * 
     */
    private static final String SYSTEM_STORAGE = "system"; //$NON-NLS-1$


    /**
     * 
     */
    public FileshareStorageConfigObjectTypeDescriptor () {
        super(FileshareStorageConfig.class, FileshareStorageConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull FileshareStorageConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareStorageConfig getGlobalDefaults () {
        FileshareStorageConfigImpl fsc = new FileshareStorageConfigImpl();
        fsc.setLocalStorage(SYSTEM_STORAGE);
        fsc.setFileStorage(SYSTEM_STORAGE);
        return fsc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareStorageConfigMutable emptyInstance () {
        return new FileshareStorageConfigImpl();
    }

}
