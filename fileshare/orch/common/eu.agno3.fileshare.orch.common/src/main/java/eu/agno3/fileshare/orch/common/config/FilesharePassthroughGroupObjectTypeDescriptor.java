package eu.agno3.fileshare.orch.common.config;


import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractBaseObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FilesharePassthroughGroupObjectTypeDescriptor extends AbstractBaseObjectTypeDescriptor<FilesharePassthroughGroup> {

    /**
     * 
     */
    public static final String OBJECT_TYPE = "urn:agno3:objects:1.0:fileshare:passthroughGroup"; //$NON-NLS-1$


    /**
     * 
     */
    public FilesharePassthroughGroupObjectTypeDescriptor () {
        super(FilesharePassthroughGroup.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }

}
