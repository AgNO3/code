/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfigMutable;
import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfigMutable;
import eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfigMutable;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareConfiguration, FileshareConfigurationImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:fileshare"; //$NON-NLS-1$


    /**
     * 
     */
    public FileshareConfigurationObjectTypeDescriptor () {
        super(FileshareConfiguration.class, FileshareConfigurationImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareConfiguration getGlobalDefaults () {
        return new FileshareConfigurationImpl();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#hasOverrideDefaultsFor(java.lang.Class)
     */
    @Override
    public boolean hasOverrideDefaultsFor ( @NonNull Class<? extends @Nullable ConfigurationObject> type ) {
        return LDAPAuthenticatorConfig.class.isAssignableFrom(type) || ADAuthenticatorConfig.class.isAssignableFrom(type)
                || KerberosAuthenticatorConfig.class.isAssignableFrom(type) || super.hasOverrideDefaultsFor(type);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getOverrideDefaults(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <TOverride extends ConfigurationObject> @Nullable TOverride getOverrideDefaults ( @NonNull Class<? extends @Nullable TOverride> type ) {

        Set<String> defaultRoles = new HashSet<>(Arrays.asList(
            "DEFAULT_USER", //$NON-NLS-1$
            "SYNCHRONIZED_USER")); //$NON-NLS-1$

        if ( LDAPAuthenticatorConfig.class.isAssignableFrom(type) ) {
            @NonNull
            LDAPAuthenticatorConfigMutable globalDefaults = LDAPAuthenticatorConfigObjectTypeDescriptor.globalDefaults();
            globalDefaults.setAlwaysAddRoles(defaultRoles);
            return (@Nullable TOverride) globalDefaults;
        }
        else if ( ADAuthenticatorConfig.class.isAssignableFrom(type) ) {
            ADAuthenticatorConfigMutable globalDefaults = ADAuthenticatorConfigObjectTypeDescriptor.globalDefaults();
            globalDefaults.setAlwaysAddRoles(defaultRoles);
            return (@Nullable TOverride) globalDefaults;
        }
        else if ( KerberosAuthenticatorConfig.class.isAssignableFrom(type) ) {
            KerberosAuthenticatorConfigMutable globalDefaults = KerberosAuthenticatorConfigObjectTypeDescriptor.globalDefaults();
            globalDefaults.setAlwaysAddRoles(defaultRoles);
            return (@Nullable TOverride) globalDefaults;
        }

        return super.getOverrideDefaults(type);
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareConfigurationMutable emptyInstance () {
        FileshareConfigurationMutable fc = new FileshareConfigurationImpl();
        fc.setContentConfiguration(FileshareContentConfigObjectTypeDescriptor.emptyInstance());
        fc.setNotificationConfiguration(FileshareNotificationConfigObjectTypeDescriptor.emptyInstance());
        fc.setSecurityPolicyConfiguration(FileshareSecurityPolicyConfigObjectTypeDescriptor.emptyInstance());
        fc.setUserConfiguration(FileshareUserConfigObjectTypeDescriptor.emptyInstance());
        fc.setWebConfiguration(FileshareWebConfigObjectTypeDescriptor.emptyInstance());
        fc.setAuthConfiguration(FileshareAuthConfigObjectTypeDescriptor.emptyInstance());
        fc.setStorageConfiguration(FileshareStorageConfigObjectTypeDescriptor.emptyInstance());
        fc.setAdvancedConfiguration(FileshareAdvancedConfigObjectTypeDescriptor.emptyInstance());
        fc.setLoggerConfiguration(FileshareLoggerConfigObjectTypeDescriptor.emptyInstance());
        return fc;
    }

}
