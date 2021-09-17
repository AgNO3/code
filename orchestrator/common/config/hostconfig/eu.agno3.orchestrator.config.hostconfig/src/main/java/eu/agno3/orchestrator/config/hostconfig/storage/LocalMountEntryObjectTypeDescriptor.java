/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class LocalMountEntryObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LocalMountEntry, LocalMountEntryImpl> {

    /**
     * 
     */
    public LocalMountEntryObjectTypeDescriptor () {
        super(LocalMountEntry.class, LocalMountEntryImpl.class, HostConfigurationMessages.BASE_PACKAGE, MountEntryObjectTypeDescriptor.TYPE_NAME);
    }

}
