/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectTypeDescriptor.class )
public class RealmsConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<RealmsConfig, RealmsConfigImpl> {

    /**
     * 
     */
    public RealmsConfigObjectTypeDescriptor () {
        super(RealmsConfig.class, RealmsConfigImpl.class, RealmsConfigMessages.BASE, "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * @return a new empty instance
     */
    public static RealmsConfigMutable emptyInstance () {
        return new RealmsConfigImpl();
    }
}
