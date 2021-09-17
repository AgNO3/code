/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractBaseObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class MountEntryObjectTypeDescriptor extends AbstractBaseObjectTypeDescriptor<MountEntry> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:hostconfig:storage:mount"; //$NON-NLS-1$


    /**
     * 
     */
    public MountEntryObjectTypeDescriptor () {
        super(MountEntry.class, HostConfigurationMessages.BASE_PACKAGE, "urn:agno3:objects:1.0:hostconfig:storage:mount"); //$NON-NLS-1$
    }

}
