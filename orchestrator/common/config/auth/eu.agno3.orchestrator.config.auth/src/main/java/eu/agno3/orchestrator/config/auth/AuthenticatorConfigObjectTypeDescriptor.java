/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractBaseObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class AuthenticatorConfigObjectTypeDescriptor extends AbstractBaseObjectTypeDescriptor<AuthenticatorConfig> {

    /**
     * 
     */
    public static final String OBJECT_TYPE = "urn:agno3:objects:1.0:auth:authenticator"; //$NON-NLS-1$


    /**
     * 
     */
    public AuthenticatorConfigObjectTypeDescriptor () {
        super(AuthenticatorConfig.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }

}
