/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;

import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.security.ldap.LDAPGroupAttrs;
import eu.agno3.runtime.security.ldap.LDAPOperationalAttrs;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationException;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationRuntimeException;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;
import eu.agno3.runtime.security.ldap.LDAPUserSynchronizer;
import eu.agno3.runtime.util.iter.ClosableIterator;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LDAPUserSynchronizer.class )
public class LDAPUserSynchronizerImpl implements LDAPUserSynchronizer {

    private static final Logger log = Logger.getLogger(LDAPUserSynchronizerImpl.class);

    private static final DateTimeFormatter RFC4517_TIME_FORMAT = DateTimeFormat.forPattern("yyyyMMddHHmmss'Z'"); //$NON-NLS-1$


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUserSynchronizer#run(eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler)
     */
    @Override
    public void run ( LDAPSynchronizationHandler handler ) {
        LDAPSynchronizationHandler realHandler = handler;
        if ( log.isDebugEnabled() ) {
            realHandler = new LoggingSynchronizationHandler(realHandler);
        }
        log.debug("Running ldap synchronizer"); //$NON-NLS-1$
        Set<String> handledUserRefs = Collections.synchronizedSet(new HashSet<>());
        Set<String> handledGroupRefs = Collections.synchronizedSet(new HashSet<>());
        Filter lastModFilter = makeLastModifiedFilter(handler);
        try ( LDAPClient cl = handler.getClientFactory().getConnection();
              LDAPClient subClient = handler.getClientFactory().getConnection() ) {

            if ( !cl.isControlSupported(SimplePagedResultsControl.PAGED_RESULTS_OID) ) {
                log.debug("Running without paging support"); //$NON-NLS-1$
            }

            if ( handler.getConfig().isUseForwardGroups() ) {
                synchronizeGroups(realHandler, handledUserRefs, handledGroupRefs, lastModFilter, cl, subClient);
                synchronizeUsers(realHandler, handledUserRefs, handledGroupRefs, lastModFilter, cl, subClient);
            }
            else {
                synchronizeUsers(realHandler, handledUserRefs, handledGroupRefs, lastModFilter, cl, subClient);
                synchronizeGroups(realHandler, handledUserRefs, handledGroupRefs, lastModFilter, cl, subClient);
            }

            synchronizeRemovals(handler, handledUserRefs, handledGroupRefs, cl);
        }
        catch (
            LDAPException |
            LDAPSynchronizationRuntimeException e ) {
            log.warn("Exception while running synchronization", e); //$NON-NLS-1$
        }
    }


    /**
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param cl2
     * @throws LDAPException
     */
    private static void synchronizeRemovals ( LDAPSynchronizationHandler handler, Set<String> handledUserRefs, Set<String> handledGroupRefs,
            LDAPClient cl ) throws LDAPException {
        if ( !handler.getConfig().isRemoveMissing() ) {
            return;
        }
        long start = System.currentTimeMillis();

        if ( handler.getConfig().isRemovalUseUUIDs() ) {
            synchronizeRemovalOnID(handler, cl);
        }
        else {
            synchronizeRemovalOnDN(handler, cl);
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Removal synchronization took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0f)); //$NON-NLS-1$
        }
    }


    /**
     * @param handler
     * @param cl
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private static void synchronizeRemovalOnID ( LDAPSynchronizationHandler handler, LDAPClient cl ) throws LDAPSearchException, LDAPException {
        removeUsersById(handler, cl);
        removeGroupsById(handler, cl);
    }


    /**
     * @param handler
     * @param cl
     * @throws LDAPSearchException
     * @throws LDAPException
     */
    private static void removeUsersById ( LDAPSynchronizationHandler handler, LDAPClient cl ) throws LDAPSearchException, LDAPException {
        Set<UUID> toDelete = new HashSet<>();
        try ( ClosableIterator<UUID> userId = handler.getUserIds() ) {
            while ( userId.hasNext() ) {
                UUID idToCheck = userId.next();
                Filter userFilter = Filter.createANDFilter(handler.getConfig().getUserConfig().getFilter(), makeIDFilter(handler, idToCheck));
                log.trace(userFilter);
                SearchResultEntry found = cl.searchForEntry(
                    cl.relativeDN(handler.getConfig().getUserConfig().getBaseDN()).toString(),
                    handler.getConfig().getUserConfig().getScope(),
                    userFilter,
                    handler.getConfig().getUserMapper().getAttributeName(LDAPUserAttrs.NAME));
                if ( found == null || handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), found.getParsedDN()) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing user by ID " + idToCheck); //$NON-NLS-1$
                    }
                    toDelete.add(idToCheck);
                }
            }
        }
        handler.removeUsers(toDelete);
    }


    private static Filter makeIDFilter ( LDAPSynchronizationHandler handler, UUID id ) {
        if ( handler.getConfig().getStyle().isIdsAreBinary() ) {
            byte[] bytes = UUIDUtil.toBytes(id);
            return Filter.createEqualityFilter(handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID), bytes);
        }

        return Filter.createEqualityFilter(handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID), id.toString());

    }


    /**
     * @param handler
     * @param cl
     * @throws LDAPSearchException
     * @throws LDAPException
     */
    private static void removeGroupsById ( LDAPSynchronizationHandler handler, LDAPClient cl ) throws LDAPSearchException, LDAPException {
        Set<UUID> toDelete = new HashSet<>();
        try ( ClosableIterator<UUID> groupId = handler.getGroupIds() ) {
            while ( groupId.hasNext() ) {
                UUID idToCheck = groupId.next();
                Filter groupFilter = Filter.createANDFilter(handler.getConfig().getGroupConfig().getFilter(), makeIDFilter(handler, idToCheck));
                log.trace(groupFilter);
                SearchResultEntry found = cl.searchForEntry(
                    cl.relativeDN(handler.getConfig().getGroupConfig().getBaseDN()).toString(),
                    handler.getConfig().getGroupConfig().getScope(),
                    groupFilter,
                    handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME));
                if ( found == null || handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), found.getParsedDN()) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing group by ID " + idToCheck); //$NON-NLS-1$
                    }
                    toDelete.add(idToCheck);
                }
            }
        }
        handler.removeGroups(toDelete);
    }


    /**
     * @param handler
     * @param cl
     * @throws LDAPException
     */
    private static void synchronizeRemovalOnDN ( LDAPSynchronizationHandler handler, LDAPClient cl ) throws LDAPException {
        removeUsersByDN(handler, cl);
        removeGroupsByDN(handler, cl);
    }


    /**
     * @param handler
     * @param cl
     * @throws LDAPException
     */
    private static void removeGroupsByDN ( LDAPSynchronizationHandler handler, LDAPClient cl ) throws LDAPException {
        Set<UUID> toDelete = new HashSet<>();
        try ( ClosableIterator<String> groupDn = handler.getGroupDNs() ) {
            while ( groupDn.hasNext() ) {
                String dnToCheck = groupDn.next();
                SearchResultEntry found = cl.searchForEntry(
                    dnToCheck,
                    SearchScope.BASE,
                    handler.getConfig().getGroupConfig().getFilter(),
                    handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME));
                if ( found == null || handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), found.getParsedDN()) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing group by DN " + dnToCheck); //$NON-NLS-1$
                    }
                    UUID groupId = handler.getGroupByDN(dnToCheck);
                    if ( groupId != null ) {
                        toDelete.add(groupId);

                    }
                    else {
                        log.warn("Failed to get group " + dnToCheck); //$NON-NLS-1$
                    }
                }
            }
        }
        handler.removeGroups(toDelete);
    }


    /**
     * @param handler
     * @param cl
     * @throws LDAPException
     */
    private static void removeUsersByDN ( LDAPSynchronizationHandler handler, LDAPClient cl ) throws LDAPException {
        Set<UUID> toDelete = new HashSet<>();
        try ( ClosableIterator<String> userDn = handler.getUserDNs() ) {
            while ( userDn.hasNext() ) {
                String dnToCheck = userDn.next();
                SearchResultEntry found = cl.searchForEntry(
                    dnToCheck,
                    SearchScope.BASE,
                    handler.getConfig().getUserConfig().getFilter(),
                    handler.getConfig().getUserMapper().getAttributeName(LDAPUserAttrs.NAME));
                if ( found == null || handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), found.getParsedDN()) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing user by DN " + dnToCheck); //$NON-NLS-1$
                    }
                    UUID userId = handler.getUserByDN(dnToCheck);
                    if ( userId != null ) {
                        toDelete.add(userId);
                    }
                    else {
                        log.warn("Failed to get user " + dnToCheck); //$NON-NLS-1$
                    }
                }
            }
        }
        handler.removeUsers(toDelete);
    }


    /**
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param lastModFilter
     * @param cl
     * @param subClient
     * @param config
     * @throws LDAPException
     */
    private void synchronizeGroups ( LDAPSynchronizationHandler handler, Set<String> handledUserRefs, Set<String> handledGroupRefs,
            Filter lastModFilter, LDAPClient cl, LDAPClient subClient ) throws LDAPException {
        long start = System.currentTimeMillis();
        Filter groupFilter = Filter.createANDFilter(handler.getConfig().getGroupConfig().getFilter(), lastModFilter);

        SearchRequest searchRequest = new SearchRequest(
            new GroupSearchResultListener(this, handler, handledUserRefs, handledGroupRefs, subClient),
            cl.relativeDN(handler.getConfig().getGroupConfig().getBaseDN()).toString(),
            handler.getConfig().getGroupConfig().getScope(),
            groupFilter,
            handler.getConfig().getGroupAttrs());
        searchRequest.setControls(new SimplePagedResultsControl(handler.getConfig().getPageSize()));
        SearchResult search = cl.search(searchRequest);

        if ( search.getResultCode() != ResultCode.SUCCESS ) {
            throw new LDAPException(search);
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Group synchronization took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0f)); //$NON-NLS-1$
        }
    }


    /**
     * @param handler
     * @param handledUserDNs
     * @param handledGroupDNs
     * @param lastModFilter
     * @param cl
     * @param subClient
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private void synchronizeUsers ( LDAPSynchronizationHandler handler, Set<String> handledUserDNs, Set<String> handledGroupDNs, Filter lastModFilter,
            LDAPClient cl, LDAPClient subClient ) throws LDAPException, LDAPSearchException {
        long start = System.currentTimeMillis();
        Filter userFilter = Filter.createANDFilter(handler.getConfig().getUserConfig().getFilter(), lastModFilter);

        UserSearchResultListener sl = new UserSearchResultListener(this, handler, handledUserDNs, handledGroupDNs, subClient);
        SearchRequest searchRequest = new SearchRequest(
            sl,
            cl.relativeDN(handler.getConfig().getUserConfig().getBaseDN()).toString(),
            handler.getConfig().getUserConfig().getScope(),
            userFilter,
            handler.getConfig().getUserAttrs());
        searchRequest.setControls(new SimplePagedResultsControl(handler.getConfig().getPageSize()));
        SearchResult search = cl.search(searchRequest);

        if ( search.getResultCode() != ResultCode.SUCCESS ) {
            throw new LDAPException(search);
        }

        long errors = sl.getErrors();
        if ( errors > 0 ) {
            log.warn(
                String.format(
                    "%d user(s) could not be created. Full synchronization (service restart) might be necessary to recover them after fixing the issue.", //$NON-NLS-1$
                    errors));
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("User synchronization took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0f)); //$NON-NLS-1$
        }
    }


    UUID handleUserResult ( SearchResultEntry userEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPException {
        String ref = getRef(handler, userEntry, handler.getConfig().getUserMapper().getAttributeName(LDAPUserAttrs.NAME));
        if ( !handledUserRefs.add(ref) ) {
            return null;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Found user result " + userEntry); //$NON-NLS-1$
        }
        UUID userId = findExistingUserId(userEntry, handler);
        if ( userId != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("User already exists with id " + userId); //$NON-NLS-1$
            }

            return handleExistingUser(userEntry, handler, handledUserRefs, handledGroupRefs, userId, cl);
        }

        UUID uuid = handleNewUser(userEntry, handler, handledUserRefs, handledGroupRefs, cl);
        if ( uuid == null ) {
            handledUserRefs.remove(ref);
        }
        return uuid;
    }


    private static String getRef ( LDAPSynchronizationHandler handler, SearchResultEntry entry, String idAttr ) {
        if ( handler.getConfig().isReferencesAreDNs() ) {
            return entry.getDN();
        }
        String attributeValue = entry.getAttributeValue(idAttr);

        if ( StringUtils.isBlank(attributeValue) ) {
            throw new LDAPSynchronizationRuntimeException("Identifier attribute is empty for " + entry); //$NON-NLS-1$
        }

        return attributeValue;
    }


    /**
     * @param userEntry
     * @param handler
     * @return
     */
    private static UUID findExistingUserId ( SearchResultEntry userEntry, LDAPSynchronizationHandler handler ) {

        UUID userId = null;
        String uuidAttr = handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID);
        if ( userEntry.hasAttribute(uuidAttr) ) {
            UUID candUUID = extractUUID(handler, userEntry, uuidAttr);
            if ( handler.userExistsById(candUUID) ) {
                return candUUID;
            }
        }

        userId = handler.getUserByDN(userEntry.getDN());
        if ( userId != null ) {
            return userId;
        }

        String userName = userEntry.getAttributeValue(handler.getConfig().getUserMapper().getAttributeName(LDAPUserAttrs.NAME));
        if ( StringUtils.isBlank(userName) ) {
            return null;
        }
        return handler.getUserByName(userName);
    }


    /**
     * @param handler
     * @param userEntry
     * @param uuidAttr
     * @return
     */
    private static UUID extractUUID ( LDAPSynchronizationHandler handler, SearchResultEntry userEntry, String uuidAttr ) {
        if ( handler.getConfig().getStyle().isIdsAreBinary() ) {
            return UUIDUtil.fromBytes(userEntry.getAttributeValueBytes(uuidAttr));
        }
        return UUID.fromString(userEntry.getAttributeValue(uuidAttr));
    }


    /**
     * @param userEntry
     * @param handler
     * @param handledUserRefs
     * @param cl
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private static UUID handleNewUser ( SearchResultEntry userEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPException {

        if ( handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), userEntry.getParsedDN()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("ignoring user " + userEntry); //$NON-NLS-1$
            }
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("New user for " + userEntry); //$NON-NLS-1$
        }

        String uuidAttr = handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID);
        UUID uuid = null;

        if ( userEntry.hasAttribute(uuidAttr) ) {
            uuid = extractUUID(handler, userEntry, uuidAttr);
        }

        LDAPUser mappedUser = handler.getConfig().getUserMapper().mapObject(userEntry);

        if ( StringUtils.isEmpty(mappedUser.getUsername()) ) {
            log.warn("Could not find username for " + userEntry.getDN()); //$NON-NLS-1$
            return null;
        }

        Set<String> userRoles = LDAPGroupResolverUtil
                .resolveUserRoles(handler.getConfig(), cl, userEntry.getDN(), mappedUser.getUsername(), userEntry);
        UUID userId = handler.createUser(userEntry.getDN(), uuid, mappedUser, userRoles);
        if ( userId != null ) {
            synchronizeDirectMembership(userId, userEntry, handler, handledUserRefs, handledGroupRefs, cl);
        }
        return userId;
    }


    /**
     * @param userEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param cl2
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private static void synchronizeDirectMembership ( UUID userId, SearchResultEntry userEntry, LDAPSynchronizationHandler handler,
            Set<String> handledUserRefs, Set<String> handledGroupRefs, LDAPClient cl )
                    throws LDAPSynchronizationException, LDAPSearchException, LDAPException {

        Set<UUID> newMembershipUUIDs = new HashSet<>();
        String memberOfAttr = handler.getConfig().getUserMapper().getAttributeName(LDAPUserAttrs.MEMBER_OF);
        if ( handler.getConfig().isUseForwardGroups() && userEntry.hasAttribute(memberOfAttr) ) {
            for ( String memberOfDn : userEntry.getAttributeValues(memberOfAttr) ) {
                if ( !isValidGroup(handler, memberOfDn, cl) ) {
                    continue;
                }

                if ( !handledGroupRefs.contains(memberOfDn) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Group was not synchronized, possbile filter exclusion: " + memberOfDn); //$NON-NLS-1$
                    }
                    continue;
                }
                newMembershipUUIDs.add(locateGroupIDByRef(handler, memberOfDn, cl));
            }

            handler.setForwardMembership(userId, newMembershipUUIDs);

        }
        else if ( handler.getConfig().isUseForwardGroups() ) {
            handler.setForwardMembership(userId, Collections.EMPTY_SET);
        }

    }


    /**
     * @param handler
     * @param ref
     * @param cl
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     */
    private static UUID locateUserIDByRef ( LDAPSynchronizationHandler handler, String ref, LDAPClient cl )
            throws LDAPSynchronizationException, LDAPException {
        UUID user;
        if ( handler.getConfig().isReferencesAreDNs() ) {
            user = handler.getUserByDN(ref);
            if ( user == null ) {
                user = findExistingUserId(getUserEntryByRef(handler, ref, cl), handler);
            }
        }
        else {
            user = handler.getUserByName(ref);

            if ( user == null ) {
                user = findExistingUserId(getUserEntryByRef(handler, ref, cl), handler);
            }
        }

        if ( user == null ) {
            throw new LDAPSynchronizationException("Could not resolve user reference " + ref); //$NON-NLS-1$
        }

        return user;
    }


    /**
     * @param handler
     * @param ref
     * @param cl
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     */
    private static UUID locateGroupIDByRef ( LDAPSynchronizationHandler handler, String ref, LDAPClient cl )
            throws LDAPSynchronizationException, LDAPException {
        UUID group = null;
        if ( handler.getConfig().isReferencesAreDNs() && !StringUtils.isBlank(ref) ) {
            group = handler.getGroupByDN(ref);
            if ( group == null ) {
                group = findExistingGroupId(getGroupEntryByRef(handler, ref, cl), handler);
            }
        }
        else if ( !StringUtils.isBlank(ref) ) {
            group = handler.getGroupByName(ref);

            if ( group == null ) {
                group = findExistingGroupId(getGroupEntryByRef(handler, ref, cl), handler);
            }
        }

        if ( group == null ) {
            throw new LDAPSynchronizationException("Could not resolve group reference " + ref); //$NON-NLS-1$
        }

        return group;
    }


    /**
     * @param ref
     * @return
     * @throws LDAPException
     */
    private static SearchResultEntry getGroupEntryByRef ( LDAPSynchronizationHandler handler, String ref, LDAPClient cl ) throws LDAPException {
        if ( handler.getConfig().isReferencesAreDNs() ) {
            return cl.searchForEntry(ref, SearchScope.BASE, handler.getConfig().getGroupConfig().getFilter(), handler.getConfig().getGroupAttrs());
        }
        Filter groupFilter = Filter.createANDFilter(
            handler.getConfig().getGroupConfig().getFilter(),
            Filter.createEqualityFilter(handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME), ref));

        return cl.searchForEntry(
            cl.relativeDN(handler.getConfig().getGroupConfig().getBaseDN()).toString(),
            handler.getConfig().getGroupConfig().getScope(),
            groupFilter,
            handler.getConfig().getGroupAttrs());
    }


    /**
     * @param ref
     * @param cl2
     * @return
     * @throws LDAPException
     */
    private static SearchResultEntry getUserEntryByRef ( LDAPSynchronizationHandler handler, String ref, LDAPClient cl ) throws LDAPException {

        Filter groupFilter = Filter.createANDFilter(
            handler.getConfig().getUserConfig().getFilter(),
            Filter.createEqualityFilter(handler.getConfig().getUserMapper().getAttributeName(LDAPUserAttrs.NAME), ref));

        if ( handler.getConfig().isReferencesAreDNs() ) {
            return cl.searchForEntry(ref, SearchScope.BASE, handler.getConfig().getUserConfig().getFilter(), handler.getConfig().getUserAttrs());
        }

        return cl.searchForEntry(
            cl.relativeDN(handler.getConfig().getUserConfig().getBaseDN()).toString(),
            handler.getConfig().getUserConfig().getScope(),
            groupFilter,
            handler.getConfig().getUserAttrs());
    }


    /**
     * @param userEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param userId
     * @param cl
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private static UUID handleExistingUser ( SearchResultEntry userEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, UUID userId, LDAPClient cl ) throws LDAPSynchronizationException, LDAPSearchException, LDAPException {
        if ( log.isDebugEnabled() ) {
            log.debug("Existing user for " + userEntry); //$NON-NLS-1$
        }

        LDAPUser mapped = handler.getConfig().getUserMapper().mapObject(userEntry);
        Set<String> userRoles = LDAPGroupResolverUtil.resolveUserRoles(handler.getConfig(), cl, userEntry.getDN(), mapped.getUsername(), userEntry);
        handler.updateUser(userId, userEntry.getDN(), mapped, userRoles);
        synchronizeDirectMembership(userId, userEntry, handler, handledUserRefs, handledGroupRefs, cl);
        return userId;
    }


    UUID handleGroupResult ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPSearchException, LDAPException {
        String ref = getRef(handler, groupEntry, handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME));
        if ( !handledGroupRefs.add(ref) ) {
            return null;
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Found group result " + groupEntry); //$NON-NLS-1$
        }

        UUID groupId = findExistingGroupId(groupEntry, handler);
        if ( groupId != null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Group already exists with id " + groupId); //$NON-NLS-1$
            }

            return handleExistingGroup(groupEntry, handler, handledUserRefs, handledGroupRefs, groupId, cl);
        }

        return handleNewGroup(groupEntry, handler, handledUserRefs, handledGroupRefs, cl);
    }


    /**
     * @param groupEntry
     * @param handler
     * @return
     */
    private static UUID findExistingGroupId ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler ) {
        UUID groupId = null;
        String uuidAttr = handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID);
        if ( groupEntry.hasAttribute(uuidAttr) ) {
            UUID candUUID = extractUUID(handler, groupEntry, uuidAttr);
            if ( handler.groupExistsById(candUUID) ) {
                return candUUID;
            }
        }

        groupId = handler.getGroupByDN(groupEntry.getDN());
        if ( groupId != null ) {
            return groupId;
        }

        String groupName = groupEntry.getAttributeValue(handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME));
        if ( StringUtils.isBlank(groupName) ) {
            return null;
        }

        return handler.getGroupByName(groupName);
    }


    /**
     * @param groupEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param groupId
     * @param cl
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private UUID handleExistingGroup ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, UUID groupId, LDAPClient cl ) throws LDAPSearchException, LDAPException, LDAPSynchronizationException {
        if ( log.isDebugEnabled() ) {
            log.debug("Existing group for " + groupEntry); //$NON-NLS-1$
        }

        handler.updateGroup(groupId, groupEntry.getDN(), handler.getConfig().getGroupMapper().mapObject(groupEntry));
        synchronizeGroupMemberships(groupId, groupEntry, handler, handledUserRefs, handledGroupRefs, cl);
        return groupId;
    }


    /**
     * @param groupEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param cl
     * @return
     * @throws LDAPException
     * @throws LDAPSynchronizationException
     * @throws LDAPSearchException
     */
    private UUID handleNewGroup ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPSearchException, LDAPException {

        if ( handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), groupEntry.getParsedDN()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("ignoring group " + groupEntry); //$NON-NLS-1$
            }
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("New group for " + groupEntry); //$NON-NLS-1$
        }

        String uuidAttr = handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID);

        UUID uuid = null;

        if ( groupEntry.hasAttribute(uuidAttr) ) {
            uuid = extractUUID(handler, groupEntry, uuidAttr);
        }

        UUID groupId = handler.createGroup(groupEntry.getDN(), uuid, handler.getConfig().getGroupMapper().mapObject(groupEntry));
        synchronizeGroupMemberships(groupId, groupEntry, handler, handledUserRefs, handledGroupRefs, cl);
        return groupId;
    }


    /**
     * @param groupId
     * @param groupEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param cl
     * @throws LDAPSynchronizationException
     * @throws LDAPSearchException
     * @throws LDAPException
     */
    private void synchronizeGroupMemberships ( UUID groupId, SearchResultEntry groupEntry, LDAPSynchronizationHandler handler,
            Set<String> handledUserRefs, Set<String> handledGroupRefs, LDAPClient cl )
                    throws LDAPSynchronizationException, LDAPSearchException, LDAPException {
        if ( handler.getConfig().isRecursiveResolveGroups() && handler.getConfig().isUseForwardGroups() ) {
            log.trace("Resolving forward nested group memberships"); //$NON-NLS-1$
            Set<UUID> newForwardNestedGroups = getForwardNestedGroups(groupEntry, handler, handledUserRefs, handledGroupRefs, cl);
            if ( log.isTraceEnabled() ) {
                log.trace("Found nested group membership in " + newForwardNestedGroups); //$NON-NLS-1$
            }

            handler.setForwardNestedGroups(groupId, newForwardNestedGroups);
        }
        else if ( !handler.getConfig().isUseForwardGroups() ) {
            log.trace("Resolving group memberships"); //$NON-NLS-1$

            Set<UUID> newNestedGroups = getNestedGroups(groupEntry, handler, handledUserRefs, handledGroupRefs, cl);
            Set<UUID> newMemberUsers = getGroupMemberUsers(groupEntry, handler, handledUserRefs, handledGroupRefs, cl);

            if ( log.isDebugEnabled() ) {
                log.debug("Found nested groups " + newNestedGroups); //$NON-NLS-1$
            }

            handler.setNestedGroups(groupId, newNestedGroups);

            if ( log.isTraceEnabled() ) {
                log.trace("Found member users " + newMemberUsers); //$NON-NLS-1$
            }

            handler.setMembers(groupId, newMemberUsers);
        }
        else {
            log.debug("No membership attribute found"); //$NON-NLS-1$
        }
    }


    /**
     * @param groupEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param cl
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     */
    private Set<UUID> getGroupMemberUsers ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPException {
        log.trace("Get group member users"); //$NON-NLS-1$
        Set<UUID> memberOfIds = new HashSet<>();
        String memberAttr = handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER);
        if ( !groupEntry.hasAttribute(memberAttr) ) {
            return memberOfIds;
        }

        for ( String memberDn : groupEntry.getAttributeValues(memberAttr) ) {
            if ( isValidUser(handler, memberDn, cl) && isValidGroup(handler, memberDn, cl) ) {
                // need to check what is really is
                if ( !checkReallyUser(handler, memberDn, cl) ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace("Not really a user " + memberDn); //$NON-NLS-1$
                    }
                    continue;
                }
            }

            int errors = 0;
            if ( isValidUser(handler, memberDn, cl) ) {
                UUID uid = getOrCreateUser(handler, handledUserRefs, handledGroupRefs, memberDn, cl);
                if ( uid != null ) {
                    memberOfIds.add(uid);
                }
                else {
                    errors++;
                }
            }
            else if ( log.isTraceEnabled() ) {
                log.trace("Not a valid user " + memberDn); //$NON-NLS-1$
            }

            if ( errors > 0 ) {
                log.warn(
                    String.format(
                        "%d group member(s) could not be resolved in %s. Full synchronization (service restart) might be necessary to recover them after fixing the issue", //$NON-NLS-1$
                        errors,
                        groupEntry.getDN()));
            }
        }
        return memberOfIds;
    }


    /**
     * @param userRef
     * @param cl
     * @return
     * @throws LDAPSearchException
     * @throws LDAPException
     */
    private static boolean checkReallyUser ( LDAPSynchronizationHandler handler, String userRef, LDAPClient cl )
            throws LDAPSearchException, LDAPException {
        SearchResultEntry search = getUserEntryByRef(handler, userRef, cl);
        return search != null && search.getDN() != null;
    }


    /**
     * @param groupRef
     * @param cl
     * @return
     * @throws LDAPSearchException
     * @throws LDAPException
     */
    private static boolean checkReallyGroup ( LDAPSynchronizationHandler handler, String groupRef, LDAPClient cl )
            throws LDAPSearchException, LDAPException {
        SearchResultEntry search = getGroupEntryByRef(handler, groupRef, cl);
        return search != null && search.getDN() != null;
    }


    /**
     * @param groupEntry
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param cl2
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private Set<UUID> getNestedGroups ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPSearchException, LDAPException {
        Set<UUID> memberIds = new HashSet<>();
        String groupMemberAttr = handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER);
        if ( !groupEntry.hasAttribute(groupMemberAttr) ) {
            return memberIds;
        }

        for ( String memberOfDn : groupEntry.getAttributeValues(groupMemberAttr) ) {
            // need to check what is really is
            boolean validGroup = isValidGroup(handler, memberOfDn, cl);
            if ( isValidUser(handler, memberOfDn, cl) && validGroup && !checkReallyGroup(handler, memberOfDn, cl) ) {
                continue;
            }

            if ( validGroup ) {
                memberIds.add(getOrCreateGroup(handler, handledUserRefs, handledGroupRefs, memberOfDn, cl));
            }
        }
        return memberIds;
    }


    /**
     * @param groupEntry
     * @param handler
     * @param handledUserRefs
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private Set<UUID> getForwardNestedGroups ( SearchResultEntry groupEntry, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient cl ) throws LDAPSynchronizationException, LDAPSearchException, LDAPException {
        Set<UUID> memberOfIds = new HashSet<>();
        for ( String memberOfDn : groupEntry.getAttributeValues(handler.getConfig().getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER_OF)) ) {
            if ( isValidGroup(handler, memberOfDn, cl) ) {
                memberOfIds.add(getOrCreateGroup(handler, handledUserRefs, handledGroupRefs, memberOfDn, cl));
            }
        }
        return memberOfIds;
    }


    /**
     * @param memberOfDn
     * @return
     * @throws LDAPSynchronizationException
     */
    private static boolean isValidGroup ( LDAPSynchronizationHandler handler, String memberOfDn, LDAPClient cl ) throws LDAPSynchronizationException {
        if ( !handler.getConfig().isReferencesAreDNs() ) {
            return true;
        }

        try {
            if ( !matchDNWithScope(
                memberOfDn,
                cl.relativeDN(handler.getConfig().getGroupConfig().getBaseDN()),
                handler.getConfig().getGroupConfig().getScope()) ) {
                return false;
            }

            if ( handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), new DN(memberOfDn)) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("ignoring group " + memberOfDn); //$NON-NLS-1$
                }
                return false;
            }

            return true;
        }
        catch ( LDAPException e ) {
            log.warn("Failed to parse DN", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param memberOfDn
     * @param handler
     * @return
     * @throws LDAPSynchronizationException
     */
    private static boolean isValidUser ( LDAPSynchronizationHandler handler, String memberOfDn, LDAPClient cl ) throws LDAPSynchronizationException {
        if ( !handler.getConfig().isReferencesAreDNs() ) {
            return true;
        }
        try {
            if ( !matchDNWithScope(
                memberOfDn,
                cl.relativeDN(handler.getConfig().getUserConfig().getBaseDN()),
                handler.getConfig().getUserConfig().getScope()) ) {
                return false;
            }

            if ( handler.getConfig().getStyle().shouldExclude(cl.getBaseDN(), new DN(memberOfDn)) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("ignoring user " + memberOfDn); //$NON-NLS-1$
                }
                return false;
            }

            return true;
        }
        catch ( LDAPException e ) {
            log.warn("Failed to parse DN", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param memberOfDn
     * @param groupSubtree
     * @param groupSearchScope
     * @return
     * @throws LDAPException
     * @throws LDAPSynchronizationException
     */
    private static boolean matchDNWithScope ( String memberOfDn, DN baseDN, SearchScope scope ) throws LDAPException, LDAPSynchronizationException {
        DN toMatch = new DN(memberOfDn);
        switch ( scope.intValue() ) {
        case SearchScope.ONE_INT_VALUE:
            return toMatch.getParent().equals(baseDN);
        case SearchScope.SUB_INT_VALUE:
            return toMatch.isDescendantOf(baseDN, true);
        default:
            throw new LDAPSynchronizationException("Unsupported search scope"); //$NON-NLS-1$
        }
    }


    /**
     * @param handler
     * @param handledUserRefs
     * @param cl
     * @param userDn
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     */
    private UUID getOrCreateUser ( LDAPSynchronizationHandler handler, Set<String> handledUserRefs, Set<String> handledGroupRefs, String ref,
            LDAPClient cl ) throws LDAPSynchronizationException, LDAPException {
        UUID userByDN = null;
        if ( handledUserRefs.contains(ref) ) {
            userByDN = locateUserIDByRef(handler, ref, cl);
        }
        else {
            userByDN = handleUserResult(getUserEntryByRef(handler, ref, cl), handler, handledUserRefs, handledGroupRefs, cl);
            if ( userByDN == null ) {
                // this can only happen if we would have created a user
                return null;
            }
        }

        if ( userByDN == null ) {
            throw new LDAPSynchronizationException("Failed to locate already synchronized user"); //$NON-NLS-1$
        }
        return userByDN;
    }


    /**
     * @param handler
     * @param handledUserRefs
     * @param ref
     * @param cl
     * @return
     * @throws LDAPSynchronizationException
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private UUID getOrCreateGroup ( LDAPSynchronizationHandler handler, Set<String> handledUserRefs, Set<String> handledGroupsRefs, String ref,
            LDAPClient cl ) throws LDAPSynchronizationException, LDAPSearchException, LDAPException {
        UUID groupByDN = null;
        if ( handledGroupsRefs.contains(ref) ) {
            groupByDN = locateGroupIDByRef(handler, ref, cl);
        }
        else {
            groupByDN = handleGroupResult(getGroupEntryByRef(handler, ref, cl), handler, handledUserRefs, handledGroupsRefs, cl);
        }

        if ( groupByDN == null ) {
            throw new LDAPSynchronizationException("Failed to locate already synchronized group " + ref); //$NON-NLS-1$
        }
        return groupByDN;
    }


    /**
     * @return
     */
    private static Filter makeLastModifiedFilter ( LDAPSynchronizationHandler handler ) {
        String modifyTimestampAttr = handler.getConfig().getOperationalMapper().getAttributeName(LDAPOperationalAttrs.MODIFY_TIMESTAMP);
        if ( handler.getLastRun() == null ) {
            return Filter.createPresenceFilter(modifyTimestampAttr);
        }
        DateTime utcLastRun = handler.getLastRun().toDateTime(DateTimeZone.UTC);
        return Filter.createGreaterOrEqualFilter(modifyTimestampAttr, utcLastRun.toString(RFC4517_TIME_FORMAT));
    }

}
