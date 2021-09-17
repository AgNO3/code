/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.krb5;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.auth.AbstractAuthenticatorConfigImpl;
import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntryImpl;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KerberosAuthenticatorConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_krb5" )
@Audited
@DiscriminatorValue ( "auth_krb5" )
public class KerberosAuthenticatorConfigImpl extends AbstractAuthenticatorConfigImpl<KerberosAuthenticatorConfig> implements
        KerberosAuthenticatorConfig, KerberosAuthenticatorConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private String kerberosRealm;

    private String keytabAlias;

    private String serviceName;

    private Boolean allowPasswordFallback;
    private Set<String> acceptPrincipalPatterns = new HashSet<>();
    private Set<String> rejectPrincipalPatterns = new HashSet<>();
    private Set<String> alwaysAddRoles = new HashSet<>();
    private Set<PatternRoleMapEntry> principalAddRoles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<KerberosAuthenticatorConfig> getType () {
        return KerberosAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfig#getKerberosRealm()
     */
    @Override
    public String getKerberosRealm () {
        return this.kerberosRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfigMutable#setKerberosRealm(java.lang.String)
     */
    @Override
    public void setKerberosRealm ( String krbRealm ) {
        this.kerberosRealm = krbRealm;
    }


    /**
     * @return the keytabAlias
     */
    @Override
    public String getKeytabAlias () {
        return this.keytabAlias;
    }


    /**
     * @param keytabAlias
     *            the keytabAlias to set
     */
    @Override
    public void setKeytabAlias ( String keytabAlias ) {
        this.keytabAlias = keytabAlias;
    }


    /**
     * @return the serviceName
     */
    @Override
    public String getServiceName () {
        return this.serviceName;
    }


    /**
     * @param serviceName
     *            the serviceName to set
     */
    @Override
    public void setServiceName ( String serviceName ) {
        this.serviceName = serviceName;
    }


    /**
     * @return the allowPasswordFallback
     */
    @Override
    public Boolean getAllowPasswordFallback () {
        return this.allowPasswordFallback;
    }


    /**
     * @param allowPasswordFallback
     *            the allowPasswordFallback to set
     */
    @Override
    public void setAllowPasswordFallback ( Boolean allowPasswordFallback ) {
        this.allowPasswordFallback = allowPasswordFallback;
    }


    /**
     * @return the acceptPrincipalPatterns
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_krb5_accept" )
    public Set<String> getAcceptPrincipalPatterns () {
        return this.acceptPrincipalPatterns;
    }


    /**
     * @param acceptPrincipalPatterns
     *            the acceptPrincipalPatterns to set
     */
    @Override
    public void setAcceptPrincipalPatterns ( Set<String> acceptPrincipalPatterns ) {
        this.acceptPrincipalPatterns = acceptPrincipalPatterns;
    }


    /**
     * @return the rejectPrincipalPatterns
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_krb5_reject" )
    public Set<String> getRejectPrincipalPatterns () {
        return this.rejectPrincipalPatterns;
    }


    /**
     * @param rejectPrincipalPatterns
     *            the rejectPrincipalPatterns to set
     */
    @Override
    public void setRejectPrincipalPatterns ( Set<String> rejectPrincipalPatterns ) {
        this.rejectPrincipalPatterns = rejectPrincipalPatterns;
    }


    /**
     * @return the alwaysAddRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_krb5_roles" )
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
     * @return the principalAddRoles
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = PatternRoleMapEntryImpl.class )
    public Set<PatternRoleMapEntry> getPrincipalAddRoles () {
        return this.principalAddRoles;
    }


    /**
     * @param principalAddRoles
     *            the principalAddRoles to set
     */
    @Override
    public void setPrincipalAddRoles ( Set<PatternRoleMapEntry> principalAddRoles ) {
        this.principalAddRoles = principalAddRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.AbstractAuthenticatorConfigImpl#doClone(eu.agno3.orchestrator.config.auth.AuthenticatorConfig)
     */
    @Override
    public void doClone ( AuthenticatorConfig obj ) {

        if ( ! ( obj instanceof KerberosAuthenticatorConfig ) ) {
            throw new IllegalArgumentException();
        }

        KerberosAuthenticatorConfig kc = (KerberosAuthenticatorConfig) obj;
        super.doClone(obj);
        this.kerberosRealm = kc.getKerberosRealm();
        this.keytabAlias = kc.getKeytabAlias();
        this.serviceName = kc.getServiceName();
        this.allowPasswordFallback = kc.getAllowPasswordFallback();
        this.acceptPrincipalPatterns = new HashSet<>(kc.getAcceptPrincipalPatterns());
        this.rejectPrincipalPatterns = new HashSet<>(kc.getRejectPrincipalPatterns());
        this.alwaysAddRoles = new HashSet<>(kc.getAlwaysAddRoles());
        this.principalAddRoles = PatternRoleMapEntryImpl.clone(kc.getPrincipalAddRoles());

    }
}
