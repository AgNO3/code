/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ad;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptionsObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class ADAuthenticatorConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<ADAuthenticatorConfig, ADAuthenticatorConfigImpl> {

    /**
     * 
     */
    public ADAuthenticatorConfigObjectTypeDescriptor () {
        super(ADAuthenticatorConfig.class, ADAuthenticatorConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull ADAuthenticatorConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull ADAuthenticatorConfig getGlobalDefaults () {
        return globalDefaults();
    }


    /**
     * @return global defaults
     */
    public static @NonNull ADAuthenticatorConfigMutable globalDefaults () {
        ADAuthenticatorConfigMutable ac = new ADAuthenticatorConfigImpl();
        ac.setAcceptNTLMFallback(true);
        ac.setSendNTLMChallenge(false);
        ac.setAllowPasswordFallback(true);
        ac.setDisablePACs(false);
        ac.setDisablePACValidation(false);
        ac.setAcceptOnlyLocal(true);
        ac.setRequireDomainUserGroup(true);
        ac.setRejectNonADPrincipals(true);
        ac.setServiceName("HTTP"); //$NON-NLS-1$
        ac.setEnableSynchronization(true);
        ac.setUserSyncFilter("(&(!(isCriticalSystemObject=TRUE))(samAccountType=805306368))"); //$NON-NLS-1$
        ac.setGroupSyncFilter("(&(!(isCriticalSystemObject=TRUE))(!(cn=DnsUpdateProxy))(samAccountType=268435456))"); //$NON-NLS-1$
        return ac;
    }


    /**
     * @return empty instance
     */
    public static @NonNull ADAuthenticatorConfig emptyInstance () {
        ADAuthenticatorConfigMutable ac = new ADAuthenticatorConfigImpl();
        ac.setSyncOptions(LDAPSyncOptionsObjectTypeDescriptor.emptyInstance());
        return ac;
    }

}
