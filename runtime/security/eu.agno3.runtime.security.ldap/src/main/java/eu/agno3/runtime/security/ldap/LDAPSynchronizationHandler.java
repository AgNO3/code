/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 *
 */
public interface LDAPSynchronizationHandler {

    /**
     * @return the ldap config
     */
    LDAPRealmConfig getConfig ();


    /**
     * 
     * @return the client factory
     */
    LDAPClientFactory getClientFactory ();


    /**
     * @return the target realm
     */
    String getRealm ();


    /**
     * @return the last run time
     */
    DateTime getLastRun ();


    /**
     * @param realm
     * @param uuid
     * @param fromString
     * @return whether the user exists
     */
    boolean userExistsById ( UUID uuid );


    /**
     * @param realm
     * @param name
     * @return the user id
     */
    UUID getUserByName ( String name );


    /**
     * @param realm
     * @param dn
     * @return the user id
     */
    UUID getUserByDN ( String dn );


    /**
     * @param realm
     * @param candUUID
     * @return whether the group exists
     */
    boolean groupExistsById ( UUID candUUID );


    /**
     * @param realm
     * @param dn
     * @return the group id
     */
    UUID getGroupByDN ( String dn );


    /**
     * @param realm
     * @param attributeValue
     * @return the group id
     */
    UUID getGroupByName ( String attributeValue );


    /**
     * @param dn
     * @param directoryId
     * @param groupEntry
     * @param roles
     * @return the id of the created group
     */
    UUID createGroup ( String dn, UUID directoryId, LDAPGroup groupEntry );


    /**
     * @param groupId
     * @param dn
     * @param groupEntry
     * @param roles
     */
    void updateGroup ( UUID groupId, String dn, LDAPGroup groupEntry );


    /**
     * @param userId
     * @param dn
     * @param userEntry
     * @param roles
     */
    void updateUser ( UUID userId, String dn, LDAPUser userEntry, Set<String> roles );


    /**
     * @param dn
     * @param directoryId
     * @param userEntry
     * @param userRoles
     * @param roles
     * @return the id of the created user
     */
    UUID createUser ( String dn, UUID directoryId, LDAPUser userEntry, Set<String> userRoles );


    /**
     * @param groupId
     * @param newNestedGroups
     */
    void setMembers ( UUID groupId, Set<UUID> newNestedGroups );


    /**
     * @param groupId
     * @param newNestedGroups
     */
    void setNestedGroups ( UUID groupId, Set<UUID> newNestedGroups );


    /**
     * @param groupId
     * @param newForwardNestedGroups
     */
    void setForwardNestedGroups ( UUID groupId, Set<UUID> newForwardNestedGroups );


    /**
     * @param userId
     * @param newMembershipUUIDs
     */
    void setForwardMembership ( UUID userId, Set<UUID> newMembershipUUIDs );


    /**
     * 
     * @return an iterator of all existing external user IDs in the realm
     */
    ClosableIterator<UUID> getUserIds ();


    /**
     * 
     * @return an iterator of all existing external group IDs in the realm
     */
    ClosableIterator<UUID> getGroupIds ();


    /**
     * @return an iterator of all existing external user DNs in the realm
     * 
     */
    ClosableIterator<String> getUserDNs ();


    /**
     * @return an iterator of all existing external group DNs in the realm
     * 
     */
    ClosableIterator<String> getGroupDNs ();


    /**
     * @param userId
     */
    void removeUser ( UUID userId );


    /**
     * @param toDelete
     */
    void removeUsers ( Set<UUID> toDelete );


    /**
     * @param groupId
     */
    void removeGroup ( UUID groupId );


    /**
     * @param toDelete
     */
    void removeGroups ( Set<UUID> toDelete );

}
