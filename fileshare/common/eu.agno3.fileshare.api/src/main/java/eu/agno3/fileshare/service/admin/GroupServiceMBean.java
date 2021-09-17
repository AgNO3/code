/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.admin;


import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.runtime.jmx.MBean;


/**
 * @author mbechler
 *
 */
public interface GroupServiceMBean extends MBean {

    /**
     * List the known groups
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @param off
     *            paging offset
     * @param limit
     *            paging limit
     * @return groups
     * @throws FileshareException
     */
    List<Group> listGroups ( int off, int limit ) throws FileshareException;


    /**
     * The total number of known groups
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @return the number of groups
     * @throws FileshareException
     */
    long getGroupCount () throws FileshareException;


    /**
     * Get a group by id
     * 
     * Access control:
     * - have manage:subjects:list
     * - OR membership of the group
     * 
     * @param id
     * @return the given group
     * @throws FileshareException
     */
    Group getGroup ( UUID id ) throws FileshareException;


    /**
     * Create a group
     * 
     * Access control:
     * - require manage:groups:create
     * 
     * @param group
     * @param createRoot
     *            whether to create a subject root for the group
     * @return the created group
     * @throws FileshareException
     */
    Group createGroup ( Group group, boolean createRoot ) throws FileshareException;


    /**
     * Delete groups
     * 
     * Access control:
     * - require manage:groups:delete
     * 
     * This will remove all files stored in this group
     * 
     * @param ids
     * @throws FileshareException
     */
    void deleteGroups ( List<UUID> ids ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:subjects:list
     * - OR membership of the group
     * 
     * @param subjectName
     * @return group result
     * @throws FileshareException
     */
    GroupQueryResult getGroupInfo ( String subjectName ) throws FileshareException;


    /**
     * Query groups
     * 
     * Access control:
     * - have manage:subjects:list
     * OR
     * - have groups:query
     * OR
     * - only return groups the user is a member of
     * 
     * @param query
     * @param limit
     * @return the query results
     * @throws FileshareException
     * @throws AuthenticationException
     */
    List<GroupQueryResult> queryGroups ( String query, int limit ) throws FileshareException;


    /**
     * Query groups, excluding the given user's member groups
     * 
     * Access control:
     * - have manage:subjects:list
     * OR
     * - have groups:query AND user is current user
     * 
     * @param query
     * @param userId
     * @param limit
     * @param singleSelectionId
     * @param i
     * @return the query results
     * @throws FileshareException
     * @throws UserNotFoundException
     */
    List<GroupQueryResult> queryGroupsExcludingUserGroups ( String query, UUID userId, int limit ) throws FileshareException;


    /**
     * Add a user to a group
     * 
     * Access control:
     * - have manage:groups:addUser
     * 
     * @param userId
     * @param groupId
     * @throws FileshareException
     */
    void addToGroup ( UUID userId, UUID groupId ) throws FileshareException;


    /**
     * Add a user to multiple groups
     * 
     * Access control:
     * - have manage:groups:addMember
     * 
     * @param id
     * @param groupIds
     * @throws FileshareException
     */
    void addToGroups ( UUID id, Set<UUID> groupIds ) throws FileshareException;


    /**
     * Get group members
     * 
     * Access control:
     * - have manage:subjects:list
     * - OR membership of the group
     * 
     * @param groupId
     * @return the group members
     * @throws FileshareException
     */
    List<Subject> getMembers ( UUID groupId ) throws FileshareException;


    /**
     * Remove a user from a group
     * 
     * Access control:
     * - have manage:groups:removeMember
     * 
     * @param userId
     * @param groupId
     * @throws FileshareException
     */
    void removeFromGroup ( UUID userId, UUID groupId ) throws FileshareException;


    /**
     * Remove a user from multiple groups
     * 
     * Access control:
     * - have manage:groups:removeMember
     * 
     * @param userId
     * @param groupIds
     * @throws FileshareException
     */
    void removeFromGroups ( UUID userId, Set<UUID> groupIds ) throws FileshareException;


    /**
     * Add multiple members to a group
     * 
     * Access control:
     * - have manage:groups:addMember
     * 
     * @param groupId
     * @param subjectIds
     * @throws FileshareException
     */
    void addMembers ( UUID groupId, List<UUID> subjectIds ) throws FileshareException;


    /**
     * Remove multiple members
     * 
     * Access control:
     * - have manage:groups:removeMember
     * 
     * @param groupId
     * @param subjectIds
     * @throws FileshareException
     */
    void removeMembers ( UUID groupId, List<UUID> subjectIds ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:groups:manage:groups:changeNotifySettings
     * 
     * @param id
     * @param disableNotifications
     * @throws FileshareException
     */
    void setNotificationDisabled ( UUID id, boolean disableNotifications ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:groups:manage:groups:changeNotifySettings
     * 
     * @param id
     * @param overrideAddress
     *            address or null to unset
     * @throws FileshareException
     */
    void setNotificationOverride ( UUID id, String overrideAddress ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:groups:manage:groups:changeNotifySettings
     * 
     * @param id
     * @param groupLocale
     * @throws FileshareException
     */
    void setGroupLocale ( UUID id, Locale groupLocale ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:groups:manage:groups:changeQuota
     * 
     * @param groupId
     * @param quota
     * @throws FileshareException
     */
    void updateGroupQuota ( UUID groupId, Long quota ) throws FileshareException;


    /**
     * @return the last modification of the user's groups
     */
    DateTime getGroupsLastModified ();


    /**
     * @return the last modification time of any group or it's contents
     */
    DateTime getGroupsRecursiveLastModified ();

}