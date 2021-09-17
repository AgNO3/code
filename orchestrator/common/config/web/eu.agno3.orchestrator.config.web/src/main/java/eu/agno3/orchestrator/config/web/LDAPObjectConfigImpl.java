/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPObjectConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_web_ldap_obj" )
@Audited
@DiscriminatorValue ( "webc_ldapobj" )
public class LDAPObjectConfigImpl extends AbstractConfigurationObject<LDAPObjectConfig> implements LDAPObjectConfig, LDAPObjectConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private String baseDN;
    private LDAPSearchScope scope;
    private String customFilter;
    private String attributeStyle;
    private Set<LDAPObjectAttributeMapping> customAttributeMappings = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LDAPObjectConfig> getType () {
        return LDAPObjectConfig.class;
    }


    /**
     * @return the baseDN
     */
    @Override
    public String getBaseDN () {
        return this.baseDN;
    }


    /**
     * @param baseDN
     *            the baseDN to set
     */
    @Override
    public void setBaseDN ( String baseDN ) {
        this.baseDN = baseDN;
    }


    /**
     * @return the scope
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public LDAPSearchScope getScope () {
        return this.scope;
    }


    /**
     * @param scope
     *            the scope to set
     */
    @Override
    public void setScope ( LDAPSearchScope scope ) {
        this.scope = scope;
    }


    /**
     * @return the customFilter
     */
    @Override
    public String getCustomFilter () {
        return this.customFilter;
    }


    /**
     * @param customFilter
     *            the customFilter to set
     */
    @Override
    public void setCustomFilter ( String customFilter ) {
        this.customFilter = customFilter;
    }


    /**
     * @return the attributeStyle
     */
    @Override
    public String getAttributeStyle () {
        return this.attributeStyle;
    }


    /**
     * @param attributeStyle
     *            the attributeStyle to set
     */
    @Override
    public void setAttributeStyle ( String attributeStyle ) {
        this.attributeStyle = attributeStyle;
    }


    /**
     * @return the customAttributeMappings
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = LDAPObjectAttributeMappingImpl.class )
    public Set<LDAPObjectAttributeMapping> getCustomAttributeMappings () {
        return this.customAttributeMappings;
    }


    /**
     * @param customAttributeMappings
     *            the customAttributeMappings to set
     */
    @Override
    public void setCustomAttributeMappings ( Set<LDAPObjectAttributeMapping> customAttributeMappings ) {
        this.customAttributeMappings = customAttributeMappings;
    }


    /**
     * @param e
     * @return cloned object
     */
    public static LDAPObjectConfigImpl clone ( LDAPObjectConfig e ) {
        LDAPObjectConfigImpl cloned = new LDAPObjectConfigImpl();
        cloned.baseDN = e.getBaseDN();
        cloned.scope = e.getScope();
        cloned.customFilter = e.getCustomFilter();
        cloned.attributeStyle = e.getAttributeStyle();
        cloned.customAttributeMappings = LDAPObjectAttributeMappingImpl.clone(e.getCustomAttributeMappings());
        return cloned;
    }
}
