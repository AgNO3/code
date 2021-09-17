/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class LoginRateLimitObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LoginRateLimitConfig, LoginRateLimitConfigImpl> {

    /**
     * 
     */
    public LoginRateLimitObjectTypeDescriptor () {
        super(LoginRateLimitConfig.class, LoginRateLimitConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return AuthenticatorsConfigObjectTypeDescriptor.OBJECT_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull LoginRateLimitConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull LoginRateLimitConfig getGlobalDefaults () {
        LoginRateLimitConfigMutable lrlc = new LoginRateLimitConfigImpl();
        lrlc.setCleanInterval(Duration.standardMinutes(10));
        lrlc.setDisableGlobalDelay(false);
        lrlc.setDisableLaxSourceCheck(false);
        lrlc.setDisableUserLockout(false);
        return lrlc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull LoginRateLimitConfigMutable emptyInstance () {
        return new LoginRateLimitConfigImpl();
    }

}
