/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class StorageConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<StorageConfiguration, StorageConfigurationImpl> {

    /**
     * 
     */
    public StorageConfigurationObjectTypeDescriptor () {
        super(
            StorageConfiguration.class,
            StorageConfigurationImpl.class,
            HostConfigurationMessages.BASE_PACKAGE,
            "urn:agno3:objects:1.0:hostconfig:storage"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull StorageConfiguration getGlobalDefaults () {
        StorageConfigurationImpl sc = new StorageConfigurationImpl();
        sc.setBackupStorage("system"); //$NON-NLS-1$
        return sc;
    }


    /**
     * @return empty instance
     */
    public static StorageConfigurationMutable emptyInstance () {
        return new StorageConfigurationImpl();
    }

}
