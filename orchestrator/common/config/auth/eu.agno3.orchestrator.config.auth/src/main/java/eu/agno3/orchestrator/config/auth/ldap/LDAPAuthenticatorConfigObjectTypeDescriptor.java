/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.LDAPConfigurationMutable;
import eu.agno3.orchestrator.config.web.LDAPConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.SSLClientMode;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class LDAPAuthenticatorConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LDAPAuthenticatorConfig, LDAPAuthenticatorConfigImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:auth:authenticator:ldap"; //$NON-NLS-1$


    /**
     * 
     */
    public LDAPAuthenticatorConfigObjectTypeDescriptor () {
        super(LDAPAuthenticatorConfig.class, LDAPAuthenticatorConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull LDAPAuthenticatorConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull LDAPAuthenticatorConfig getGlobalDefaults () {
        return globalDefaults();
    }


    /**
     * @return the global defaults
     */
    public static @NonNull LDAPAuthenticatorConfigMutable globalDefaults () {
        LDAPAuthenticatorConfigMutable lac = new LDAPAuthenticatorConfigImpl();
        lac.setEnableSynchronization(false);
        lac.setDisableAuthentication(false);
        lac.setEnforcePasswordPolicy(true);
        lac.setEnforcePasswordPolicyOnChange(true);
        lac.setAddGroupNameAsRole(false);

        LDAPConfigurationMutable connConfig = LDAPConfigurationObjectTypeDescriptor.globalDefaults();
        connConfig.setSslClientMode(SSLClientMode.REQUIRE_STARTTLS);
        lac.setConnectionConfig(connConfig);
        return lac;
    }


    /**
     * @return empty instance
     */
    public static @NonNull LDAPAuthenticatorConfigMutable emptyInstance () {
        LDAPAuthenticatorConfigMutable lac = new LDAPAuthenticatorConfigImpl();
        lac.setConnectionConfig(LDAPConfigurationObjectTypeDescriptor.emptyInstance());
        lac.setSchemaConfig(LDAPAuthSchemaConfigObjectTypeDescriptor.emptyInstance());
        lac.setSyncOptions(LDAPSyncOptionsObjectTypeDescriptor.emptyInstance());
        return lac;
    }

}
