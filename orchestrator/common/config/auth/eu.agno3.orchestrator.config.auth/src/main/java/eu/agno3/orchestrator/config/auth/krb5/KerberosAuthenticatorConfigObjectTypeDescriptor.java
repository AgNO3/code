/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth.krb5;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class KerberosAuthenticatorConfigObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<KerberosAuthenticatorConfig, KerberosAuthenticatorConfigImpl> {

    /**
     * 
     */
    public KerberosAuthenticatorConfigObjectTypeDescriptor () {
        super(KerberosAuthenticatorConfig.class, KerberosAuthenticatorConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull KerberosAuthenticatorConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull KerberosAuthenticatorConfig getGlobalDefaults () {
        return globalDefaults();
    }


    /**
     * @return global defaults
     */
    public static @NonNull KerberosAuthenticatorConfigMutable globalDefaults () {
        KerberosAuthenticatorConfigImpl kc = new KerberosAuthenticatorConfigImpl();
        kc.setAllowPasswordFallback(true);
        kc.setServiceName("HTTP"); //$NON-NLS-1$
        return kc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull KerberosAuthenticatorConfigMutable emptyInstance () {
        return new KerberosAuthenticatorConfigImpl();
    }

}
