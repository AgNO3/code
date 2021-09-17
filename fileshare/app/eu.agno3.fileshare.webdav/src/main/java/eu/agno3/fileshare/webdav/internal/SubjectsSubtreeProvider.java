/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.security.Principal;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.webdav.sync.SyncTokenData;
import eu.agno3.runtime.validation.email.EMailValidator;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport.SearchArgument;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 *
 */
public class SubjectsSubtreeProvider implements SubtreeProvider {

    private static final Logger log = Logger.getLogger(SubjectsSubtreeProvider.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#isApplicable(java.lang.String,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public boolean isApplicable ( String repositoryPath, DAVLayout layout ) {
        return layout != DAVLayout.OWNCLOUD && repositoryPath.startsWith(SubjectsDAVNode.SUBJECTS_PATH); // $NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#resolve(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      java.lang.String, eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public DAVTreeNode<EntityKey> resolve ( FileshareDAVTreeProviderInternal dt, String repositoryPath, DAVLayout layout )
            throws FileshareException, DavException {
        if ( !repositoryPath.startsWith(SubjectsDAVNode.SUBJECTS_PATH) ) {
            return null;
        }

        if ( SubjectsDAVNode.SUBJECTS_PATH.equals(repositoryPath) ) {
            return new SubjectsDAVNode(layout);
        }

        int nextSep = repositoryPath.indexOf('/', SubjectsDAVNode.SUBJECTS_PATH.length() + 1);

        String type;
        String relative;
        if ( nextSep >= 0 ) {
            type = repositoryPath.substring(SubjectsDAVNode.SUBJECTS_PATH.length() + 1, nextSep);
            relative = repositoryPath.substring(nextSep + 1);
        }
        else {
            type = repositoryPath.substring(SubjectsDAVNode.SUBJECTS_PATH.length() + 1);
            relative = null;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Type is " + type); //$NON-NLS-1$
        }
        switch ( type ) {
        case "users": //$NON-NLS-1$
            return resolveUser(dt, relative, layout);
        case "groups": //$NON-NLS-1$
            return resolveGroup(dt, relative, layout);
        case "tokens": //$NON-NLS-1$
            return resolveToken(dt, relative, layout);
        case "mail": //$NON-NLS-1$
            return resolveMail(dt, relative, layout);

        }

        return null;
    }


    /**
     * @param dt
     * @param relative
     * @param layout
     * @return
     */
    private static DAVTreeNode<EntityKey> resolveMail ( FileshareDAVTreeProviderInternal dt, String relative, DAVLayout layout ) {
        if ( StringUtils.isBlank(relative) ) {
            return new MailSubjectsDAVNode(layout);
        }

        String decoded;
        try {
            decoded = URLDecoder.decode(relative, "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            throw new AssertionError(e);
        }

        if ( EMailValidator.checkEMailValid(decoded, true, false, false, false) ) {
            return new MailSubjectDAVNode(
                decoded,
                dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getMailTrustLevel(decoded),
                layout);
        }

        return null;
    }


    /**
     * @param dt
     * @param relative
     * @param layout
     * @return
     * @throws FileshareException
     */
    private static DAVTreeNode<EntityKey> resolveToken ( FileshareDAVTreeProviderInternal dt, String relative, DAVLayout layout )
            throws FileshareException {
        if ( StringUtils.isBlank(relative) ) {
            return new TokenSubjectsDAVNode(layout);
        }
        UUID grantId;
        try {
            grantId = UUID.fromString(relative);
        }
        catch ( IllegalArgumentException e ) {
            // non-existant token URIs can be always used in creation
            return new RemovedDAVNode(new TokenSubjectsDAVNode(layout), relative, false, layout);
        }

        Grant g = dt.getContext().getShareService().getGrantUnchecked(grantId);
        if ( ! ( g instanceof TokenGrant ) ) {
            return null;
        }

        if ( g instanceof MailGrant ) {
            String mailAddress = ( (MailGrant) g ).getMailAddress();
            return new MailSubjectDAVNode(

                (MailGrant) g,
                dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getMailTrustLevel(mailAddress),
                layout);
        }

        return new TokenSubjectDAVNode(
            (TokenGrant) g,
            dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getLinkTrustLevel(),
            layout);
    }


    /**
     * @param dt
     * @param relative
     * @param layout
     * @return
     * @throws FileshareException
     */
    private static DAVTreeNode<EntityKey> resolveUser ( FileshareDAVTreeProviderInternal dt, String relative, DAVLayout layout )
            throws FileshareException {
        if ( StringUtils.isBlank(relative) ) {
            return new UserSubjectsDAVNode(layout);
        }
        UUID userId;
        try {
            userId = UUID.fromString(relative);
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Failed to parse user id", e); //$NON-NLS-1$
            return null;
        }

        SubjectQueryResult subjectInfo = dt.getContext().getSubjectService().getSubjectInfo(userId);
        if ( ! ( subjectInfo instanceof UserInfo ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Is not a user " + subjectInfo); //$NON-NLS-1$
            }
            return null;
        }

        UserDetails userDetails = null;
        try {
            userDetails = dt.getContext().getUserService().getUserDetails(userId);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            log.debug("Failed to get user details for " + userId, e); //$NON-NLS-1$
        }

        return new UserSubjectDAVNode(
            (UserInfo) subjectInfo,
            userDetails,
            dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(subjectInfo.getTrustLevel()),
            layout);
    }


    /**
     * @param dt
     * @param relative
     * @param layout
     * @return
     * @throws FileshareException
     */
    private static DAVTreeNode<EntityKey> resolveGroup ( FileshareDAVTreeProviderInternal dt, String relative, DAVLayout layout )
            throws FileshareException {
        if ( StringUtils.isBlank(relative) ) {
            return new GroupSubjectsDAVNode(layout);
        }
        UUID groupId;
        try {
            groupId = UUID.fromString(relative);
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Failed to parse user id", e); //$NON-NLS-1$
            return null;
        }

        SubjectQueryResult subjectInfo = dt.getContext().getSubjectService().getSubjectInfo(groupId);
        if ( ! ( subjectInfo instanceof GroupInfo ) ) {
            return null;
        }

        return new GroupSubjectDAVNode(
            (GroupInfo) subjectInfo,
            dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(subjectInfo.getTrustLevel()),
            layout);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#handlesChildren(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public boolean handlesChildren ( DAVTreeNode<EntityKey> node, DAVLayout layout ) {
        return node instanceof SubjectsDAVNode || node instanceof SubjectDAVNode || node instanceof SubjectBaseNode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#getChildren(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public Collection<DAVTreeNode<EntityKey>> getChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException {
        if ( node instanceof SubjectDAVNode ) {
            // subjects don't have children
            return null;
        }

        if ( node instanceof SubjectsDAVNode ) {
            return Arrays.asList(
                new UserSubjectsDAVNode(layout),
                new GroupSubjectsDAVNode(layout),
                new MailSubjectsDAVNode(layout),
                new TokenSubjectsDAVNode(layout));
        }
        else if ( node instanceof UserSubjectsDAVNode ) {
            User currentUser = dt.getAccessControl().getCurrentUser();
            return Arrays.asList(
                new UserSubjectDAVNode(
                    currentUser,
                    currentUser.getUserDetails(),
                    dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(currentUser),
                    layout));
        }
        else if ( node instanceof GroupSubjectsDAVNode ) {
            Collection<DAVTreeNode<EntityKey>> userGroups = new ArrayList<>();
            for ( Group g : dt.getAccessControl().getCurrentUserGroupClosure() ) {
                userGroups.add(
                    new GroupSubjectDAVNode(g, dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(g), layout));
            }
            return userGroups;
        }

        // we don't allow enumeration for the individual types

        return Collections.EMPTY_LIST;
    }


    @Override
    public Map<DAVTreeNode<EntityKey>, Status> findPrincipalMatch ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped,
            DAVLayout layout, PrincipalMatchReport pm ) throws UserNotFoundException, AuthenticationException {
        Map<DAVTreeNode<EntityKey>, Status> res = new HashMap<>();

        if ( !SecurityConstants.PRINCIPAL_URL.equals(pm.getPrincipalPropertyName()) ) {
            return res;
        }

        if ( wrapped instanceof SubjectsDAVNode || wrapped instanceof UserSubjectsDAVNode ) {
            User currentUser = dt.getAccessControl().getCurrentUser();
            res.put(
                new UserSubjectDAVNode(
                    currentUser,
                    currentUser.getUserDetails(),
                    dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(currentUser),
                    layout),
                new Status(200));
        }

        if ( wrapped instanceof SubjectsDAVNode || wrapped instanceof GroupSubjectsDAVNode ) {
            for ( Group g : dt.getAccessControl().getCurrentUserGroupClosure() ) {
                res.put(
                    new GroupSubjectDAVNode(g, dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(g), layout),
                    new Status(200));
            }
        }
        return res;
    }


    @Override
    public Map<DAVTreeNode<EntityKey>, Status> searchPrincipal ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped,
            DAVLayout layout, FixedPrincipalSearchReport ps ) throws FileshareException {

        boolean includeGroups = false;
        boolean includeUsers = false;
        if ( ps.getSearchRoots().length != 1 || wrapped instanceof SubjectsDAVNode || wrapped instanceof EntityDAVNode ) {
            // this either a search on the subjects root or one specifying apply-to-principal-collection-set
            includeGroups = true;
            includeUsers = true;
        }
        else if ( wrapped instanceof UserSubjectsDAVNode ) {
            includeUsers = true;
        }
        else if ( wrapped instanceof GroupSubjectDAVNode ) {
            includeGroups = true;
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Not searching principals on " + wrapped); //$NON-NLS-1$
            }
            return Collections.EMPTY_MAP;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Search arguments are " + Arrays.toString(ps.getSearchArguments())); //$NON-NLS-1$
        }
        Map<DAVTreeNode<EntityKey>, Status> res = new HashMap<>();
        for ( SearchArgument s : ps.getSearchArguments() ) {
            DavPropertyNameSet searchProperties = s.getSearchProperties();
            String searchArg = s.getSearchString();
            if ( searchArg == null ) {
                searchArg = StringUtils.EMPTY;
            }
            if ( searchProperties.contains(DavPropertyName.DISPLAYNAME) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Searching for '%s' in %s", searchArg, searchProperties)); //$NON-NLS-1$
                }
                for ( SubjectQueryResult sqi : dt.getContext().getSubjectService().querySubjects(searchArg, -1) ) {
                    if ( includeGroups && sqi instanceof GroupInfo ) {
                        res.put(
                            new GroupSubjectDAVNode(
                                (GroupInfo) sqi,
                                dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(sqi.getTrustLevel()),
                                layout),
                            new Status(200));
                    }
                    else if ( includeUsers && sqi instanceof UserInfo ) {
                        UserDetails details = null;
                        try {
                            details = dt.getContext().getUserService().getUserDetails(sqi.getId());
                        }
                        catch (
                            FileshareException |
                            UndeclaredThrowableException e ) {
                            log.debug("Failed to get user details for " + sqi.getId(), e); //$NON-NLS-1$
                        }
                        res.put(
                            new UserSubjectDAVNode(
                                (UserInfo) sqi,
                                details,
                                dt.getContext().getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(sqi.getTrustLevel()),
                                layout),
                            new Status(200));
                    }
                }
                break;
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Skipping search for '%s' in %s", searchArg, searchProperties)); //$NON-NLS-1$
            }
        }
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#getRootChildren(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public Collection<? extends DAVTreeNode<EntityKey>> getRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout )
            throws FileshareException {
        return Collections.EMPTY_LIST;
    }


    @Override
    public boolean contributeChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, boolean rootModified ) throws FileshareException {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#contributePath(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout,
     *      java.lang.StringBuilder)
     */
    @Override
    public boolean contributePath ( FileshareDAVTreeProviderInternal fileshareDAVTreeProviderImpl, DAVTreeNode<EntityKey> node, DAVLayout l,
            StringBuilder path ) throws FileshareException {
        return false;
    }


    /**
     * @param vc
     * @param princNode
     * @param existingGrants
     * @return
     * @throws UnsupportedEncodingException
     */
    static Grant principalToGrant ( VFSContext vc, DAVTreeNode<EntityKey> princNode, Set<Grant> existingGrants ) throws UnsupportedEncodingException {
        for ( Grant g : existingGrants ) {
            if ( princNode instanceof UserSubjectDAVNode && g instanceof SubjectGrant ) {
                SubjectGrant sg = (SubjectGrant) g;
                UserSubjectDAVNode udn = (UserSubjectDAVNode) princNode;
                if ( sg.getTarget() instanceof User && sg.getTarget().getId().equals(udn.getUserInfo().getId()) ) {
                    return sg;
                }
            }
            else if ( princNode instanceof GroupSubjectDAVNode && g instanceof SubjectGrant ) {
                SubjectGrant sg = (SubjectGrant) g;
                GroupSubjectDAVNode gdn = (GroupSubjectDAVNode) princNode;
                if ( sg.getTarget() instanceof Group && sg.getTarget().getId().equals(gdn.getGroupInfo().getId()) ) {
                    return sg;
                }
            }
            else if ( princNode instanceof MailSubjectDAVNode && g instanceof MailGrant ) {
                MailGrant mg = (MailGrant) g;
                if ( mg.getMailAddress().equals( ( (MailSubjectDAVNode) princNode ).getMailAddress()) ) {
                    return mg;
                }
            }
            else if ( princNode instanceof TokenSubjectDAVNode && g instanceof TokenGrant ) {
                if ( g.equals( ( (TokenSubjectDAVNode) princNode ).getGrant()) ) {
                    return g;
                }
            }
        }

        return null;
    }


    /**
     * @param g
     * @return
     */
    static Principal grantToPrincipal ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            Subject s = ( (SubjectGrant) g ).getTarget();
            if ( s instanceof User ) {
                return Principal.getHrefPrincipal(getUserUrl((UserInfo) s));
            }
            else if ( s instanceof Group ) {
                return Principal.getHrefPrincipal(getGroupUrl((GroupInfo) s));
            }
        }
        else if ( g instanceof MailGrant ) {
            return Principal.getHrefPrincipal(getTokenUrl(g.getId()));
        }
        else if ( g instanceof TokenGrant ) {
            return Principal.getHrefPrincipal(getTokenUrl(g.getId()));
        }
        return null;
    }


    /**
     * @param id
     * @return url for the token principal
     */
    public static String getTokenUrl ( UUID id ) {
        return TokenSubjectsDAVNode.SUBJECTS_TOKENS + '/' + id;
    }


    /**
     * 
     * @param g
     * @return url of the group principal
     */
    public static String getGroupUrl ( GroupInfo g ) {
        return GroupSubjectsDAVNode.SUBJECTS_GROUPS + '/' + g.getId();
    }


    /**
     * 
     * @param u
     * @return url of the user principal
     */
    public static String getUserUrl ( UserInfo u ) {
        return UserSubjectsDAVNode.SUBJECTS_USERS + '/' + u.getId();
    }


    /**
     * @param subject
     * @return subject url
     */
    public static String getSubjectUrl ( SubjectInfo subject ) {
        if ( subject instanceof UserInfo ) {
            return getUserUrl((UserInfo) subject);
        }
        else if ( subject instanceof GroupInfo ) {
            return getGroupUrl((GroupInfo) subject);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Unhandled subject type " + subject.getClass().getName()); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param g
     * @return url for grant
     */
    public static String getGrantUrl ( Grant g ) {
        return getTokenUrl(g.getId());
    }

}
