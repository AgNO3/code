/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
@MapAs ( FileshareUserSelfServiceConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_user_selfservice" )
@Audited
@DiscriminatorValue ( "filesh_user_self" )
public class FileshareUserSelfServiceConfigImpl extends AbstractConfigurationObject<FileshareUserSelfServiceConfig> implements
        FileshareUserSelfServiceConfig, FileshareUserSelfServiceConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4084320953546162622L;

    private Boolean registrationEnabled;
    private Duration registrationTokenLifetime;
    private Boolean registrationUserExpires;
    private Duration registrationUserExpiration;
    private Set<String> registrationUserRoles = new HashSet<>();

    private Boolean invitationEnabled;
    private Duration invitationTokenLifetime;
    private Boolean invitationUserExpires;
    private Duration invitationUserExpiration;
    private Set<String> invitationUserRoles = new HashSet<>();
    private Boolean trustInvitedUserNames;
    private Boolean allowInvitingUserExtension;

    private Boolean localPasswordRecoveryEnabled;
    private Duration passwordRecoveryTokenLifetime;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareUserSelfServiceConfig> getType () {
        return FileshareUserSelfServiceConfig.class;
    }


    /**
     * @return the registrationEnabled
     */
    @Override
    public Boolean getRegistrationEnabled () {
        return this.registrationEnabled;
    }


    /**
     * @param registrationEnabled
     *            the registrationEnabled to set
     */
    @Override
    public void setRegistrationEnabled ( Boolean registrationEnabled ) {
        this.registrationEnabled = registrationEnabled;
    }


    /**
     * @return the registrationTokenLifetime
     */
    @Override
    public Duration getRegistrationTokenLifetime () {
        return this.registrationTokenLifetime;
    }


    /**
     * @param registrationTokenLifetime
     *            the registrationTokenLifetime to set
     */
    @Override
    public void setRegistrationTokenLifetime ( Duration registrationTokenLifetime ) {
        this.registrationTokenLifetime = registrationTokenLifetime;
    }


    /**
     * @return the registrationUserExpires
     */
    @Override
    public Boolean getRegistrationUserExpires () {
        return this.registrationUserExpires;
    }


    /**
     * @param registrationUserExpires
     *            the registrationUserExpires to set
     */
    @Override
    public void setRegistrationUserExpires ( Boolean registrationUserExpires ) {
        this.registrationUserExpires = registrationUserExpires;
    }


    /**
     * @return the registrationUserExpiration
     */
    @Override
    public Duration getRegistrationUserExpiration () {
        return this.registrationUserExpiration;
    }


    /**
     * @param registrationUserExpiration
     *            the registrationUserExpiration to set
     */
    @Override
    public void setRegistrationUserExpiration ( Duration registrationUserExpiration ) {
        this.registrationUserExpiration = registrationUserExpiration;
    }


    /**
     * @return the registrationUserRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_user_selfservice_regroles" )
    public Set<String> getRegistrationUserRoles () {
        return this.registrationUserRoles;
    }


    /**
     * @param registrationUserRoles
     *            the registrationUserRoles to set
     */
    @Override
    public void setRegistrationUserRoles ( Set<String> registrationUserRoles ) {
        this.registrationUserRoles = registrationUserRoles;
    }


    /**
     * @return the invitationEnabled
     */
    @Override
    public Boolean getInvitationEnabled () {
        return this.invitationEnabled;
    }


    /**
     * @param invitationEnabled
     *            the invitationEnabled to set
     */
    @Override
    public void setInvitationEnabled ( Boolean invitationEnabled ) {
        this.invitationEnabled = invitationEnabled;
    }


    /**
     * @return the invitationTokenLifetime
     */
    @Override
    public Duration getInvitationTokenLifetime () {
        return this.invitationTokenLifetime;
    }


    /**
     * @param invitationTokenLifetime
     *            the invitationTokenLifetime to set
     */
    @Override
    public void setInvitationTokenLifetime ( Duration invitationTokenLifetime ) {
        this.invitationTokenLifetime = invitationTokenLifetime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserSelfServiceConfig#getInvitationUserExpires()
     */
    @Override
    public Boolean getInvitationUserExpires () {
        return this.invitationUserExpires;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserSelfServiceConfigMutable#setInvitationUserExpires(java.lang.Boolean)
     */
    @Override
    public void setInvitationUserExpires ( Boolean invitationUserExpires ) {
        this.invitationUserExpires = invitationUserExpires;
    }


    /**
     * @return the invitationUserExpiration
     */
    @Override
    public Duration getInvitationUserExpiration () {
        return this.invitationUserExpiration;
    }


    /**
     * @param invitationUserExpiration
     *            the invitationUserExpiration to set
     */
    @Override
    public void setInvitationUserExpiration ( Duration invitationUserExpiration ) {
        this.invitationUserExpiration = invitationUserExpiration;
    }


    /**
     * @return the invitationUserRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_user_selfservice_invroles" )
    public Set<String> getInvitationUserRoles () {
        return this.invitationUserRoles;
    }


    /**
     * @param invitationUserRoles
     *            the invitationUserRoles to set
     */
    @Override
    public void setInvitationUserRoles ( Set<String> invitationUserRoles ) {
        this.invitationUserRoles = invitationUserRoles;
    }


    /**
     * @return the trustInvitedUserNames
     */
    @Override
    public Boolean getTrustInvitedUserNames () {
        return this.trustInvitedUserNames;
    }


    /**
     * @param trustInvitedUserNames
     *            the trustInvitedUserNames to set
     */
    @Override
    public void setTrustInvitedUserNames ( Boolean trustInvitedUserNames ) {
        this.trustInvitedUserNames = trustInvitedUserNames;
    }


    /**
     * @return the allowInvitingUserExtension
     */
    @Override
    public Boolean getAllowInvitingUserExtension () {
        return this.allowInvitingUserExtension;
    }


    /**
     * @param allowInvitingUserExtension
     *            the allowInvitingUserExtension to set
     */
    @Override
    public void setAllowInvitingUserExtension ( Boolean allowInvitingUserExtension ) {
        this.allowInvitingUserExtension = allowInvitingUserExtension;
    }


    /**
     * @return the localPasswordRecoveryEnabled
     */
    @Override
    public Boolean getLocalPasswordRecoveryEnabled () {
        return this.localPasswordRecoveryEnabled;
    }


    /**
     * @param localPasswordRecoveryEnabled
     *            the localPasswordRecoveryEnabled to set
     */
    @Override
    public void setLocalPasswordRecoveryEnabled ( Boolean localPasswordRecoveryEnabled ) {
        this.localPasswordRecoveryEnabled = localPasswordRecoveryEnabled;
    }


    /**
     * @return the passwordRecoveryTokenLifetime
     */
    @Override
    public Duration getPasswordRecoveryTokenLifetime () {
        return this.passwordRecoveryTokenLifetime;
    }


    /**
     * @param passwordRecoveryTokenLifetime
     *            the passwordRecoveryTokenLifetime to set
     */
    @Override
    public void setPasswordRecoveryTokenLifetime ( Duration passwordRecoveryTokenLifetime ) {
        this.passwordRecoveryTokenLifetime = passwordRecoveryTokenLifetime;
    }

}
