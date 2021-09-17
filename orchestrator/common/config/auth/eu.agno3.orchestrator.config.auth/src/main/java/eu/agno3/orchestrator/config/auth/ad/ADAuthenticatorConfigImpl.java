/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ad;


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
import eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptionsImpl;
import eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptionsMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( ADAuthenticatorConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_ad" )
@Audited
@DiscriminatorValue ( "auth_ad" )
public class ADAuthenticatorConfigImpl extends AbstractAuthenticatorConfigImpl<ADAuthenticatorConfig> implements ADAuthenticatorConfig,
        ADAuthenticatorConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private String domain;

    private String keytab;

    private String serviceName;

    private Boolean sendNTLMChallenge;
    private Boolean acceptNTLMFallback;
    private Boolean disablePACValidation;
    private Boolean disablePACs;

    private Boolean rejectNonADPrincipals;
    private Boolean acceptOnlyLocal;
    private Boolean requireDomainUserGroup;

    private Set<String> acceptDomainSids = new HashSet<>();
    private Set<String> rejectDomainSids = new HashSet<>();

    private Set<String> requiredSids = new HashSet<>();
    private Set<String> rejectSids = new HashSet<>();

    private Set<StaticRoleMapEntry> sidRoles = new HashSet<>();

    private Boolean allowPasswordFallback;
    private Set<String> acceptPrincipalPatterns = new HashSet<>();
    private Set<String> rejectPrincipalPatterns = new HashSet<>();
    private Set<String> alwaysAddRoles = new HashSet<>();
    private Set<PatternRoleMapEntry> principalAddRoles = new HashSet<>();

    private Boolean enableSynchronization;

    private String userSyncBase;
    private String userSyncFilter;

    private String groupSyncBase;
    private String groupSyncFilter;

    private LDAPSyncOptionsImpl syncOptions;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<ADAuthenticatorConfig> getType () {
        return ADAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig#getDomain()
     */
    @Override
    public String getDomain () {
        return this.domain;
    }


    /**
     * @param domain
     *            the domain to set
     */
    @Override
    public void setDomain ( String domain ) {
        this.domain = domain;
    }


    /**
     * @return the keytab
     */
    @Override
    public String getKeytab () {
        return this.keytab;
    }


    /**
     * @param keytab
     *            the keytab to set
     */
    @Override
    public void setKeytab ( String keytab ) {
        this.keytab = keytab;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig#getServiceName()
     */
    @Override
    public String getServiceName () {
        return this.serviceName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfigMutable#setServiceName(java.lang.String)
     */
    @Override
    public void setServiceName ( String serviceName ) {
        this.serviceName = serviceName;
    }


    /**
     * @return the acceptNTLMFallback
     */
    @Override
    public Boolean getAcceptNTLMFallback () {
        return this.acceptNTLMFallback;
    }


    /**
     * @param acceptNTLMFallback
     *            the acceptNTLMFallback to set
     */
    @Override
    public void setAcceptNTLMFallback ( Boolean acceptNTLMFallback ) {
        this.acceptNTLMFallback = acceptNTLMFallback;
    }


    /**
     * @return the sendNTLMChallenge
     */
    @Override
    public Boolean getSendNTLMChallenge () {
        return this.sendNTLMChallenge;
    }


    /**
     * @param sendNTLMChallenge
     *            the sendNTLMChallenge to set
     */
    @Override
    public void setSendNTLMChallenge ( Boolean sendNTLMChallenge ) {
        this.sendNTLMChallenge = sendNTLMChallenge;
    }


    /**
     * @return the disablePACValidation
     */
    @Override
    public Boolean getDisablePACValidation () {
        return this.disablePACValidation;
    }


    /**
     * @param disablePACValidation
     *            the disablePACValidation to set
     */
    @Override
    public void setDisablePACValidation ( Boolean disablePACValidation ) {
        this.disablePACValidation = disablePACValidation;
    }


    /**
     * @return the disablePACs
     */
    @Override
    public Boolean getDisablePACs () {
        return this.disablePACs;
    }


    /**
     * @param disablePACs
     *            the disablePACs to set
     */
    @Override
    public void setDisablePACs ( Boolean disablePACs ) {
        this.disablePACs = disablePACs;
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
     * @return the rejectNonADPrincipals
     */
    @Override
    public Boolean getRejectNonADPrincipals () {
        return this.rejectNonADPrincipals;
    }


    /**
     * @param rejectNonADPrincipals
     *            the rejectNonADPrincipals to set
     */
    @Override
    public void setRejectNonADPrincipals ( Boolean rejectNonADPrincipals ) {
        this.rejectNonADPrincipals = rejectNonADPrincipals;
    }


    /**
     * @return the acceptOnlyLocal
     */
    @Override
    public Boolean getAcceptOnlyLocal () {
        return this.acceptOnlyLocal;
    }


    /**
     * @param acceptOnlyLocal
     *            the acceptOnlyLocal to set
     */
    @Override
    public void setAcceptOnlyLocal ( Boolean acceptOnlyLocal ) {
        this.acceptOnlyLocal = acceptOnlyLocal;
    }


    /**
     * @return the requireDomainUserGroup
     */
    @Override
    public Boolean getRequireDomainUserGroup () {
        return this.requireDomainUserGroup;
    }


    /**
     * @param requireDomainUserGroup
     *            the requireDomainUserGroup to set
     */
    @Override
    public void setRequireDomainUserGroup ( Boolean requireDomainUserGroup ) {
        this.requireDomainUserGroup = requireDomainUserGroup;
    }


    /**
     * @return the acceptDomainSids
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_ad_accdomsid" )
    public Set<String> getAcceptDomainSids () {
        return this.acceptDomainSids;
    }


    /**
     * @param acceptDomainSids
     *            the acceptDomainSids to set
     */
    @Override
    public void setAcceptDomainSids ( Set<String> acceptDomainSids ) {
        this.acceptDomainSids = acceptDomainSids;
    }


    /**
     * @return the rejectDomainSids
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_ad_rejdomsid" )
    public Set<String> getRejectDomainSids () {
        return this.rejectDomainSids;
    }


    /**
     * @param rejectDomainSids
     *            the rejectDomainSids to set
     */
    @Override
    public void setRejectDomainSids ( Set<String> rejectDomainSids ) {
        this.rejectDomainSids = rejectDomainSids;
    }


    /**
     * @return the requiredSids
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_ad_reqsid" )
    public Set<String> getRequiredSids () {
        return this.requiredSids;
    }


    /**
     * @param requiredSids
     *            the requiredSids to set
     */
    @Override
    public void setRequiredSids ( Set<String> requiredSids ) {
        this.requiredSids = requiredSids;
    }


    /**
     * @return the rejectSids
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_auth_ad_rejsid" )
    public Set<String> getRejectSids () {
        return this.rejectSids;
    }


    /**
     * @param rejectSids
     *            the rejectSids to set
     */
    @Override
    public void setRejectSids ( Set<String> rejectSids ) {
        this.rejectSids = rejectSids;
    }


    /**
     * @return the sidRoles
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = StaticRoleMapEntryImpl.class )
    public Set<StaticRoleMapEntry> getSidRoles () {
        return this.sidRoles;
    }


    /**
     * @param sidRoles
     *            the sidRoles to set
     */
    public void setSidRoles ( Set<StaticRoleMapEntry> sidRoles ) {
        this.sidRoles = sidRoles;
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
    @CollectionTable ( name = "config_auth_ad_acceptprinc" )
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
    @CollectionTable ( name = "config_auth_ad_rejectprinc" )
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
    @CollectionTable ( name = "config_auth_ad_roles" )
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
     * @return the userSyncBase
     */
    @Override
    public String getUserSyncBase () {
        return this.userSyncBase;
    }


    /**
     * @param userSyncBase
     *            the userSyncBase to set
     */
    @Override
    public void setUserSyncBase ( String userSyncBase ) {
        this.userSyncBase = userSyncBase;
    }


    /**
     * @return the userSyncFilter
     */
    @Override
    public String getUserSyncFilter () {
        return this.userSyncFilter;
    }


    /**
     * @param userSyncFilter
     *            the userSyncFilter to set
     */
    @Override
    public void setUserSyncFilter ( String userSyncFilter ) {
        this.userSyncFilter = userSyncFilter;
    }


    /**
     * @return the groupSyncBase
     */
    @Override
    public String getGroupSyncBase () {
        return this.groupSyncBase;
    }


    /**
     * @param groupSyncBase
     *            the groupSyncBase to set
     */
    @Override
    public void setGroupSyncBase ( String groupSyncBase ) {
        this.groupSyncBase = groupSyncBase;
    }


    /**
     * @return the groupSyncFilter
     */
    @Override
    public String getGroupSyncFilter () {
        return this.groupSyncFilter;
    }


    /**
     * @param groupSyncFilter
     *            the groupSyncFilter to set
     */
    @Override
    public void setGroupSyncFilter ( String groupSyncFilter ) {
        this.groupSyncFilter = groupSyncFilter;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.AbstractAuthenticatorConfigImpl#doClone(eu.agno3.orchestrator.config.auth.AuthenticatorConfig)
     */
    @Override
    public void doClone ( AuthenticatorConfig obj ) {

        if ( ! ( obj instanceof ADAuthenticatorConfig ) ) {
            throw new IllegalArgumentException();
        }

        ADAuthenticatorConfig kc = (ADAuthenticatorConfig) obj;
        super.doClone(obj);
        this.domain = kc.getDomain();
        this.serviceName = kc.getServiceName();
        this.sendNTLMChallenge = kc.getSendNTLMChallenge();
        this.acceptNTLMFallback = kc.getAcceptNTLMFallback();
        this.disablePACValidation = kc.getDisablePACValidation();
        this.disablePACs = kc.getDisablePACs();
        this.rejectNonADPrincipals = kc.getRejectNonADPrincipals();
        this.acceptOnlyLocal = kc.getAcceptOnlyLocal();
        this.requireDomainUserGroup = kc.getRequireDomainUserGroup();

        this.acceptDomainSids = new HashSet<>(kc.getAcceptDomainSids());
        this.rejectDomainSids = new HashSet<>(kc.getRejectDomainSids());
        this.requiredSids = new HashSet<>(kc.getRequiredSids());
        this.rejectSids = new HashSet<>(kc.getRejectSids());

        this.allowPasswordFallback = kc.getAllowPasswordFallback();
        this.acceptPrincipalPatterns = new HashSet<>(kc.getAcceptPrincipalPatterns());
        this.rejectPrincipalPatterns = new HashSet<>(kc.getRejectPrincipalPatterns());
        this.alwaysAddRoles = new HashSet<>(kc.getAlwaysAddRoles());

        this.sidRoles = StaticRoleMapEntryImpl.clone(kc.getSidRoles());
        this.principalAddRoles = PatternRoleMapEntryImpl.clone(kc.getPrincipalAddRoles());

        this.enableSynchronization = kc.getEnableSynchronization();
        this.syncOptions = LDAPSyncOptionsImpl.clone(kc.getSyncOptions());

        this.userSyncBase = kc.getUserSyncBase();
        this.userSyncFilter = kc.getUserSyncFilter();

        this.groupSyncBase = kc.getGroupSyncBase();
        this.groupSyncFilter = kc.getGroupSyncFilter();
    }
}
