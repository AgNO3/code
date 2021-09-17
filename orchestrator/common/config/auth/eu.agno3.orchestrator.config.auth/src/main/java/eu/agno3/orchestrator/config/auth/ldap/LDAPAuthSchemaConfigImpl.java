/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.web.LDAPObjectAttributeMapping;
import eu.agno3.orchestrator.config.web.LDAPObjectAttributeMappingImpl;
import eu.agno3.orchestrator.config.web.LDAPObjectConfigImpl;
import eu.agno3.orchestrator.config.web.LDAPObjectConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPAuthSchemaConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_ldap_scheme" )
@Audited
@DiscriminatorValue ( "auth_ldapsch" )
public class LDAPAuthSchemaConfigImpl extends AbstractConfigurationObject<LDAPAuthSchemaConfig> implements LDAPAuthSchemaConfig,
        LDAPAuthSchemaConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private LDAPObjectConfigImpl userSchema;
    private LDAPObjectConfigImpl groupSchema;

    private Set<LDAPObjectAttributeMapping> operationalAttributeMappings = new HashSet<>();

    private Boolean recursiveResolveGroups;
    private Boolean referencesAreDNs;
    private Boolean useForwardGroups;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LDAPAuthSchemaConfig> getType () {
        return LDAPAuthSchemaConfig.class;
    }


    /**
     * @return the userSchema
     */
    @Override
    @ManyToOne ( fetch = FetchType.LAZY, targetEntity = LDAPObjectConfigImpl.class )
    public LDAPObjectConfigMutable getUserSchema () {
        return this.userSchema;
    }


    /**
     * @param userSchema
     *            the userSchema to set
     */
    @Override
    public void setUserSchema ( LDAPObjectConfigMutable userSchema ) {
        this.userSchema = (LDAPObjectConfigImpl) userSchema;
    }


    /**
     * @return the groupSchema
     */
    @Override
    @ManyToOne ( fetch = FetchType.LAZY, targetEntity = LDAPObjectConfigImpl.class )
    public LDAPObjectConfigMutable getGroupSchema () {
        return this.groupSchema;
    }


    /**
     * @param groupSchema
     *            the groupSchema to set
     */
    @Override
    public void setGroupSchema ( LDAPObjectConfigMutable groupSchema ) {
        this.groupSchema = (LDAPObjectConfigImpl) groupSchema;
    }


    /**
     * @return the customAttributeMappings
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = LDAPObjectAttributeMappingImpl.class )
    public Set<LDAPObjectAttributeMapping> getOperationalAttributeMappings () {
        return this.operationalAttributeMappings;
    }


    /**
     * @param customAttributeMappings
     *            the customAttributeMappings to set
     */
    @Override
    public void setOperationalAttributeMappings ( Set<LDAPObjectAttributeMapping> customAttributeMappings ) {
        this.operationalAttributeMappings = customAttributeMappings;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ldap.LDAPAuthSchemaConfig#getRecursiveResolveGroups()
     */
    @Override
    public Boolean getRecursiveResolveGroups () {
        return this.recursiveResolveGroups;
    }


    /**
     * @param recursiveResolveGroups
     *            the recursiveResolveGroups to set
     */
    @Override
    public void setRecursiveResolveGroups ( Boolean recursiveResolveGroups ) {
        this.recursiveResolveGroups = recursiveResolveGroups;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ldap.LDAPAuthSchemaConfig#getReferencesAreDNs()
     */
    @Override
    public Boolean getReferencesAreDNs () {
        return this.referencesAreDNs;
    }


    /**
     * @param referencesAreDNs
     *            the referencesAreDNs to set
     */
    @Override
    public void setReferencesAreDNs ( Boolean referencesAreDNs ) {
        this.referencesAreDNs = referencesAreDNs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ldap.LDAPAuthSchemaConfig#getUseForwardGroups()
     */
    @Override
    public Boolean getUseForwardGroups () {
        return this.useForwardGroups;
    }


    /**
     * @param useFowardGroups
     *            the useFowardGroups to set
     */
    @Override
    public void setUseForwardGroups ( Boolean useFowardGroups ) {
        this.useForwardGroups = useFowardGroups;
    }


    /**
     * @param schemaConfig
     * @return cloned object
     */
    public static LDAPAuthSchemaConfigImpl clone ( LDAPAuthSchemaConfig schemaConfig ) {
        LDAPAuthSchemaConfigImpl cloned = new LDAPAuthSchemaConfigImpl();
        cloned.userSchema = LDAPObjectConfigImpl.clone(schemaConfig.getUserSchema());
        cloned.groupSchema = LDAPObjectConfigImpl.clone(schemaConfig.getGroupSchema());
        cloned.operationalAttributeMappings = LDAPObjectAttributeMappingImpl.clone(schemaConfig.getOperationalAttributeMappings());

        cloned.recursiveResolveGroups = schemaConfig.getRecursiveResolveGroups();
        cloned.referencesAreDNs = schemaConfig.getReferencesAreDNs();
        cloned.useForwardGroups = schemaConfig.getUseForwardGroups();
        return cloned;
    }
}
