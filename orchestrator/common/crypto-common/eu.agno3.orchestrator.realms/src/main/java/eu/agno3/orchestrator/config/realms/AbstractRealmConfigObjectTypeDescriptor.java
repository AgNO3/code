/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractBaseObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class AbstractRealmConfigObjectTypeDescriptor extends AbstractBaseObjectTypeDescriptor<RealmConfig> {

    /**
     * 
     */
    public static final String OBJECT_TYPE = "urn:agno3:objects:1.0:realms:realm"; //$NON-NLS-1$


    /**
     * 
     */
    public AbstractRealmConfigObjectTypeDescriptor () {
        super(RealmConfig.class, RealmsConfigMessages.BASE);
    }

}
