/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.UnknownAccountException;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;

import eu.agno3.runtime.ldap.client.AttributeMatchStyle;
import eu.agno3.runtime.ldap.client.LDAPAttributeMatcher;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.security.ldap.LDAPGroupAttrs;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;


/**
 * @author mbechler
 *
 */
public class LDAPGroupResolverUtil {

    private static final Logger log = Logger.getLogger(LDAPGroupResolverUtil.class);


    /**
     * 
     * @param config
     * @param cl
     * @param userDn
     * @param userName
     * @param userEntry
     * @return resolved roles
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    static Set<String> resolveUserRoles ( LDAPRealmConfig config, LDAPClient cl, String userDn, String userName, SearchResultEntry userEntry )
            throws LDAPSearchException, LDAPException {
        return resolveUserRoles(config, cl, userDn, userName, userEntry, false);
    }


    /**
     * @param cl
     * @param userDn
     * @param userName
     * @param userEntry
     * @param doFilter
     *            whether to apply the config's filter and base dn to the group search
     * @return resolved roles
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    static Set<String> resolveUserRoles ( LDAPRealmConfig config, LDAPClient cl, String userDn, String userName, SearchResultEntry userEntry,
            boolean doFilter ) throws LDAPException, LDAPSearchException {
        Queue<String> toResolve = new LinkedBlockingQueue<>();
        if ( config.isReferencesAreDNs() ) {
            toResolve.add(userDn);
        }
        else {
            toResolve.add(userName);
        }

        Set<String> resolved = new HashSet<>();
        Set<SearchResultEntry> foundGroups = new HashSet<>();
        Set<String> foundRoles = new HashSet<>();
        Set<String> roles = new HashSet<>(config.getAlwaysAddRoles());

        roles.addAll(mapUserToRoles(config, cl, userDn, userEntry));

        String baseDn = doFilter ? cl.relativeDN(config.getGroupConfig().getBaseDN()).toString() : cl.getBaseDN().toString();
        LDAPGroupResolverUtil.addDirectMemberships(config, userDn, foundGroups, toResolve, foundRoles, cl);
        LDAPGroupResolverUtil.resolveGroups(config, foundGroups, resolved, toResolve, foundRoles, cl, baseDn, doFilter);
        roles.addAll(LDAPGroupResolverUtil.mapGroupsToRoles(config, foundGroups));
        roles.addAll(foundRoles);
        if ( log.isDebugEnabled() ) {
            log.debug("Resolved group DNs:  " + foundGroups); //$NON-NLS-1$
            log.debug("Resolved direct roles: " + foundRoles); //$NON-NLS-1$
            log.debug("Complete roles: " + roles); //$NON-NLS-1$
        }

        return roles;
    }


    /**
     * @param foundGroups
     * @return
     * @throws LDAPException
     */
    static Collection<String> mapGroupsToRoles ( LDAPRealmConfig config, Set<SearchResultEntry> foundGroups ) throws LDAPException {
        Set<String> roles = new HashSet<>();
        for ( SearchResultEntry groupEntry : foundGroups ) {
            roles.addAll(mapGroupToRoles(config, groupEntry));
        }
        return roles;
    }


    /**
     * @param groupEntry
     * @return
     * @throws LDAPException
     */
    private static Collection<? extends String> mapGroupToRoles ( LDAPRealmConfig config, SearchResultEntry groupEntry ) throws LDAPException {
        Set<String> roles = new HashSet<>();

        String normalizedDN = groupEntry.getParsedDN().toNormalizedString();
        if ( log.isDebugEnabled() ) {
            log.debug("Checking for matches for " + normalizedDN); //$NON-NLS-1$
        }
        Set<String> staticMappedRoles = config.getStaticRoleMappings().get(normalizedDN);
        if ( staticMappedRoles != null ) {
            roles.addAll(staticMappedRoles);
        }

        for ( Entry<Pattern, Set<String>> e : config.getPatternRoleMappings().entrySet() ) {
            if ( e.getKey().matcher(normalizedDN).matches() ) {
                roles.addAll(e.getValue());
            }
        }

        if ( config.isAddGroupNameAsRole() ) {
            roles.add(groupEntry.getAttributeValue(config.getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME)));
        }

        addAttributeMapRoles(config, normalizedDN, roles, groupEntry);
        return roles;
    }


    private static Collection<? extends String> mapUserToRoles ( LDAPRealmConfig config, LDAPClient cl, String userDn, SearchResultEntry userEntry )
            throws LDAPException {
        Set<String> roles = new HashSet<>();

        if ( log.isDebugEnabled() ) {
            log.debug("Checking for matches for " + userDn); //$NON-NLS-1$
        }
        Set<String> staticMappedRoles = config.getStaticRoleMappings().get(userDn);
        if ( staticMappedRoles != null ) {
            roles.addAll(staticMappedRoles);
        }

        for ( Entry<Pattern, Set<String>> e : config.getPatternRoleMappings().entrySet() ) {
            if ( e.getKey().matcher(userDn).matches() ) {
                roles.addAll(e.getValue());
            }
        }

        if ( config.getRoleMapAttributes() != null ) {
            SearchResultEntry e = userEntry;
            if ( e == null ) {
                e = cl.getEntry(userDn, config.getRoleMapAttributes().keySet().toArray(new String[0]));
            }
            addAttributeMapRoles(config, userDn, roles, e);
        }

        return roles;
    }


    /**
     * @param config
     * @param dn
     * @param roles
     * @param e
     */
    private static void addAttributeMapRoles ( LDAPRealmConfig config, String dn, Set<String> roles, SearchResultEntry e ) {

        for ( Entry<String, AttributeMatchStyle> matcher : config.getRoleMapAttributes().entrySet() ) {
            String attr = matcher.getKey();
            if ( !StringUtils.isBlank(attr) && e.hasAttribute(attr) ) {
                Attribute val = e.getAttribute(attr);

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Found role map attribute value %s for %s", val, dn)); //$NON-NLS-1$
                }

                for ( Entry<String, Set<String>> map : config.getAttributeRoleMappings().entrySet() ) {
                    if ( LDAPAttributeMatcher.matchAttribute(matcher.getValue(), val, map.getKey(), e) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format("Matched attribute value %s, adding roles %s", map.getKey(), map.getValue())); //$NON-NLS-1$
                        }
                        roles.addAll(map.getValue());
                    }
                }
            }
        }

    }


    /**
     * @param ldapPrinc
     * @param foundDNs
     * @param toResolve
     * @param foundRoles
     * @param cl
     * @throws LDAPException
     */
    static void addDirectMemberships ( LDAPRealmConfig config, String userDn, Set<SearchResultEntry> foundDNs, Queue<String> toResolve,
            Set<String> foundRoles, LDAPClient cl ) throws LDAPException {

        SearchResultEntry userRes = cl.getEntry(userDn);
        if ( userRes == null ) {
            throw new UnknownAccountException();
        }

        String roleAttr = config.getUserMapper().getAttributeName(LDAPUserAttrs.ROLE);
        if ( !StringUtils.isBlank(roleAttr) && userRes.hasAttribute(roleAttr) ) {
            foundRoles.addAll(Arrays.asList(userRes.getAttributeValues(roleAttr)));
        }

        String memberOfAttr = config.getUserMapper().getAttributeName(LDAPUserAttrs.MEMBER_OF);
        if ( config.isUseForwardGroups() && !StringUtils.isBlank(memberOfAttr) && userRes.hasAttribute(memberOfAttr) ) {
            addDirectMembership(config, foundDNs, toResolve, foundRoles, cl, userRes);
        }
    }


    /**
     * @param foundDNs
     * @param toResolve
     * @param cl
     * @param res
     * @throws LDAPException
     */
    private static void addDirectMembership ( LDAPRealmConfig config, Set<SearchResultEntry> foundDNs, Queue<String> toResolve,
            Set<String> foundRoles, LDAPClient cl, SearchResultEntry res ) throws LDAPException {
        if ( !config.isUseForwardGroups() ) {
            return;
        }
        for ( String membership : res.getAttributeValues(config.getUserMapper().getAttributeName(LDAPUserAttrs.MEMBER_OF)) ) {
            SearchResultEntry entry = cl.getEntry(membership, config.getGroupAttrs());
            addGroup(config, foundDNs, toResolve, foundRoles, entry, cl);
        }
    }


    /**
     * @param foundDNs
     * @param resolved
     * @param toResolve
     * @param cl
     * @param baseDn
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    static void resolveGroups ( LDAPRealmConfig config, Set<SearchResultEntry> foundDNs, Set<String> resolved, Queue<String> toResolve,
            Set<String> foundRoles, LDAPClient cl, String baseDn, boolean doFilter ) throws LDAPException, LDAPSearchException {
        while ( !toResolve.isEmpty() ) {
            String member = toResolve.poll();
            if ( resolved.contains(member) ) {
                continue;
            }
            resolved.add(member);

            if ( log.isDebugEnabled() ) {
                log.debug("Resolving groups for " + member); //$NON-NLS-1$
            }

            if ( config.isUseForwardGroups() ) {
                SearchResultEntry entry = cl.getEntry(member, config.getGroupAttrs());
                if ( entry != null ) {
                    addGroup(config, foundDNs, toResolve, foundRoles, entry, cl);
                }
            }
            else {
                Filter groupFilter;
                if ( doFilter ) {
                    groupFilter = Filter.createANDFilter(
                        config.getStyle().createGroupFilter(),
                        config.getGroupConfig().getFilter(),
                        Filter.createEqualityFilter(config.getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER), member));
                }
                else {
                    groupFilter = Filter.createANDFilter(
                        config.getStyle().createGroupFilter(),
                        Filter.createEqualityFilter(config.getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER), member));
                }

                SearchRequest req = new SearchRequest(baseDn, config.getGroupConfig().getScope(), groupFilter.toString(), config.getGroupAttrs());
                SearchResult groupRes = cl.search(req);

                if ( groupRes == null || groupRes.getEntryCount() == 0 ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace("No groups found for " + req); //$NON-NLS-1$
                    }
                    continue;
                }

                for ( SearchResultEntry memberships : groupRes.getSearchEntries() ) {
                    addGroup(config, foundDNs, toResolve, foundRoles, memberships, cl);
                }
            }
        }
    }


    /**
     * @param foundDNs
     * @param toResolve
     * @param groupResult
     * @throws LDAPException
     */
    private static void addGroup ( LDAPRealmConfig config, Set<SearchResultEntry> foundDNs, Queue<String> toResolve, Set<String> foundRoles,
            SearchResultEntry groupResult, LDAPClient cl ) throws LDAPException {
        if ( groupResult == null ) {
            return;
        }

        String groupName = groupResult.getAttributeValue(config.getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME));
        String groupDn = groupResult.getDN();

        String roleAttr = config.getGroupMapper().getAttributeName(LDAPGroupAttrs.ROLE);
        if ( !StringUtils.isBlank(roleAttr) && groupResult.hasAttribute(roleAttr) ) {
            foundRoles.addAll(Arrays.asList(groupResult.getAttributeValues(roleAttr)));
        }

        foundDNs.add(groupResult);

        String memberOfAttr = config.getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER_OF);
        if ( config.isUseForwardGroups() && config.isRecursiveResolveGroups() && !StringUtils.isBlank(memberOfAttr) ) {
            if ( groupResult.hasAttribute(memberOfAttr) ) {
                addDirectMembership(config, foundDNs, toResolve, foundRoles, cl, groupResult);
            }
        }

        if ( config.isRecursiveResolveGroups() ) {
            toResolve.add(config.isReferencesAreDNs() ? groupDn : groupName);
        }
    }

}
