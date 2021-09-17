/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.auth.AbstractAuthenticatorConfigImpl;
import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntryImpl;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntryImpl;
import eu.agno3.orchestrator.config.web.LDAPConfigurationImpl;
import eu.agno3.orchestrator.config.web.LDAPConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPAuthenticatorConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_ldap" )
@Audited
@DiscriminatorValue ( "auth_ldap" )
public class LDAPAuthenticatorConfigImpl extends AbstractAuthenticatorConfigImpl<LDAPAuthenticatorConfig> implements LDAPAuthenticatorConfig,
        LDAPAuthenticatorConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private LDAPConfigurationImpl connectionConfig;
    private LDAPAuthSchemaConfigImpl schemaConfig;

    private Boolean disableAuthentication;

    private Boolean enforcePasswordPolicy;
    private Boolean enforcePasswordPolicyOnChange;

    private Boolean addGroupNameAsRole;
    private Set<String> alwaysAddRoles = new HashSet<>();
    private Set<StaticRoleMapEntry> staticRoleMappings = new HashSet<>();
    private Set<PatternRoleMapEntry> patternRoleMappings = new HashSet<>();

    private Boolean enableSynchronization;
    private LDAPSyncOptionsImpl syncOptions;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LDAPAuthenticatorConfig> getType () {
        return LDAPAuthenticatorConfig.class;
    }


    /**
     * @return the connectionConfig
     */
    @Override
    @ManyToOne ( fetch = FetchType.LAZY, targetEntity = LDAPConfigurationImpl.class )
    public LDAPConfigurationMutable getConnectionConfig () {
        return this.connectionConfig;
    }


    /**
     * @param connectionConfig
     *            the connectionConfig to set
     */
    @Override
    public void setConnectionConfig ( LDAPConfigurationMutable connectionConfig ) {
        this.connectionConfig = (LDAPConfigurationImpl) connectionConfig;
    }


    /**
     * @return the schemaConfig
     */
    @Override
    @ManyToOne ( fetch = FetchType.LAZY, targetEntity = LDAPAuthSchemaConfigImpl.class )
    public LDAPAuthSchemaConfigMutable getSchemaConfig () {
        return this.schemaConfig;
    }


    /**
     * @param schemaConfig
     *            the schemaConfig to set
     */
    @Override
    public void setSchemaConfig ( LDAPAuthSchemaConfigMutable schemaConfig ) {
        this.schemaConfig = (LDAPAuthSchemaConfigImpl) schemaConfig;
    }


    /**
     * @return the disableAuthentication
     */
    @Override
    public Boolean getDisableAuthentication () {
        return this.disableAuthentication;
    }


    /**
     * @param disableAuthentication
     *            the disableAuthentication to set
     */
    @Override
    public void setDisableAuthentication ( Boolean disableAuthentication ) {
        this.disableAuthentication = disableAuthentication;
    }


    /**
     * @return the enforcePasswordPolicy
     */
    @Override
    public Boolean getEnforcePasswordPolicy () {
        return this.enforcePasswordPolicy;
    }


    /**
     * @param enforcePasswordPolicy
     *            the enforcePasswordPolicy to set
     */
    @Override
    public void setEnforcePasswordPolicy ( Boolean enforcePasswordPolicy ) {
        this.enforcePasswordPolicy = enforcePasswordPolicy;
    }


    /**
     * @return the enforcePasswordPolicyOnChange
     */
    @Override
    public Boolean getEnforcePasswordPolicyOnChange () {
        return this.enforcePasswordPolicyOnChange;
    }


    /**
     * @param enforcePasswordPolicyOnChange
     *            the enforcePasswordPolicyOnChange to set
     */
    @Override
    public void setEnforcePasswordPolicyOnChange ( Boolean enforcePasswordPolicyOnChange ) {
        this.enforcePasswordPolicyOnChange = enforcePasswordPolicyOnChange;
    }


    /**
     * @return the addGroupNameAsRole
     */
    @Override
    public Boolean getAddGroupNameAsRole () {
        return this.addGroupNameAsRole;
    }


    /**
     * @param addGroupNameAsRole
     *            the addGroupNameAsRole to set
     */
    @Override
    public void setAddGroupNameAsRole ( Boolean addGroupNameAsRole ) {
        this.addGroupNameAsRole = addGroupNameAsRole;
    }


    /**
     * @return the alwaysAddRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "auth_ldap_roles_always" )
    public Set<String> getAlwaysAddRoles () {
        return this.alwaysAddRoles;
    }


    /**
     * @param alwaysAddRoles
     *            the alwaysAddRoles to set
     */
    @Override
    public void setAlwaysAddRoles ( Set<String> alwaysAddRoles ) {
        this.alwaysAddRoles = alwaysAddRoles;
    }


    /**
     * @return the staticRoleMappings
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = StaticRoleMapEntryImpl.class )
    public Set<StaticRoleMapEntry> getStaticRoleMappings () {
        return this.staticRoleMappings;
    }


    /**
     * @param staticRoleMappings
     *            the staticRoleMappings to set
     */
    @Override
    public void setStaticRoleMappings ( Set<StaticRoleMapEntry> staticRoleMappings ) {
        this.staticRoleMappings = staticRoleMappings;
    }


    /**
     * @return the patternRoleMappings
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = PatternRoleMapEntryImpl.class )
    public Set<PatternRoleMapEntry> getPatternRoleMappings () {
        return this.patternRoleMappings;
    }


    /**
     * @param patternRoleMappings
     *            the patternRoleMappings to set
     */
    @Override
    public void setPatternRoleMappings ( Set<PatternRoleMapEntry> patternRoleMappings ) {
        this.patternRoleMappings = patternRoleMappings;
    }


    /**
     * @return the enableSynchronization
     */
    @Override
    public Boolean getEnableSynchronization () {
        return this.enableSynchronization;
    }


    /**
     * @param enableSynchronization
     *            the enableSynchronization to set
     */
    @Override
    public void setEnableSynchronization ( Boolean enableSynchronization ) {
        this.enableSynchronization = enableSynchronization;
    }


    /**
     * @return the syncOptions
     */
    @Override
    @ManyToOne ( fetch = FetchType.LAZY, targetEntity = LDAPSyncOptionsImpl.class )
    public LDAPSyncOptionsMutable getSyncOptions () {
        return this.syncOptions;
    }


    /**
     * @param syncOptions
     *            the syncOptions to set
     */
    @Override
    public void setSyncOptions ( LDAPSyncOptionsMutable syncOptions ) {
        this.syncOptions = (LDAPSyncOptionsImpl) syncOptions;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.AbstractAuthenticatorConfigImpl#doClone(eu.agno3.orchestrator.config.auth.AuthenticatorConfig)
     */
    @Override
    public void doClone ( AuthenticatorConfig obj ) {

        if ( ! ( obj instanceof LDAPAuthenticatorConfig ) ) {
            throw new IllegalArgumentException();
        }

        LDAPAuthenticatorConfig kc = (LDAPAuthenticatorConfig) obj;
        super.doClone(kc);

        this.connectionConfig = LDAPConfigurationImpl.clone(kc.getConnectionConfig());
        this.schemaConfig = LDAPAuthSchemaConfigImpl.clone(kc.getSchemaConfig());

        this.disableAuthentication = kc.getDisableAuthentication();
        this.enforcePasswordPolicy = kc.getEnforcePasswordPolicy();
        this.enforcePasswordPolicyOnChange = kc.getEnforcePasswordPolicyOnChange();
        this.addGroupNameAsRole = kc.getAddGroupNameAsRole();
        this.alwaysAddRoles = new HashSet<>(kc.getAlwaysAddRoles());
        this.staticRoleMappings = StaticRoleMapEntryImpl.clone(kc.getStaticRoleMappings());
        this.patternRoleMappings = PatternRoleMapEntryImpl.clone(kc.getPatternRoleMappings());

        this.enableSynchronization = kc.getEnableSynchronization();
        this.syncOptions = LDAPSyncOptionsImpl.clone(kc.getSyncOptions());
    }
}
