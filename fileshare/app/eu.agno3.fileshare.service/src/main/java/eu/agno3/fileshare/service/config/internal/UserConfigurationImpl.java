/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.service.config.UserConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = UserConfiguration.class, configurationPid = "users" )
public class UserConfigurationImpl implements UserConfiguration {

    private boolean registrationEnabled;
    private boolean invitationEnabled;

    private Duration registrationTokenLifetime;
    private Duration invitationTokenLifetime;
    private Duration passwordRecoveryTokenLifetime;

    private Duration registrationUserExpiration;
    private Duration invitationUserExpiration;

    private Map<String, String> lostPasswordUrls = new HashMap<>();

    private Set<String> registrationUserRoles;
    private Set<String> invitationUserRoles;

    private boolean localPasswordRecoveryEnabled;

    private boolean trustInvitedUserNames;
    private boolean allowInvitingUserExtension;

    private Collection<String> staticSynchronizationRoles;
    private Set<String> defaultRoles;

    private Set<String> noSubjectRootRoles;


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> prop ) {

        this.registrationEnabled = ConfigUtil.parseBoolean(prop, "registrationEnabled", false); //$NON-NLS-1$
        this.invitationEnabled = ConfigUtil.parseBoolean(prop, "invitationEnabled", false); //$NON-NLS-1$
        this.localPasswordRecoveryEnabled = ConfigUtil.parseBoolean(prop, "localPasswordRecoveryEnabled", false); //$NON-NLS-1$
        this.registrationTokenLifetime = ConfigUtil.parseDuration(prop, "registrationTokenLifetime", Duration.standardDays(5)); //$NON-NLS-1$
        this.invitationTokenLifetime = ConfigUtil.parseDuration(prop, "invitationTokenLifetime", Duration.standardDays(5)); //$NON-NLS-1$
        this.passwordRecoveryTokenLifetime = ConfigUtil.parseDuration(prop, "passwordRecoveryTokenLifetime", Duration.standardDays(1)); //$NON-NLS-1$

        this.registrationUserExpiration = ConfigUtil.parseDuration(prop, "registrationUserExpiration", null); //$NON-NLS-1$
        this.invitationUserExpiration = ConfigUtil.parseDuration(prop, "invitationUserExpiration", null); //$NON-NLS-1$
        this.lostPasswordUrls = ConfigUtil.parseStringMap(prop, "lostPasswordUrls", new HashMap<>()); //$NON-NLS-1$
        this.registrationUserRoles = ConfigUtil.parseStringSet(prop, "registrationUserRoles",//$NON-NLS-1$ 
            new HashSet<>(Arrays.asList("SELF_REGISTERED_USER"))); //$NON-NLS-1$
        this.invitationUserRoles = ConfigUtil.parseStringSet(prop, "invitationUserRoles", //$NON-NLS-1$ 
            new HashSet<>(Arrays.asList("INVITED_USER", //$NON-NLS-1$
                "EXTERNAL_USER"))); //$NON-NLS-1$
        this.defaultRoles = ConfigUtil.parseStringSet(prop, "defaultRoles", DEFAULT_DEFAULT_ROLES); //$NON-NLS-1$
        this.staticSynchronizationRoles = ConfigUtil.parseStringSet(prop, "staticSynchronizationRoles", DEFAULT_STATIC_SYNC_ROLES); //$NON-NLS-1$

        this.trustInvitedUserNames = ConfigUtil.parseBoolean(prop, "trustInvitedUserNames", false); //$NON-NLS-1$
        this.allowInvitingUserExtension = ConfigUtil.parseBoolean(prop, "allowInvitingUserExtension", true); //$NON-NLS-1$

        this.noSubjectRootRoles = ConfigUtil.parseStringSet(prop, "noSubjectRootRoles", //$NON-NLS-1$
            Collections.singleton("SELF_REGISTERED_USER")); //$NON-NLS-1$
    }

    private static final Set<String> DEFAULT_DEFAULT_ROLES = new HashSet<>();
    private static final Set<String> DEFAULT_STATIC_SYNC_ROLES = new HashSet<>();

    static {
        DEFAULT_DEFAULT_ROLES.add("DEFAULT_USER"); //$NON-NLS-1$

        DEFAULT_STATIC_SYNC_ROLES.add("ADMIN_CREATED_USER"); //$NON-NLS-1$
        DEFAULT_STATIC_SYNC_ROLES.add("SYNCHRONIZED_USER"); //$NON-NLS-1$
        DEFAULT_STATIC_SYNC_ROLES.add("EXTERNAL_USER"); //$NON-NLS-1$
        DEFAULT_STATIC_SYNC_ROLES.add("SELF_REGISTERED_USER"); //$NON-NLS-1$
        DEFAULT_STATIC_SYNC_ROLES.add("INVITED_USER"); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getDefaultRoles()
     */
    @Override
    public Set<String> getDefaultRoles () {
        return this.defaultRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#isRegistrationEnabled()
     */
    @Override
    public boolean isRegistrationEnabled () {
        return this.registrationEnabled;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getRegistrationTokenLifetime()
     */
    @Override
    public Duration getRegistrationTokenLifetime () {
        return this.registrationTokenLifetime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getRegistrationUserExpiration()
     */
    @Override
    public Duration getRegistrationUserExpiration () {
        return this.registrationUserExpiration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getRegistrationUserRoles()
     */
    @Override
    public Set<String> getRegistrationUserRoles () {
        return this.registrationUserRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#isInvitationEnabled()
     */
    @Override
    public boolean isInvitationEnabled () {
        return this.invitationEnabled;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getInvitationTokenLifetime()
     */
    @Override
    public Duration getInvitationTokenLifetime () {
        return this.invitationTokenLifetime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getInvitationUserExpiration()
     */
    @Override
    public Duration getInvitationUserExpiration () {
        return this.invitationUserExpiration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getInvitationUserRoles()
     */
    @Override
    public Set<String> getInvitationUserRoles () {
        return this.invitationUserRoles;
    }


    @Override
    public String getLostPasswordUrl ( String realm ) {
        return this.lostPasswordUrls.get(realm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#isLocalPasswordRecoveryEnabled()
     */
    @Override
    public boolean isLocalPasswordRecoveryEnabled () {
        return this.localPasswordRecoveryEnabled;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getPasswordRecoveryTokenLifetime()
     */
    @Override
    public Duration getPasswordRecoveryTokenLifetime () {
        return this.passwordRecoveryTokenLifetime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#getStaticSynchronizationRoles()
     */
    @Override
    public Collection<String> getStaticSynchronizationRoles () {
        return this.staticSynchronizationRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#isTrustInvitedUserNames()
     */
    @Override
    public boolean isTrustInvitedUserNames () {
        return this.trustInvitedUserNames;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#isAllowInvitingUserExtension()
     */
    @Override
    public boolean isAllowInvitingUserExtension () {
        return this.allowInvitingUserExtension;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.UserConfiguration#hasNoSubjectRoot(java.util.Set)
     */
    @Override
    public boolean hasNoSubjectRoot ( Set<String> roles ) {
        for ( String role : roles ) {
            if ( this.noSubjectRootRoles.contains(role) ) {
                return true;
            }
        }
        return false;
    }
}
