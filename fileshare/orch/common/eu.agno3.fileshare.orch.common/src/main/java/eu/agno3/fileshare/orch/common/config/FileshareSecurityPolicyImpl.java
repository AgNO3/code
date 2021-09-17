/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareSecurityPolicy.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_secpolicy" )
@Audited
@DiscriminatorValue ( "filesh_secpol" )
public class FileshareSecurityPolicyImpl extends AbstractConfigurationObject<FileshareSecurityPolicy> implements FileshareSecurityPolicy,
        FileshareSecurityPolicyMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5876645864215111149L;

    private String label;
    private Integer sortPriority;
    private Set<GrantType> allowedShareTypes = new HashSet<>();

    private Boolean disallowWebDAVAccess;

    private Boolean enableDefaultExpiration;
    private Duration defaultExpirationDuration;
    private Boolean restrictExpirationDuration;
    private Duration maximumExpirationDuration;

    private Boolean enableShareExpiration;
    private Duration defaultShareLifetime;
    private Boolean restrictShareLifetime;
    private Duration maximumShareLifetime;

    private Duration afterShareGracePeriod;

    private Boolean transportRequireEncryption;
    private Boolean transportRequirePFS;
    private Integer transportMinHashBlockSize;
    private Integer transportMinKeySize;

    private Set<String> requireAnyRole = new HashSet<>();
    private Set<String> disallowRoles = new HashSet<>();

    private Integer minTokenPasswordEntropy;
    private Boolean requireTokenPassword;
    private Boolean noUserTokenPasswords;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareSecurityPolicy> getType () {
        return FileshareSecurityPolicy.class;
    }


    /**
     * @return the label
     */
    @Override
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    @Override
    public void setLabel ( String label ) {
        this.label = label;
    }


    /**
     * @return the sortPriority
     */
    @Override
    public Integer getSortPriority () {
        return this.sortPriority;
    }


    /**
     * @param sortPriority
     *            the sortPriority to set
     */
    @Override
    public void setSortPriority ( Integer sortPriority ) {
        this.sortPriority = sortPriority;
    }


    /**
     * @return the allowedShareTypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_secpolicy_stypes" )
    @Enumerated ( EnumType.STRING )
    public Set<GrantType> getAllowedShareTypes () {
        return this.allowedShareTypes;
    }


    /**
     * @param allowedShareTypes
     *            the allowedShareTypes to set
     */
    @Override
    public void setAllowedShareTypes ( Set<GrantType> allowedShareTypes ) {
        this.allowedShareTypes = allowedShareTypes;
    }


    /**
     * @return the disallowWebDAVAccess
     */
    @Override
    public Boolean getDisallowWebDAVAccess () {
        return this.disallowWebDAVAccess;
    }


    /**
     * @param disallowWebDAVAccess
     *            the disallowWebDAVAccess to set
     */
    @Override
    public void setDisallowWebDAVAccess ( Boolean disallowWebDAVAccess ) {
        this.disallowWebDAVAccess = disallowWebDAVAccess;
    }


    /**
     * @return the maximumExpirationDuration
     */
    @Override
    public Duration getMaximumExpirationDuration () {
        return this.maximumExpirationDuration;
    }


    /**
     * @param maximumExpirationDuration
     *            the maximumExpirationDuration to set
     */
    @Override
    public void setMaximumExpirationDuration ( Duration maximumExpirationDuration ) {
        this.maximumExpirationDuration = maximumExpirationDuration;
    }


    /**
     * @return the maximumShareLifetime
     */
    @Override
    public Duration getMaximumShareLifetime () {
        return this.maximumShareLifetime;
    }


    /**
     * @param maximumShareLifetime
     *            the maximumShareLifetime to set
     */
    @Override
    public void setMaximumShareLifetime ( Duration maximumShareLifetime ) {
        this.maximumShareLifetime = maximumShareLifetime;
    }


    /**
     * @return the transportRequireEncryption
     */
    @Override
    public Boolean getTransportRequireEncryption () {
        return this.transportRequireEncryption;
    }


    /**
     * @param transportRequireEncryption
     *            the transportRequireEncryption to set
     */
    @Override
    public void setTransportRequireEncryption ( Boolean transportRequireEncryption ) {
        this.transportRequireEncryption = transportRequireEncryption;
    }


    /**
     * @return the transportRequirePFS
     */
    @Override
    public Boolean getTransportRequirePFS () {
        return this.transportRequirePFS;
    }


    /**
     * @param transportRequirePFS
     *            the transportRequirePFS to set
     */
    @Override
    public void setTransportRequirePFS ( Boolean transportRequirePFS ) {
        this.transportRequirePFS = transportRequirePFS;
    }


    /**
     * @return the transportMinHashBlockSize
     */
    @Override
    public Integer getTransportMinHashBlockSize () {
        return this.transportMinHashBlockSize;
    }


    /**
     * @param transportMinHashBlockSize
     *            the transportMinHashBlockSize to set
     */
    @Override
    public void setTransportMinHashBlockSize ( Integer transportMinHashBlockSize ) {
        this.transportMinHashBlockSize = transportMinHashBlockSize;
    }


    /**
     * @return the transportMinKeySize
     */
    @Override
    public Integer getTransportMinKeySize () {
        return this.transportMinKeySize;
    }


    /**
     * @param transportMinKeySize
     *            the transportMinKeySize to set
     */
    @Override
    public void setTransportMinKeySize ( Integer transportMinKeySize ) {
        this.transportMinKeySize = transportMinKeySize;
    }


    /**
     * @return the requireAnyRole
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_secpolicy_reqrole" )
    public Set<String> getRequireAnyRole () {
        return this.requireAnyRole;
    }


    /**
     * @param requireAnyRole
     *            the requireAnyRole to set
     */
    @Override
    public void setRequireAnyRole ( Set<String> requireAnyRole ) {
        this.requireAnyRole = requireAnyRole;
    }


    /**
     * @return the disallowRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_secpolicy_disallowrole" )
    public Set<String> getDisallowRoles () {
        return this.disallowRoles;
    }


    /**
     * @param disallowRoles
     *            the disallowRoles to set
     */
    @Override
    public void setDisallowRoles ( Set<String> disallowRoles ) {
        this.disallowRoles = disallowRoles;
    }


    /**
     * @return the minTokenPasswordEntropy
     */
    @Override
    public Integer getMinTokenPasswordEntropy () {
        return this.minTokenPasswordEntropy;
    }


    /**
     * @param minTokenPasswordEntropy
     *            the minTokenPasswordEntropy to set
     */
    @Override
    public void setMinTokenPasswordEntropy ( Integer minTokenPasswordEntropy ) {
        this.minTokenPasswordEntropy = minTokenPasswordEntropy;
    }


    /**
     * @return the requireTokenPassword
     */
    @Override
    public Boolean getRequireTokenPassword () {
        return this.requireTokenPassword;
    }


    /**
     * @param requireTokenPassword
     *            the requireTokenPassword to set
     */
    @Override
    public void setRequireTokenPassword ( Boolean requireTokenPassword ) {
        this.requireTokenPassword = requireTokenPassword;
    }


    /**
     * @return the noUserTokenPasswords
     */
    @Override
    public Boolean getNoUserTokenPasswords () {
        return this.noUserTokenPasswords;
    }


    /**
     * @param noUserTokenPasswords
     *            the noUserTokenPasswords to set
     */
    @Override
    public void setNoUserTokenPasswords ( Boolean noUserTokenPasswords ) {
        this.noUserTokenPasswords = noUserTokenPasswords;
    }


    /**
     * @return the defaultExpirationDuration
     */
    @Override
    public Duration getDefaultExpirationDuration () {
        return this.defaultExpirationDuration;
    }


    /**
     * @param defaultExpirationDuration
     *            the defaultExpirationDuration to set
     */
    @Override
    public void setDefaultExpirationDuration ( Duration defaultExpirationDuration ) {
        this.defaultExpirationDuration = defaultExpirationDuration;
    }


    /**
     * @return the defaultShareLifetime
     */
    @Override
    public Duration getDefaultShareLifetime () {
        return this.defaultShareLifetime;
    }


    /**
     * @param defaultShareLifetime
     *            the defaultShareLifetime to set
     */
    @Override
    public void setDefaultShareLifetime ( Duration defaultShareLifetime ) {
        this.defaultShareLifetime = defaultShareLifetime;
    }


    /**
     * @return the afterShareGracePeriod
     */
    @Override
    public Duration getAfterShareGracePeriod () {
        return this.afterShareGracePeriod;
    }


    /**
     * @param afterShareGracePeriod
     *            the afterShareGracePeriod to set
     */
    @Override
    public void setAfterShareGracePeriod ( Duration afterShareGracePeriod ) {
        this.afterShareGracePeriod = afterShareGracePeriod;
    }


    /**
     * @return the enableDefaultExpiration
     */
    @Override
    public Boolean getEnableDefaultExpiration () {
        return this.enableDefaultExpiration;
    }


    /**
     * @param enableDefaultExpiration
     *            the enableDefaultExpiration to set
     */
    @Override
    public void setEnableDefaultExpiration ( Boolean enableDefaultExpiration ) {
        this.enableDefaultExpiration = enableDefaultExpiration;
    }


    /**
     * @return the restrictExpirationDuration
     */
    @Override
    public Boolean getRestrictExpirationDuration () {
        return this.restrictExpirationDuration;
    }


    /**
     * @param restrictExpirationDuration
     *            the restrictExpirationDuration to set
     */
    @Override
    public void setRestrictExpirationDuration ( Boolean restrictExpirationDuration ) {
        this.restrictExpirationDuration = restrictExpirationDuration;
    }


    /**
     * @return the enableShareExpiration
     */
    @Override
    public Boolean getEnableShareExpiration () {
        return this.enableShareExpiration;
    }


    /**
     * @param enableShareExpiration
     *            the enableShareExpiration to set
     */
    @Override
    public void setEnableShareExpiration ( Boolean enableShareExpiration ) {
        this.enableShareExpiration = enableShareExpiration;
    }


    /**
     * @return the restrictShareLifetime
     */
    @Override
    public Boolean getRestrictShareLifetime () {
        return this.restrictShareLifetime;
    }


    /**
     * @param restrictShareLifetime
     *            the restrictShareLifetime to set
     */
    @Override
    public void setRestrictShareLifetime ( Boolean restrictShareLifetime ) {
        this.restrictShareLifetime = restrictShareLifetime;
    }
}
