/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.util.iter.ClosableIterator;
import eu.agno3.runtime.util.iter.NoCloseIterator;


/**
 * @author mbechler
 *
 */
public class MockSynchronizationHandler implements LDAPSynchronizationHandler {

    private static final Logger log = Logger.getLogger(MockSynchronizationHandler.class);

    private Map<String, UUID> createdUsers = new HashMap<>();
    private Map<String, UUID> createdGroups = new HashMap<>();

    private LDAPRealmConfig config;
    private String realm;
    private LDAPClientFactory clientFactory;


    /**
     * @param config
     * @param realm
     * @param clf
     * 
     */
    public MockSynchronizationHandler ( LDAPRealmConfig config, String realm, LDAPClientFactory clf ) {
        this.config = config;
        this.realm = realm;
        this.clientFactory = clf;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getLastRun()
     */
    @Override
    public DateTime getLastRun () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getClientFactory()
     */
    @Override
    public LDAPClientFactory getClientFactory () {
        return this.clientFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getRealm()
     */
    @Override
    public String getRealm () {
        return this.realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getConfig()
     */
    @Override
    public LDAPRealmConfig getConfig () {
        return this.config;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#userExistsById(java.util.UUID)
     */
    @Override
    public boolean userExistsById ( UUID uuid ) {
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserByName(java.lang.String)
     */
    @Override
    public UUID getUserByName ( String name ) {
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserByDN(java.lang.String)
     */
    @Override
    public UUID getUserByDN ( String dn ) {
        return this.createdUsers.get(dn);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#groupExistsById(java.util.UUID)
     */
    @Override
    public boolean groupExistsById ( UUID candUUID ) {
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupByDN(java.lang.String)
     */
    @Override
    public UUID getGroupByDN ( String dn ) {
        return this.createdGroups.get(dn);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupByName(java.lang.String)
     */
    @Override
    public UUID getGroupByName ( String attributeValue ) {
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#createGroup(java.lang.String, java.util.UUID,
     *      eu.agno3.runtime.security.ldap.LDAPGroup)
     */
    @Override
    public UUID createGroup ( String dn, UUID directoryId, LDAPGroup groupEntry ) {
        UUID groupId = UUID.randomUUID();
        this.createdGroups.put(dn, groupId);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Created fake group %s for %s: %s", groupId, dn, groupEntry.getDisplayName())); //$NON-NLS-1$
        }
        return groupId;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#updateGroup(java.util.UUID, java.lang.String,
     *      eu.agno3.runtime.security.ldap.LDAPGroup)
     */
    @Override
    public void updateGroup ( UUID groupId, String dn, LDAPGroup groupEntry ) {}


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#updateUser(java.util.UUID, java.lang.String,
     *      eu.agno3.runtime.security.ldap.LDAPUser, java.util.Set)
     */
    @Override
    public void updateUser ( UUID userId, String dn, LDAPUser userEntry, Set<String> roles ) {}


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#createUser(java.lang.String, java.util.UUID,
     *      eu.agno3.runtime.security.ldap.LDAPUser, java.util.Set)
     */
    @Override
    public UUID createUser ( String dn, UUID directoryId, LDAPUser userEntry, Set<String> roles ) {
        UUID userId = UUID.randomUUID();
        this.createdUsers.put(dn, userId);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Created fake user %s for %s: %s", userId, dn, userEntry.getDisplayName())); //$NON-NLS-1$
        }
        return userId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setMembers(java.util.UUID, java.util.Set)
     */
    @Override
    public void setMembers ( UUID groupId, Set<UUID> newNestedGroups ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setNestedGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void setNestedGroups ( UUID groupId, Set<UUID> newNestedGroups ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setForwardNestedGroups(java.util.UUID,
     *      java.util.Set)
     */
    @Override
    public void setForwardNestedGroups ( UUID groupId, Set<UUID> newForwardNestedGroups ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setForwardMembership(java.util.UUID,
     *      java.util.Set)
     */
    @Override
    public void setForwardMembership ( UUID userId, Set<UUID> newMembershipUUIDs ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserDNs()
     */
    @Override
    public ClosableIterator<String> getUserDNs () {
        return new NoCloseIterator<>(this.createdUsers.keySet().iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserIds()
     */
    @Override
    public ClosableIterator<UUID> getUserIds () {
        return new NoCloseIterator<>(this.createdUsers.values().iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupDNs()
     */
    @Override
    public ClosableIterator<String> getGroupDNs () {
        return new NoCloseIterator<>(this.createdGroups.keySet().iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupIds()
     */
    @Override
    public ClosableIterator<UUID> getGroupIds () {
        return new NoCloseIterator<>(this.createdGroups.values().iterator());

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeGroup(java.util.UUID)
     */
    @Override
    public void removeGroup ( UUID groupId ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeGroups(java.util.Set)
     */
    @Override
    public void removeGroups ( Set<UUID> toDelete ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeUser(java.util.UUID)
     */
    @Override
    public void removeUser ( UUID userId ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeUsers(java.util.Set)
     */
    @Override
    public void removeUsers ( Set<UUID> toDelete ) {
        // ignore
    }
}
