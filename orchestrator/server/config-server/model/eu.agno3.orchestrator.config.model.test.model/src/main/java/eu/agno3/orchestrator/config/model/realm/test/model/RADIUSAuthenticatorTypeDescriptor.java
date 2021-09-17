/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class RADIUSAuthenticatorTypeDescriptor extends AbstractObjectTypeDescriptor<RADIUSAuthenticator, RADIUSAuthenticatorImpl> {

    /**
     * 
     */
    public RADIUSAuthenticatorTypeDescriptor () {
        super(RADIUSAuthenticator.class, RADIUSAuthenticatorImpl.class, null);
    }

}
