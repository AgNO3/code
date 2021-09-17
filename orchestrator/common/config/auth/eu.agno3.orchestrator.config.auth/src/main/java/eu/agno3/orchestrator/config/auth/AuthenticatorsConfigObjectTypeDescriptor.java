/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class AuthenticatorsConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<AuthenticatorsConfig, AuthenticatorsConfigImpl> {

    /**
     * 
     */
    public static final String OBJECT_TYPE = "urn:agno3:objects:1.0:auth:collection"; //$NON-NLS-1$


    /**
     * 
     */
    public AuthenticatorsConfigObjectTypeDescriptor () {
        super(AuthenticatorsConfig.class, AuthenticatorsConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull AuthenticatorsConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull AuthenticatorsConfig getGlobalDefaults () {
        AuthenticatorsConfigImpl ac = new AuthenticatorsConfigImpl();
        ac.setEnableLocalAuth(true);
        ac.setAllowInsecureAuth(false);
        return ac;
    }


    /**
     * @return empty instance
     */
    public static @NonNull AuthenticatorsConfigMutable emptyInstance () {
        AuthenticatorsConfigImpl ac = new AuthenticatorsConfigImpl();
        ac.setLoginRateLimit(LoginRateLimitObjectTypeDescriptor.emptyInstance());
        ac.setPasswordPolicy(PasswordPolicyConfigObjectTypeDescriptor.emptyInstance());
        return ac;
    }

}
