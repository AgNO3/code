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
public class PasswordPolicyConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<PasswordPolicyConfig, PasswordPolicyConfigImpl> {

    /**
     * 
     */
    public PasswordPolicyConfigObjectTypeDescriptor () {
        super(PasswordPolicyConfig.class, PasswordPolicyConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull PasswordPolicyConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull PasswordPolicyConfig getGlobalDefaults () {
        PasswordPolicyConfigMutable ppc = new PasswordPolicyConfigImpl();
        ppc.setEntropyLowerLimit(25);
        ppc.setEnableAgeCheck(false);
        ppc.setIgnoreUnknownAge(false);
        return ppc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull PasswordPolicyConfigMutable emptyInstance () {
        return new PasswordPolicyConfigImpl();
    }

}
