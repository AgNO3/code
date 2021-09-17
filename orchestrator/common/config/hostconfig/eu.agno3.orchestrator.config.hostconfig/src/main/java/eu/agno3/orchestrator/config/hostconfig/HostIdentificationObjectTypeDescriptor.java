/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class HostIdentificationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<HostIdentification, HostIdentificationImpl> {

    /**
     * 
     */
    public HostIdentificationObjectTypeDescriptor () {
        super(HostIdentification.class, HostIdentificationImpl.class, HostConfigurationMessages.BASE_PACKAGE, "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * @return empty instance
     */
    public static HostIdentificationMutable emptyInstance () {
        return new HostIdentificationImpl();
    }
}
