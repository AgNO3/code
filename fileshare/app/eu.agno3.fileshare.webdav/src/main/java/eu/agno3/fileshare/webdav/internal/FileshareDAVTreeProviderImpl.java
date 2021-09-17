/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.InputContextImpl;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.search.QueryGrammerSet;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.security.AclRestrictionsProperty;
import org.apache.jackrabbit.webdav.security.CurrentUserPrivilegeSetProperty;
import org.apache.jackrabbit.webdav.security.Principal;
import org.apache.jackrabbit.webdav.security.Privilege;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.security.SupportedPrivilege;
import org.apache.jackrabbit.webdav.security.SupportedPrivilegeSetProperty;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.CannotShareToSelfException;
import eu.agno3.fileshare.exceptions.ContentVirusException;
import eu.agno3.fileshare.exceptions.EntityExpirationInvalidException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantExistsException;
import eu.agno3.fileshare.exceptions.GroupNotFoundException;
import eu.agno3.fileshare.exceptions.InconsistentSecurityLabelException;
import eu.agno3.fileshare.exceptions.InvalidSyncTokenException;
import eu.agno3.fileshare.exceptions.MailingDisabledException;
import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.exceptions.UploadException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.ShareProperties;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TokenShare;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.DirectoryService;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.service.api.internal.BrowseServiceInternal;
import eu.agno3.fileshare.service.api.internal.DeliveryService;
import eu.agno3.fileshare.service.api.internal.EntityServiceInternal;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.ShareServiceInternal;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.gui.GuiServiceContext;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.webdav.FileshareDAVTreeProvider;
import eu.agno3.fileshare.webdav.sync.SyncTokenData;
import eu.agno3.runtime.security.password.PasswordGenerationException;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.ExtendedStatus;
import eu.agno3.runtime.webdav.server.PathEscapeUtil;
import eu.agno3.runtime.webdav.server.ResolvedHrefProperty;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.acl.PrincipalSearchProperty;
import eu.agno3.runtime.webdav.server.acl.ReplacedAce;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclProperty;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;
import eu.agno3.runtime.webdav.server.impl.DefaultDavResourceFactory;
import eu.agno3.runtime.webdav.server.impl.DefaultWebdavRequestImpl;
import eu.agno3.runtime.webdav.server.impl.ExtendedMultiStatusResponse;


/**
 * @author mbechler
 *
 */
@Component ( service = FileshareDAVTreeProvider.class )
public class FileshareDAVTreeProviderImpl implements FileshareDAVTreeProvider, FileshareDAVTreeProviderInternal {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    /**
     * 
     */
    private static final Principal OWNER_PRINCIPAL = Principal.getPropertyPrincipal(DavPropertyName.create("owner", DavConstants.NAMESPACE)); //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileshareDAVTreeProviderImpl.class);

    private static final Set<String> REJECT_FILES = new HashSet<>();
    private static final Set<String> REJECT_PREFIX = new HashSet<>();

    static final Set<String> ROOT_BLACKLIST = new HashSet<>();

    private static final Set<String> PUT_CONTENT_SUPPORTED = new HashSet<>();

    private static Field REQUEST_FIELD;

    private DAVPrivileges privileges = new DAVPrivileges();
    private UserRootSubtreeProvider userRootProvider = new UserRootSubtreeProvider();
    private List<SubtreeProvider> providers = new ArrayList<>(
        Arrays.asList(
            new GroupsRootSubtreeProvider(),
            new OthersRootSubtreeProvider(),
            new SubjectsSubtreeProvider(),
            new TransferSubtreeProvider(),
            this.userRootProvider));

    static {
        try {
            REQUEST_FIELD = InputContextImpl.class.getDeclaredField("request"); //$NON-NLS-1$
            REQUEST_FIELD.setAccessible(true);
        }
        catch (
            NoSuchFieldException |
            SecurityException e )

        {
            log.error("Failed to get request field from Jackrabbit InputContextImpl"); //$NON-NLS-1$
        }

        REJECT_FILES.add("folder.jpg"); //$NON-NLS-1$
        REJECT_FILES.add("folder.gif"); //$NON-NLS-1$
        REJECT_FILES.add("desktop.ini"); //$NON-NLS-1$
        REJECT_FILES.add("thumbs.db"); //$NON-NLS-1$
        REJECT_FILES.add("swdrm.dll"); //$NON-NLS-1$
        REJECT_FILES.add(".ds_store"); //$NON-NLS-1$
        REJECT_FILES.add(".localized"); //$NON-NLS-1$

        REJECT_PREFIX.add("._."); //$NON-NLS-1$

        ROOT_BLACKLIST.add(GroupsRootDAVNode.GROUPS_NAME);
        ROOT_BLACKLIST.add(OthersRootDAVNode.OTHERS_NAME);

        PUT_CONTENT_SUPPORTED.add("content-length"); //$NON-NLS-1$
        PUT_CONTENT_SUPPORTED.add("content-type"); //$NON-NLS-1$
        PUT_CONTENT_SUPPORTED.add("content-md5"); //$NON-NLS-1$
        PUT_CONTENT_SUPPORTED.add("content-language"); //$NON-NLS-1$
        PUT_CONTENT_SUPPORTED.add("content-range"); //$NON-NLS-1$
    }

    private GuiServiceContext ctx;
    private DeliveryService deliveryService;
    private PolicyEvaluator policyEvaluator;
    private QuotaService quotaService;
    private VFSServiceInternal vfs;
    private AccessControlService accessControl;
    private EntityServiceInternal entityService;
    private DirectoryService directoryService;
    private BrowseServiceInternal browseService;
    private ShareServiceInternal shareService;


    @Reference
    protected synchronized void setGuiServiceContext ( GuiServiceContext cx ) {
        this.ctx = cx;
    }


    protected synchronized void unsetGuiServiceContext ( GuiServiceContext cx ) {
        if ( this.ctx == cx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setDeliveryService ( DeliveryService ds ) {
        this.deliveryService = ds;
    }


    protected synchronized void unsetDeliveryService ( DeliveryService ds ) {
        if ( this.deliveryService == ds ) {
            this.deliveryService = null;
        }
    }


    @Reference
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        this.policyEvaluator = pe;
    }


    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        if ( this.policyEvaluator == pe ) {
            this.policyEvaluator = null;
        }
    }


    @Reference
    protected synchronized void setQuotaService ( QuotaService qs ) {
        this.quotaService = qs;
    }


    protected synchronized void unsetQuotaService ( QuotaService qs ) {
        if ( this.quotaService == qs ) {
            this.quotaService = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    @Reference
    protected synchronized void setAccessControl ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControl ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    @Reference
    protected synchronized void setEntityService ( EntityServiceInternal es ) {
        this.entityService = es;
    }


    protected synchronized void unsetEntityService ( EntityServiceInternal es ) {
        if ( this.entityService == es ) {
            this.entityService = null;
        }
    }


    @Reference
    protected synchronized void setBrowseServiceInternal ( BrowseServiceInternal bs ) {
        this.browseService = bs;
    }


    protected synchronized void unsetBrowseServiceInternal ( BrowseServiceInternal bs ) {
        if ( this.browseService == bs ) {
            this.browseService = null;
        }
    }


    @Reference
    protected synchronized void setShareServiceInternal ( ShareServiceInternal ss ) {
        this.shareService = ss;
    }


    protected synchronized void unsetShareServiceInternal ( ShareServiceInternal ss ) {
        if ( this.shareService == ss ) {
            this.shareService = null;
        }
    }


    @Reference
    protected synchronized void setDirectoryService ( DirectoryService ds ) {
        this.directoryService = ds;
    }


    protected synchronized void unsetDirectoryService ( DirectoryService ds ) {
        if ( this.directoryService == ds ) {
            this.directoryService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getRootNode()
     */
    @Override
    public DAVTreeNode<EntityKey> getRootNode () throws DavException {
        return this.userRootProvider.getRootNode(this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal#getProviders()
     */
    @Override
    public Collection<SubtreeProvider> getProviders () {
        return this.providers;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#writeOutput(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      java.io.OutputStream)
     */
    @Override
    public void writeOutput ( DAVTreeNode<EntityKey> n, OutputStream outputStream ) throws IOException {
        if ( ! ( n instanceof EntityDAVNode ) ) {
            return;
        }

        EntityDAVNode en = (EntityDAVNode) n;
        if ( ! ( en.getEntity() instanceof VFSFileEntity ) ) {
            return;
        }

        try ( VFSContext v = this.vfs.getVFS(n.getId()).begin(true) ) {
            this.deliveryService.deliverDAV(
                v,
                (VFSFileEntity) en.getEntity(),
                WebUtils.getHttpRequest(SecurityUtils.getSubject()),
                WebUtils.getHttpResponse(SecurityUtils.getSubject()),
                outputStream);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            log.error("Failed to deliver file", e); //$NON-NLS-1$
            throw new IOException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getResourceWithPath(java.lang.String)
     */
    @Override
    public DAVTreeNode<EntityKey> getResourceWithPath ( String repositoryPath ) throws DavException {

        if ( !SecurityUtils.getSubject().isAuthenticated() ) {
            log.debug("Not authenticated"); //$NON-NLS-1$
            return null;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Get resource with path " + repositoryPath); //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(repositoryPath) ) {
            int lastSep = repositoryPath.lastIndexOf('/');
            String localName = PathEscapeUtil.unescapePathSegment(repositoryPath.substring(lastSep + 1).toLowerCase());
            if ( REJECT_FILES.contains(localName) ) {
                return null;
            }

            for ( String prefix : REJECT_PREFIX ) {
                if ( localName.startsWith(prefix) ) {
                    return null;
                }
            }
        }

        try {
            DAVLayout layout = getLayout(WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            for ( SubtreeProvider sp : this.providers ) {
                if ( sp.isApplicable(repositoryPath, layout) ) {
                    return sp.resolve(this, repositoryPath, layout);
                }
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            wrapException(e);
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getAbsolutePath(eu.agno3.runtime.webdav.server.DAVTreeNode)
     */
    @Override
    public String getAbsolutePath ( DAVTreeNode<EntityKey> node ) throws DavException {
        String absPath = node.getAbsolutePath();
        if ( absPath != null ) {
            return absPath;
        }
        try {
            DAVLayout l;
            if ( node instanceof AbstractVirtualDAVNode ) {
                l = ( (AbstractVirtualDAVNode) node ).getLayout();
            }
            else if ( node instanceof EntityDAVNode ) {
                l = ( (EntityDAVNode) node ).getLayout();
            }
            else {
                throw new EntityNotFoundException();
            }

            DAVTreeNode<EntityKey> actualNode = node;
            StringBuilder path = new StringBuilder(); // $NON-NLS-1$
            boolean handled = false;
            if ( node instanceof RemovedDAVNode ) {
                actualNode = ( (RemovedDAVNode) node ).getParent();
                absPath = actualNode.getAbsolutePath();
                if ( absPath != null ) {
                    path.append(absPath);
                    handled = true;
                }
            }

            if ( !handled ) {
                for ( SubtreeProvider subtreeProvider : this.providers ) {
                    handled |= subtreeProvider.contributePath(this, actualNode, l, path);
                    if ( handled ) {
                        break;
                    }
                }
            }

            if ( !handled ) {
                throw new EntityNotFoundException("No path found for " + actualNode); //$NON-NLS-1$
            }

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Constructed path %s for %s", path, actualNode)); //$NON-NLS-1$
            }

            if ( actualNode.isCollection() && path.length() > 0 && path.charAt(path.length() - 1) != '/' ) {
                path.append('/');
            }

            if ( node instanceof RemovedDAVNode ) {
                path.append(node.getPathName());
                if ( node.isCollection() && path.length() > 0 && path.charAt(path.length() - 1) != '/' ) {
                    path.append('/');
                }
            }

            return path.toString();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to construct path", e); //$NON-NLS-1$
            wrapException(e);
            return null;
        }

    }


    @Override
    public DateTime getRecursiveEntityLastModified ( VFSContainerEntity e ) {
        if ( e != null ) {
            DateTime lastMod = this.ctx.getBrowseService().getRecursiveLastModified(e.getEntityKey());
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Entity %s last modified %s", e, lastMod)); //$NON-NLS-1$
            }
            return lastMod;
        }
        return null;
    }


    @Override
    public Set<DavPropertyName> getSupportedDynamicProperties ( DAVTreeNode<EntityKey> wrapped ) {
        log.debug("Get supported dynamic properties"); //$NON-NLS-1$
        if ( wrapped instanceof EntityDAVNode ) {
            return new HashSet<>(
                Arrays.asList(
                    SecurityConstants.ACL,
                    SecurityConstants.ACL_RESTRICTIONS,
                    SecurityConstants.CURRENT_USER_PRIVILEGE_SET,
                    SecurityConstants.GROUP,
                    SecurityConstants.INHERITED_ACL_SET,
                    SecurityConstants.OWNER,
                    SecurityConstants.PRINCIPAL_COLLECTION_SET,
                    SecurityConstants.SUPPORTED_PRIVILEGE_SET,
                    Constants.PERMISSIONS,
                    Constants.ALLOWED_SECURITY_LEVELS,
                    Constants.SEND_NOTIFICATIONS,
                    Constants.ALLOW_FILE_OVERWRITE,
                    Constants.GRANT_DETAILS));
        }
        else if ( wrapped instanceof SubjectDAVNode ) {
            return new HashSet<>(
                Arrays.asList(
                    SecurityConstants.PRINCIPAL_URL,
                    SecurityConstants.ALTERNATE_URI_SET,
                    SecurityConstants.GROUP_MEMBER_SET,
                    SecurityConstants.GROUP_MEMBERSHIP));
        }

        return Collections.EMPTY_SET;
    }


    @Override
    public DavProperty<?> getDynamicProperty ( DAVTreeNode<EntityKey> wrapped, DavPropertyName name ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Get property {%s}%s on %s", name.getNamespace().getURI(), name.getName(), wrapped)); //$NON-NLS-1$
        }

        HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        if ( DavConstants.NAMESPACE.equals(name.getNamespace()) ) {
            if ( "acl-restrictions".equals(name.getName()) ) { //$NON-NLS-1$
                return new AclRestrictionsProperty(true, true, false, OWNER_PRINCIPAL);
            }

            // RFC 5397
            if ( "current-user-principal".equals(name.getName()) ) { //$NON-NLS-1$
                try {
                    return new ResolvedHrefProperty(name, SubjectsSubtreeProvider.getUserUrl(this.accessControl.getCurrentUser()), false, false);
                }
                catch (
                    UserNotFoundException |
                    AuthenticationException e ) {
                    log.warn("Failed to get user info", e); //$NON-NLS-1$
                    return null;
                }
            }
        }

        if ( DavConstants.NAMESPACE.equals(name.getNamespace()) && wrapped instanceof EntityDAVNode ) {
            return getEntityProperty(name, req, (EntityDAVNode) wrapped);
        }
        else if ( DavConstants.NAMESPACE.equals(name.getNamespace()) && wrapped instanceof AbstractVirtualDAVNode ) {
            return getVirtualProperty(name, req, wrapped);
        }

        if ( Constants.AGNO3_NS.equals(name.getNamespace()) && wrapped instanceof EntityDAVNode ) {
            return getCustomEntityProperty(wrapped, name, req);
        }

        if ( wrapped instanceof TokenSubjectDAVNode && Constants.AGNO3_NS.equals(name.getNamespace()) ) {
            if ( Constants.TOKEN_URL.getName().equals(name.getName()) ) {
                try {
                    ShareProperties props = new ShareProperties();
                    TokenShare ts = this.shareService.recreateTokenShare( ( (TokenSubjectDAVNode) wrapped ).getGrant(), props);
                    return new HrefProperty(Constants.TOKEN_URL, ts.getViewURL(), true);
                }
                catch ( FileshareException e ) {
                    log.warn("Failed to recreate token share", e); //$NON-NLS-1$
                    return null;
                }
            }
        }
        return null;
    }


    /**
     * @param wrapped
     * @param name
     * @param req
     */
    private DavProperty<?> getCustomEntityProperty ( DAVTreeNode<EntityKey> wrapped, DavPropertyName name, HttpServletRequest req ) {
        EntityDAVNode en = (EntityDAVNode) wrapped;
        if ( Constants.PERMISSIONS.equals(name) ) {
            return new DefaultDavProperty<>(Constants.PERMISSIONS, this.privileges.mapEffectivePermissions(en), true);
        }
        else if ( Constants.GRANT_SETTINGS.equals(name) ) {
            return new GrantSettingsProperty(makeGrantSettings(en));
        }
        else if ( Constants.ALLOWED_SECURITY_LEVELS.equals(name) ) {
            return new AllowedSecurityLevelsProperty(makeAllowedSecurityLevels(en, req));
        }
        else if ( wrapped.isCollection() && Constants.ALLOW_FILE_OVERWRITE.equals(name) ) {
            return new DefaultDavProperty<>(Constants.ALLOW_FILE_OVERWRITE, ( (VFSContainerEntity) en.getEntity() ).getAllowFileOverwrite(), true);
        }
        else if ( wrapped.isCollection() && Constants.SEND_NOTIFICATIONS.equals(name) ) {
            return new DefaultDavProperty<>(Constants.SEND_NOTIFICATIONS, ( (VFSContainerEntity) en.getEntity() ).getSendNotifications(), true);
        }
        else if ( Constants.GRANT_DETAILS.equals(name) && en.getGrantId() != null ) {
            try {
                return makeGrantDetails(en.getGrantId());
            }
            catch ( FileshareException e ) {
                log.debug("Failed to get grant details", e); //$NON-NLS-1$
                return null;
            }
        }
        return null;
    }


    /**
     * @param grantId
     * @return
     * @throws FileshareException
     */
    private DavProperty<?> makeGrantDetails ( UUID grantId ) throws FileshareException {
        return new GrantDetailProperty(this.shareService.getGrant(grantId));
    }


    /**
     * @param wrapped
     * @param req
     * @return
     */
    private List<String> makeAllowedSecurityLevels ( EntityDAVNode wrapped, HttpServletRequest req ) {
        List<String> labels = new LinkedList<>(this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefinedLabels());
        Iterator<String> it = labels.iterator();
        for ( String label : labels ) {
            if ( this.policyEvaluator.isPolicyFulfilled(label, req) != null ) {
                it.remove();
            }
        }
        int idx = labels.indexOf(wrapped.getEntity().getSecurityLabel().getLabel());
        if ( idx < 0 ) {
            return labels;
        }
        return labels.subList(idx, labels.size());
    }


    /**
     * @param wrapped
     * @return
     */
    private GrantSettings makeGrantSettings ( EntityDAVNode wrapped ) {
        GrantSettings s = new GrantSettings();

        SecurityLabel lbl = wrapped.getEntity().getSecurityLabel();

        try {
            PolicyConfiguration pc = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(lbl.getLabel());

            Set<GrantType> allowedTypes = EnumSet.copyOf(pc.getAllowedShareTypes());
            if ( this.ctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
                allowedTypes.remove(GrantType.MAIL);
            }
            else {
                s.setNotificationsAllowed(true);
            }

            s.setAllowedTypes(allowedTypes);

            Duration defaultShareLifetime = pc.getDefaultShareLifetime();
            Duration maxShareLifetime = pc.getMaximumShareLifetime();
            if ( defaultShareLifetime != null ) {
                s.setDefaultExpire(DateTime.now().plus(defaultShareLifetime).withMillisOfDay(0));
            }
            if ( maxShareLifetime != null ) {
                s.setMaxExpire(DateTime.now().plus(maxShareLifetime));
            }

            s.setMinTokenPasswordEntropy(pc.getMinTokenPasswordEntropy());
            s.setNoUserTokenPassword(pc.isNoUserTokenPasswords());
            s.setRequireTokenPassword(pc.isRequireTokenPassword());

            s.setDefaultPermissions(DAVPrivileges.READ_PRIVILEGE);

        }
        catch ( PolicyNotFoundException e ) {
            log.debug("Policy not found", e); //$NON-NLS-1$
        }

        return s;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#addDynamicHeaders(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      org.apache.jackrabbit.webdav.io.OutputContext)
     */
    @Override
    public void addDynamicHeaders ( DAVTreeNode<EntityKey> wrapped, OutputContext oc ) {
        if ( wrapped instanceof EntityDAVNode ) {
            Integer perm = this.privileges.mapEffectivePermissions((EntityDAVNode) wrapped);
            if ( perm != null ) {
                oc.setProperty("X-Permissions", perm.toString()); //$NON-NLS-1$
            }

            if ( ( (EntityDAVNode) wrapped ).getGrantId() == null ) {
                oc.setProperty("X-Is-Owner", Boolean.TRUE.toString()); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param name
     * @param req
     * @param wrapped
     * @return
     */
    private DavProperty<?> getVirtualProperty ( DavPropertyName name, HttpServletRequest req, DAVTreeNode<EntityKey> wrapped ) {
        switch ( name.getName() ) {
        case "current-user-privilege-set": //$NON-NLS-1$
            return new CurrentUserPrivilegeSetProperty(new Privilege[] {
                Privilege.PRIVILEGE_READ, Privilege.PRIVILEGE_READ_CURRENT_USER_PRIVILEGE_SET
            });
        case "supported-privilege-set": //$NON-NLS-1$
            return new SupportedPrivilegeSetProperty(new SupportedPrivilege[0]);
        }

        if ( wrapped instanceof SubjectDAVNode ) {
            return getSubjectProperty(name, (SubjectDAVNode) wrapped);
        }
        return null;
    }


    /**
     * @param name
     * @param wrapped
     * @return
     */
    private DavProperty<?> getSubjectProperty ( DavPropertyName name, SubjectDAVNode wrapped ) {
        if ( SecurityConstants.PRINCIPAL_URL.equals(name) ) {
            String absolutePath = wrapped.getAbsolutePath();
            if ( !StringUtils.isBlank(absolutePath) ) {
                return new ResolvedHrefProperty(name, absolutePath, false, false);
            }
            log.warn("No uri for " + wrapped); //$NON-NLS-1$
        }
        else if ( SecurityConstants.GROUP_MEMBERSHIP.equals(name) ) {
            UUID subjectId = null;
            if ( wrapped instanceof UserSubjectDAVNode ) {
                subjectId = ( (UserSubjectDAVNode) wrapped ).getUserInfo().getId();
            }
            else if ( wrapped instanceof GroupSubjectDAVNode ) {
                subjectId = ( (GroupSubjectDAVNode) wrapped ).getGroupInfo().getId();
            }
            if ( subjectId == null ) {
                return null;
            }
            try {
                List<String> refs = new ArrayList<>();
                Subject subject = this.ctx.getSubjectService().getSubject(subjectId);
                for ( Group group : subject.getMemberships() ) {
                    refs.add(SubjectsSubtreeProvider.getGroupUrl(group));
                }
                boolean[] collections = new boolean[refs.size()];
                Arrays.fill(collections, false);
                return new ResolvedHrefProperty(name, refs.toArray(new String[refs.size()]), collections, true);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                log.warn("Failed to lookup subject membership", e); //$NON-NLS-1$
                return null;
            }
        }
        return null;
    }


    /**
     * @param name
     * @param req
     * @param en
     * @return
     */
    private DavProperty<?> getEntityProperty ( DavPropertyName name, HttpServletRequest req, EntityDAVNode en ) {
        if ( en.getEntity() != null ) {
            Subject owner = en.getEntity().getOwner();
            switch ( name.getName() ) {
            case "owner": //$NON-NLS-1$
                return getSubjectHref(name, owner);
            case "group": //$NON-NLS-1$
                if ( ! ( owner instanceof Group ) ) {
                    return null;
                }
                return getSubjectHref(name, owner);
            case "current-user-privilege-set": //$NON-NLS-1$
                Privilege[] mapPermissions = this.privileges.mapPermissions(en.getPermissions(), en.getGrantId() == null);
                return new CurrentUserPrivilegeSetProperty(mapPermissions);
            case "acl": //$NON-NLS-1$
                if ( !en.getEntity().hasParent() ) {
                    return null;
                }
                return makeACL(en);
            case "supported-privilege-set": //$NON-NLS-1$
                if ( !en.getEntity().hasParent() ) {
                    return null;
                }
                return new SupportedPrivilegeSetProperty(new SupportedPrivilege[] {
                    this.privileges.getSupportedPrivilege(en.getEntity(), getUserLocale(req))
                });
            }
        }

        if ( "principal-collection-set".equals(name.getName()) ) { //$NON-NLS-1$
            return new ResolvedHrefProperty(SecurityConstants.PRINCIPAL_COLLECTION_SET, new String[] {
                UserSubjectsDAVNode.SUBJECTS_USERS, GroupSubjectsDAVNode.SUBJECTS_GROUPS
            }, new boolean[] {
                true, true
            }, true);
        }

        return null;
    }


    /**
     * @param name
     * @param owner
     * @return
     */
    private static DavProperty<?> getSubjectHref ( DavPropertyName name, Subject owner ) {
        return new ResolvedHrefProperty(name, SubjectsSubtreeProvider.getSubjectUrl(owner), false, false);
    }


    /**
     * @param req
     * @return
     */
    private static Locale getUserLocale ( HttpServletRequest req ) {
        return Locale.ROOT;
    }


    @Override
    public void alterAcl ( DAVTreeNode<EntityKey> wrapped, ReplacedAclProperty acl ) throws DavException {
        if ( ! ( wrapped instanceof EntityDAVNode ) ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        String remHdr = req.getHeader("X-ACL-Removed"); //$NON-NLS-1$
        boolean haveChangeData = remHdr != null;

        if ( !haveChangeData && log.isDebugEnabled() ) {
            log.debug("Heuristic ACL modification, missing hints"); //$NON-NLS-1$
        }

        EntityDAVNode dn = (EntityDAVNode) wrapped;
        try ( VFSContext vc = this.vfs.getVFS(wrapped.getId()).begin(false) ) {
            this.accessControl.checkOwner(vc, dn.getEntity());
            this.policyEvaluator.checkPolicy(vc, dn.getEntity(), req);
            ContentEntity mapped = vc.getOrCreateMappedEntity(dn.getEntity());
            Set<Grant> existingGrants = mapped.getGrants();

            Set<Grant> removals;

            Locale locale = req.getLocale();
            if ( !haveChangeData ) {
                removals = alterACLHeuristic(acl, dn, vc, existingGrants, locale);
            }
            else {
                removals = alterACLImproved(acl, dn, vc, existingGrants, locale, remHdr);
            }

            for ( Grant remove : removals ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Revoke grant " + remove); //$NON-NLS-1$
                }
                this.shareService.revokeShare(remove.getId());
            }

            vc.commit();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException |
            IllegalArgumentException |
            UnsupportedEncodingException e ) {
            log.debug("Failed", e); //$NON-NLS-1$
            wrapException(e);
        }
    }


    /**
     * @param acl
     * @param dn
     * @param vc
     * @param existingGrants
     * @return
     * @throws DavException
     * @throws FileshareException
     * @throws UnsupportedEncodingException
     */
    private Set<Grant> alterACLHeuristic ( ReplacedAclProperty acl, EntityDAVNode dn, VFSContext vc, Set<Grant> existingGrants, Locale l )
            throws DavException, FileshareException, UnsupportedEncodingException {
        Set<Grant> removals = new HashSet<>(existingGrants);
        List<ReplacedAce> aces = acl.getValue();
        if ( log.isDebugEnabled() ) {
            log.debug("alterAcl: " + aces); //$NON-NLS-1$
        }
        for ( ReplacedAce ace : aces ) {
            if ( ace.isProtected() || ace.getInheritedHref() != null ) {
                continue;
            }
            Map<QName, Element> extraElements = ace.getExtraElements();
            Principal p = ace.getPrincipal();
            Set<GrantPermission> perms = getACEPermissions(ace);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Grant %s to %s", perms, p.getHref())); //$NON-NLS-1$
            }

            DAVTreeNode<EntityKey> princNode = getPrincipalNode(p);
            Grant g = getGrant(vc, existingGrants, extraElements, princNode);
            if ( g == null ) {
                createGrant(dn, perms, princNode, extraElements, l);
            }
            else {
                if ( log.isDebugEnabled() ) {
                    log.debug("Existing grant " + g); //$NON-NLS-1$
                }
                removals.remove(g);
                updateGrant(perms, g, extraElements);
            }
        }
        return removals;
    }


    /**
     * @param acl
     * @param dn
     * @param vc
     * @param existingGrants
     * @param remHdr
     * @param addHdr
     * @param modHdr
     * @return
     * @throws FileshareException
     * @throws DavException
     * @throws UnsupportedEncodingException
     */
    private Set<Grant> alterACLImproved ( ReplacedAclProperty acl, EntityDAVNode dn, VFSContext vc, Set<Grant> existingGrants, Locale l,
            String remHdr ) throws FileshareException, DavException, UnsupportedEncodingException {

        Set<UUID> rem = makeUUIDs(remHdr);
        Set<Grant> toRemove = new HashSet<>();

        for ( ReplacedAce ace : acl.getValue() ) {
            if ( ace.isProtected() || ace.getInheritedHref() != null ) {
                continue;
            }
            Principal p = ace.getPrincipal();
            Set<GrantPermission> perms = getACEPermissions(ace);
            Map<QName, Element> extraElements = ace.getExtraElements();
            if ( extraElements.containsKey(Constants.MODIFY) && extraElements.containsKey(Constants.GRANT_ID) ) {
                UUID gid = UUID.fromString(extraElements.get(Constants.GRANT_ID).getTextContent());
                if ( log.isDebugEnabled() ) {
                    log.debug("Modifying grant id " + gid); //$NON-NLS-1$
                }

                Grant g = this.shareService.getGrant(gid);
                updateGrant(perms, g, extraElements);
            }
            else if ( extraElements.containsKey(Constants.ADD) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Adding grant %s to %s", perms, p.getHref())); //$NON-NLS-1$
                }
                createGrant(dn, perms, getPrincipalNode(p), extraElements, l);
            }
            else if ( log.isDebugEnabled() ) {
                log.debug(String.format("Unchanged grant %s to %s", perms, p.getHref())); //$NON-NLS-1$
            }
        }

        for ( Grant ex : existingGrants ) {
            if ( rem.contains(ex.getId()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Removing grant " + ex.getId()); //$NON-NLS-1$
                }
                toRemove.add(ex);
            }
        }
        return toRemove;

    }


    /**
     * @param modHdr
     * @return
     */
    private static Set<UUID> makeUUIDs ( String modHdr ) {
        Set<UUID> ids = new HashSet<>();
        if ( modHdr == null ) {
            return ids;
        }
        for ( String id : StringUtils.split(modHdr, ',') ) {
            try {
                ids.add(UUID.fromString(id.trim()));
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to parse ID " + id, e); //$NON-NLS-1$
            }
        }
        return ids;
    }


    /**
     * @param p
     * @return
     * @throws DavException
     */
    private DAVTreeNode<EntityKey> getPrincipalNode ( Principal p ) throws DavException {
        if ( StringUtils.isBlank(p.getHref()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Unsupported principal type " + p); //$NON-NLS-1$
            }
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Unsupported principal type", //$NON-NLS-1$
                null,
                createError(DavConstants.NAMESPACE, "recognized-principal")); //$NON-NLS-1$
        }

        DAVTreeNode<EntityKey> princNode = this.getResourceWithPath(p.getHref());
        if ( ! ( princNode instanceof SubjectDAVNode ) && ! ( princNode instanceof TokenSubjectsDAVNode ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Unsupported principal type " + princNode); //$NON-NLS-1$
            }
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Unsupported principal type", //$NON-NLS-1$
                null,
                createError(DavConstants.NAMESPACE, "recognized-principal")); //$NON-NLS-1$
        }
        return princNode;
    }


    /**
     * @param vc
     * @param existingGrants
     * @param extraElements
     * @param princNode
     * @return
     * @throws FileshareException
     * @throws UnsupportedEncodingException
     */
    private Grant getGrant ( VFSContext vc, Set<Grant> existingGrants, Map<QName, Element> extraElements, DAVTreeNode<EntityKey> princNode )
            throws FileshareException, UnsupportedEncodingException {
        if ( extraElements.containsKey(Constants.GRANT_ID) ) {
            UUID gid = UUID.fromString(extraElements.get(Constants.GRANT_ID).getTextContent());
            if ( log.isDebugEnabled() ) {
                log.debug("Found grant id " + gid); //$NON-NLS-1$
            }
            return this.shareService.getGrant(gid);
        }

        return SubjectsSubtreeProvider.principalToGrant(vc, princNode, existingGrants);
    }


    /**
     * @param dn
     * @param perms
     * @param princNode
     * @param extraElements
     * @throws FileshareException
     * @throws DavException
     * @throws UnsupportedEncodingException
     */
    private void createGrant ( EntityDAVNode dn, Set<GrantPermission> perms, DAVTreeNode<EntityKey> princNode, Map<QName, Element> extraElements,
            Locale l ) throws FileshareException, DavException, UnsupportedEncodingException {
        ShareProperties props = makeShareProps(dn.getEntity(), perms, extraElements, l);

        if ( princNode instanceof UserSubjectDAVNode || princNode instanceof GroupSubjectDAVNode ) {
            createSubjectGrant(dn, princNode, extraElements, props);
        }
        else if ( princNode instanceof MailSubjectDAVNode ) {
            createMailGrant(dn, princNode, extraElements, props);
        }
        else if ( extraElements.containsKey(Constants.TOK_ID) && princNode instanceof TokenSubjectsDAVNode ) {
            createTokenGrant(dn, extraElements, props);
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Unsupported principal type " + princNode); //$NON-NLS-1$
            }
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Unsupported principal type", //$NON-NLS-1$
                null,
                createError(DavConstants.NAMESPACE, "recognized-principal")); //$NON-NLS-1$
        }
    }


    /**
     * @param dn
     * @param extraElements
     * @param props
     * @throws FileshareException
     * @throws UnsupportedEncodingException
     */
    private void createTokenGrant ( EntityDAVNode dn, Map<QName, Element> extraElements, ShareProperties props )
            throws FileshareException, UnsupportedEncodingException {
        log.debug("Create token grant"); //$NON-NLS-1$
        String id = extraElements.get(Constants.TOK_ID).getTextContent();
        Element commentEl = extraElements.get(Constants.TOK_COMMENT);
        if ( commentEl != null ) {
            props.setMessage(commentEl.getTextContent());
        }

        TokenShare shareToken = this.shareService.shareToken(dn.getId(), id, props);

        String viewURL = shareToken.getViewURL();
        String pwString = StringUtils.EMPTY;
        if ( !StringUtils.isBlank(props.getPassword()) ) {
            pwString = String.format(",pw=%s", encodeURLParam(props.getPassword())); //$NON-NLS-1$
        }

        HttpServletResponse httpResponse = WebUtils.getHttpResponse(SecurityUtils.getSubject());
        httpResponse.addHeader(
            "X-Created-Grant", //$NON-NLS-1$
            String.format(
                "id=%s,type=link,ref=%s%s", //$NON-NLS-1$
                shareToken.getGrant().getId(),
                encodeURLParam(viewURL), // $NON-NLS-1$
                pwString));
    }


    private static String encodeURLParam ( String val ) throws UnsupportedEncodingException {
        return StringUtils.replace(
            URLEncoder.encode(val, UTF_8),
            "+", //$NON-NLS-1$
            "%20"); //$NON-NLS-1$
    }


    /**
     * @param dn
     * @param princNode
     * @param extraElements
     * @param props
     * @throws FileshareException
     * @throws UnsupportedEncodingException
     */
    private void createMailGrant ( EntityDAVNode dn, DAVTreeNode<EntityKey> princNode, Map<QName, Element> extraElements, ShareProperties props )
            throws FileshareException, UnsupportedEncodingException {
        log.debug("Create mail grant"); //$NON-NLS-1$
        boolean resend = extraElements.containsKey(Constants.MAIL_RESEND);
        MailRecipient recp = new MailRecipient();
        recp.setMailAddress( ( (MailSubjectDAVNode) princNode ).getMailAddress());
        List<MailGrant> issued = this.shareService.shareByMail(dn.getId(), Collections.singleton(recp), props, resend);

        if ( issued.isEmpty() ) {
            throw new GrantExistsException();
        }

        MailGrant g = issued.get(0);
        String pwString = StringUtils.EMPTY;
        if ( !StringUtils.isBlank(props.getPassword()) ) {
            pwString = String.format(",pw=%s", encodeURLParam(props.getPassword())); //$NON-NLS-1$
        }

        HttpServletResponse httpResponse = WebUtils.getHttpResponse(SecurityUtils.getSubject());
        httpResponse.addHeader(
            "X-Created-Grant", //$NON-NLS-1$
            String.format(
                "id=%s,type=mail%s", //$NON-NLS-1$
                g.getId(),
                pwString));
    }


    /**
     * @param dn
     * @param princNode
     * @param extraElements
     * @param props
     * @throws FileshareException
     */
    private void createSubjectGrant ( EntityDAVNode dn, DAVTreeNode<EntityKey> princNode, Map<QName, Element> extraElements, ShareProperties props )
            throws FileshareException {
        log.debug("Create user grant"); //$NON-NLS-1$
        UUID id = princNode instanceof UserSubjectDAVNode ? ( (UserSubjectDAVNode) princNode ).getUserInfo().getId()
                : ( (GroupSubjectDAVNode) princNode ).getGroupInfo().getId();

        List<SubjectGrant> issued = this.shareService
                .shareToSubjects(dn.getId(), Arrays.asList(id), props, extraElements.containsKey(Constants.NOTIFY));

        if ( issued.isEmpty() ) {
            throw new GrantExistsException();
        }

        SubjectGrant g = issued.get(0);
        HttpServletResponse httpResponse = WebUtils.getHttpResponse(SecurityUtils.getSubject());
        httpResponse.addHeader(
            "X-Created-Grant", //$NON-NLS-1$
            String.format(
                "id=%s,type=subject", //$NON-NLS-1$
                g.getId()));
    }


    /**
     * @param perms
     * @param extraElements
     * @return
     * @throws DavException
     * @throws PolicyNotFoundException
     */
    private ShareProperties makeShareProps ( VFSEntity e, Set<GrantPermission> perms, Map<QName, Element> extraElements, Locale l )
            throws DavException, PolicyNotFoundException {
        ShareProperties props = new ShareProperties();
        props.setPermissions(perms);
        if ( extraElements.containsKey(Constants.EXPIRES) ) {
            String expData = extraElements.get(Constants.EXPIRES).getTextContent();
            if ( !StringUtils.isBlank(expData) ) {
                props.setExpiry(DateTime.parse(expData));
            }
        }

        if ( extraElements.containsKey(Constants.MAIL_SUBJECT) ) {
            props.setNotificationSubject(extraElements.get(Constants.MAIL_SUBJECT).getTextContent());
        }

        if ( extraElements.containsKey(Constants.MAIL_TEXT) ) {
            props.setMessage(extraElements.get(Constants.MAIL_TEXT).getTextContent());
        }

        if ( extraElements.containsKey(Constants.TOKEN_PASSWORD) ) {
            Element pwElem = extraElements.get(Constants.TOKEN_PASSWORD);
            if ( StringUtils.isBlank(pwElem.getTextContent()) ) {
                // empty password indicates we should create one
                log.debug("Password requested but none, generating one"); //$NON-NLS-1$
                try {
                    props.setPassword(this.shareService.generateSharePassword(e.getSecurityLabel(), l));
                }
                catch ( PasswordGenerationException e1 ) {
                    throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1);
                }
            }
            else {
                props.setPassword(pwElem.getTextContent());
            }
        }

        return props;
    }


    /**
     * @param perms
     * @param g
     * @param extraElements
     * @throws FileshareException
     */
    private void updateGrant ( Set<GrantPermission> perms, Grant g, Map<QName, Element> extraElements ) throws FileshareException {
        if ( !g.getPermissions().equals(perms) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Modified permissions " + g); //$NON-NLS-1$
            }
            this.shareService.setPermissions(g.getId(), perms);
        }

        if ( extraElements.containsKey(Constants.EXPIRES) ) {
            String data = extraElements.get(Constants.EXPIRES).getTextContent();
            DateTime expiry = null;
            if ( !StringUtils.isBlank(data) ) {
                expiry = DateTime.parse(data);
            }
            if ( !Objects.equals(g.getExpires(), expiry) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Modified expires " + g); //$NON-NLS-1$
                }
                this.shareService.setExpiry(g.getId(), expiry);
            }
        }

        if ( g instanceof TokenGrant ) {
            if ( extraElements.containsKey(Constants.TOK_COMMENT) ) {
                String comment = extraElements.get(Constants.TOK_COMMENT).getTextContent();
                if ( Objects.equals(comment, ( (TokenGrant) g ).getComment()) ) {
                    this.shareService.updateComment(g.getId(), comment);
                }
            }
            if ( extraElements.containsKey(Constants.TOK_ID) ) {
                String identifier = extraElements.get(Constants.TOK_ID).getTextContent();
                if ( Objects.equals(identifier, ( (TokenGrant) g ).getIdentifier()) ) {
                    this.shareService.updateIdentifier(g.getId(), identifier);
                }
            }
        }
    }


    /**
     * @param ace
     * @return
     * @throws DavException
     */
    private Set<GrantPermission> getACEPermissions ( ReplacedAce ace ) throws DavException {
        if ( ace.isDeny() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Contains Deny " + ace); //$NON-NLS-1$
            }
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Deny not supported", //$NON-NLS-1$
                null,
                createError(DavConstants.NAMESPACE, "grant-only")); //$NON-NLS-1$
        }

        if ( ace.isInvert() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Contains Invert " + ace); //$NON-NLS-1$
            }
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Invert not supported", //$NON-NLS-1$
                null,
                createError(DavConstants.NAMESPACE, "no-invert")); //$NON-NLS-1$
        }

        Privilege[] privs = ace.getPrivileges();
        Set<Privilege> validPrivileges = new HashSet<>();
        for ( Privilege priv : privs ) {
            if ( this.privileges.isAbstract(priv) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Contains abstract " + ace); //$NON-NLS-1$
                }
                throw new DavException(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Cannot grant abstract privileges", //$NON-NLS-1$
                    null,
                    createError(DavConstants.NAMESPACE, "no-abstract")); //$NON-NLS-1$
            }
            else if ( !this.privileges.isSupported(priv) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Contains unsupported privilege " + ace); //$NON-NLS-1$
                }
                throw new DavException(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Unknown privilege", //$NON-NLS-1$
                    null,
                    createError(DavConstants.NAMESPACE, "not-supported-privilege")); //$NON-NLS-1$
            }
            validPrivileges.add(priv);
        }
        return this.privileges.mapToPermissions(validPrivileges);
    }


    static Element createError ( Namespace namespace, String name ) {
        try {
            return DomUtil.createDocument().createElementNS(namespace.getURI(), name);
        }
        catch (
            DOMException |
            ParserConfigurationException e ) {
            log.warn("Failed to create error response XML", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param qname
     * @param value
     * @return
     */
    private static Element createElement ( QName qname, String value ) {
        try {
            Element expires = DomUtil.createDocument().createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
            expires.setTextContent(value);
            return expires;
        }
        catch (
            DOMException |
            ParserConfigurationException e ) {
            log.warn("Failed to create element", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param en
     * @return
     * @throws DavException
     */
    private ReplacedAclProperty makeACL ( EntityDAVNode en ) {
        try ( VFSContext vc = this.vfs.getVFS(en.getId()).begin(true) ) {
            List<ReplacedAce> aces = new ArrayList<>();
            this.accessControl.checkOwner(vc, en.getEntity());

            aces.add(
                new ReplacedAce(
                    OWNER_PRINCIPAL,
                    false,
                    false,
                    this.privileges.mapPermissions(EnumSet.allOf(GrantPermission.class), true),
                    true,
                    true,
                    null));

            Set<Grant> grants = this.shareService.getEffectiveGrantsInternal(vc, en.getEntity(), null);
            for ( Grant g : grants ) {
                Principal principal = SubjectsSubtreeProvider.grantToPrincipal(g);
                if ( principal == null ) {
                    continue;
                }

                ReplacedAce ace;
                if ( !g.getEntity().getEntityKey().equals(en.getId()) ) {
                    // inherited
                    ace = new ReplacedAce(
                        principal,
                        false,
                        false,
                        this.privileges.mapPermissions(GrantPermission.fromInt(g.getEffectivePerms()), false),
                        true,
                        true,
                        getAbsolutePath(this.adapt(vc, g.getEntity(), g.getPermissions(), false, en.getLayout(), null)));
                }
                else if ( g.getEffectivePerms() != g.getPerms() ) {
                    // partially inherited

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Partially inherited grant %s inherited from %s", g, g.getInheritedId())); //$NON-NLS-1$
                    }

                    Grant inh = null;
                    if ( g.getInheritedId() != null ) {
                        try {
                            inh = this.shareService.getGrant(g.getInheritedId());
                        }
                        catch ( FileshareException e ) {
                            log.debug("Failed to get inherited grant", e); //$NON-NLS-1$
                        }
                    }

                    ace = new ReplacedAce(principal, false, false, this.privileges.mapPermissions(g.getPermissions(), false), true, false, null);
                    ReplacedAce ia = new ReplacedAce(
                        principal,
                        false,
                        false,
                        this.privileges.mapPermissions(GrantPermission.fromInt(g.getEffectivePerms()), false),
                        true,
                        true,
                        inh != null ? getAbsolutePath(this.adapt(vc, inh.getEntity(), inh.getPermissions(), false, en.getLayout(), null)) : null);
                    if ( inh != null ) {
                        addExtraGrantProperties(inh, ia);
                    }
                    aces.add(ia);
                }
                else {
                    ace = new ReplacedAce(principal, false, false, this.privileges.mapPermissions(g.getPermissions(), false), true, false, null);
                }

                addExtraGrantProperties(g, ace);
                aces.add(ace);
            }

            return new ReplacedAclProperty(aces);
        }
        catch (
            FileshareException |
            DavException e ) {
            log.debug("Failed to get ACL", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param g
     * @param ace
     */
    private static void addExtraGrantProperties ( Grant g, ReplacedAce ace ) {
        if ( g.getExpires() != null ) {
            ace.addExtraContentElement(createElement(Constants.EXPIRES, g.getExpires().toString()));
        }
        ace.addExtraContentElement(createElement(Constants.GRANT_ID, g.getId().toString()));

        if ( g instanceof TokenGrant ) {
            TokenGrant tg = (TokenGrant) g;
            if ( tg.getPasswordProtected() ) {
                ace.addExtraContentElement(createElement(Constants.TOKEN_PASSWORD, tg.getPassword()));
            }
        }
    }


    @Override
    public DAVTreeNode<EntityKey> resolveRelative ( VFSContext v, VFSContainerEntity root, String relative, Set<GrantPermission> permissions,
            boolean shared, DAVLayout layout, UUID grantId ) throws FileshareException, DavException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Resolving relative path %s against %s", relative, root)); //$NON-NLS-1$
        }

        String[] pathSegments = PathEscapeUtil.splitPathIntoSegments(relative);
        VFSEntity e = this.ctx.getBrowseService().getRelativePath(root.getEntityKey(), pathSegments);

        HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        if ( e == null && layout == DAVLayout.OWNCLOUD ) {
            return notFoundHandler(v, root, relative, req, permissions, grantId, shared);
        }
        else if ( e == null ) {
            return null;
        }
        return adapt(v, e, permissions, shared, layout, grantId);
    }


    /**
     * @param root
     * @param relative
     * @param shared
     * @param permissions
     * @return
     * @throws FileshareException
     * @throws DavException
     */
    private DAVTreeNode<EntityKey> notFoundHandler ( VFSContext v, VFSContainerEntity root, String relative, HttpServletRequest req,
            Set<GrantPermission> permissions, UUID grantId, boolean shared ) throws FileshareException, DavException {

        String chunked = req.getHeader("OC-Chunked"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(chunked) ) {
            return OCChunkingHandler.handleOCChunked(this, v, root, relative, req, permissions, grantId, shared);
        }

        log.debug("Not found"); //$NON-NLS-1$
        return null;
    }


    /**
     * @param root
     * @param actualRootRelative
     * @param actualName
     * @return
     * @throws FileshareException
     * @throws DavException
     */
    VFSEntity getUploadTarget ( VFSEntity root, String actualRootRelative, String actualName ) throws FileshareException, DavException {
        VFSEntity target;
        if ( StringUtils.isBlank(actualRootRelative) ) {
            return root;
        }

        target = this.ctx.getBrowseService()
                .getRelativePath(root.getEntityKey(), PathEscapeUtil.splitPathIntoSegments(actualRootRelative + "/" + actualName)); //$NON-NLS-1$
        VFSContainerEntity parent = (VFSContainerEntity) this.ctx.getBrowseService()
                .getRelativePath(root.getEntityKey(), PathEscapeUtil.splitPathIntoSegments(actualRootRelative));

        if ( target == null ) {
            return parent;
        }
        else if ( !parent.getAllowFileOverwrite() ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        return target;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getNodeChildren(eu.agno3.runtime.webdav.server.DAVTreeNode)
     */
    @Override
    public Collection<DAVTreeNode<EntityKey>> getNodeChildren ( DAVTreeNode<EntityKey> node ) throws DavException {
        try {
            DAVLayout layout = getLayout(WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            for ( SubtreeProvider subtreeProvider : this.providers ) {
                if ( subtreeProvider.handlesChildren(node, layout) ) {
                    return subtreeProvider.getChildren(this, node, layout);
                }
            }

            if ( node instanceof EntityDAVNode ) {
                return getEntityChildren(node, ( (EntityDAVNode) node ).getPermissions(), ( (EntityDAVNode) node ).isShared(), layout);
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            wrapException(e);
        }

        return Collections.EMPTY_LIST;
    }


    @Override
    public Collection<DAVTreeNode<EntityKey>> getEntityChildren ( DAVTreeNode<EntityKey> node, Set<GrantPermission> permissions, boolean shared,
            DAVLayout layout ) throws FileshareException {
        if ( node.getId() == null ) {
            log.warn("No ID", node); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        return adapt(
            getBrowseService().getChildrenInternal(node.getId(), false),
            permissions,
            shared,
            layout,
            node instanceof EntityDAVNode ? ( (EntityDAVNode) node ).getGrantId() : null,
            node.getId());
    }


    @Override
    public Collection<DAVTreeNode<EntityKey>> adapt ( VFSContext vc, CollectionResult<VFSEntity> res, Set<GrantPermission> permissions,
            boolean shared, DAVLayout layout, UUID grantId ) throws FileshareException {
        List<DAVTreeNode<EntityKey>> adapted = new LinkedList<>();
        for ( VFSEntity e : res.getCollection() ) {
            adapted.add(adapt(vc, e, permissions, shared, layout, grantId));
        }
        return adapted;
    }


    @Override
    public Collection<DAVTreeNode<EntityKey>> adapt ( CollectionResult<VFSEntity> res, Set<GrantPermission> permissions, boolean shared,
            DAVLayout layout, UUID grantId, EntityKey parentKey ) throws FileshareException {
        List<DAVTreeNode<EntityKey>> adapted = new LinkedList<>();
        for ( VFSEntity e : res.getCollection() ) {
            adapted.add(adapt(e, permissions, shared, layout, grantId, parentKey));
        }
        return adapted;
    }


    @Override
    public EntityDAVNode adapt ( VFSContext vc, VFSEntity e, Set<GrantPermission> permissions, boolean shared, DAVLayout layout, UUID grantId )
            throws FileshareException {
        VFSContainerEntity parent = vc.getParent(e);
        EntityKey parentKey = parent != null ? parent.getEntityKey() : null;
        return adapt(e, permissions, shared, layout, grantId, parentKey);
    }


    @Override
    public EntityDAVNode adapt ( VFSEntity e, Set<GrantPermission> permissions, boolean shared, DAVLayout layout, UUID grantId,
            EntityKey parentKey ) {
        EntityDAVNode n = new EntityDAVNode(e, parentKey, layout);
        n.setPermissions(permissions);
        n.setShared(shared);
        if ( layout == DAVLayout.OWNCLOUD && e instanceof VFSContainerEntity ) {
            n.setModificationTime(getRecursiveEntityLastModified((VFSContainerEntity) e));
        }
        if ( grantId != null ) {
            n.setGrantId(grantId);
        }
        return n;
    }


    /**
     * @param input
     * @return
     * @throws FileshareException
     */
    private static HttpServletRequest unwrapRequest ( InputContext input ) throws FileshareException {
        if ( ! ( input instanceof InputContextImpl ) ) {
            throw new FileshareException("Cannot handle input"); //$NON-NLS-1$
        }

        try {
            WebdavRequest wr = (WebdavRequest) REQUEST_FIELD.get(input);

            if ( wr instanceof DefaultWebdavRequestImpl ) {
                return ( (DefaultWebdavRequestImpl) wr ).getHttpRequest();
            }

            return wr;
        }
        catch (
            IllegalArgumentException |
            IllegalAccessException e ) {
            throw new FileshareException("Failed to extract request", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DavException
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#createCollection(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      java.lang.String, org.apache.jackrabbit.webdav.io.InputContext)
     */
    @Override
    public DAVTreeNode<EntityKey> createCollection ( DAVTreeNode<EntityKey> parent, String name, InputContext context ) throws DavException {
        if ( ! ( parent instanceof EntityDAVNode ) ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        EntityDAVNode en = (EntityDAVNode) parent;
        ContainerEntity e = new ContainerEntity();
        e.setLocalName(name);

        try {
            VFSContainerEntity created = this.ctx.getDirectoryService().create(parent.getId(), e);
            return adapt(created, en.getPermissions(), en.isShared(), en.getLayout(), en.getGrantId(), en.getId());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            wrapException(ex);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#delete(eu.agno3.runtime.webdav.server.DAVTreeNode)
     */
    @Override
    public void delete ( DAVTreeNode<EntityKey> node ) throws DavException {
        if ( ! ( node instanceof EntityDAVNode ) ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        try {
            this.ctx.getEntityService().delete(node.getId());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            wrapException(e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DavException
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#rename(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      java.lang.String)
     */
    @Override
    public DAVTreeNode<EntityKey> rename ( DAVTreeNode<EntityKey> node, String targetName ) throws DavException {
        if ( ! ( node instanceof EntityDAVNode ) ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        EntityDAVNode en = (EntityDAVNode) node;

        try {
            VFSEntity renamed = this.ctx.getEntityService().rename(node.getId(), targetName);
            return adapt(renamed, en.getPermissions(), en.isShared(), en.getLayout(), en.getGrantId(), en.getId());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            wrapException(e);
            return null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws DavException
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#move(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, java.lang.String)
     */
    @Override
    public DAVTreeNode<EntityKey> move ( DAVTreeNode<EntityKey> node, DAVTreeNode<EntityKey> toParent, String targetName ) throws DavException {
        if ( ! ( node instanceof EntityDAVNode ) || ! ( toParent instanceof EntityDAVNode ) ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        EntityDAVNode en = (EntityDAVNode) toParent;

        try {
            VFSEntity moved;
            if ( !node.getDisplayName().equals(targetName) ) {
                moved = this.ctx.getEntityService().moveAndRename(node.getId(), toParent.getId(), targetName);
            }
            else {
                moved = this.ctx.getEntityService().move(node.getId(), toParent.getId());
            }
            return adapt(moved, en.getPermissions(), en.isShared(), en.getLayout(), en.getGrantId(), en.getId());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            wrapException(e);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DavException
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#alterProperties(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      java.util.List)
     */
    @Override
    public Map<? extends PropEntry, ExtendedStatus> alterProperties ( DAVTreeNode<EntityKey> node, List<? extends PropEntry> changeList )
            throws DavException {
        if ( ! ( node instanceof EntityDAVNode ) ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        Map<PropEntry, ExtendedStatus> entries = new HashMap<>();
        for ( PropEntry pe : changeList ) {
            if ( pe instanceof DavProperty<?> ) {
                entries.put(pe, alterProperty(node, (DavProperty<?>) pe));
            }
            else if ( pe instanceof DavPropertyName ) {
                entries.put(pe, removeProperty(node, (DavPropertyName) pe));
            }
            else {
                entries.put(pe, new ExtendedStatus(HttpServletResponse.SC_FORBIDDEN));
            }

        }
        return entries;
    }


    /**
     * @param node
     * @param pe
     * @return
     */
    private ExtendedStatus removeProperty ( DAVTreeNode<EntityKey> node, DavPropertyName pe ) {
        try {
            if ( node instanceof EntityDAVNode ) {
                EntityDAVNode en = (EntityDAVNode) node;
                if ( !en.isCollection() && pe.equals(Constants.EXPIRES_PROP) ) {
                    this.entityService.setExpirationDate(en.getId(), null);
                }
            }
        }
        catch ( Exception e ) {
            return toStatus(e);
        }

        return new ExtendedStatus(HttpServletResponse.SC_FORBIDDEN);
    }


    /**
     * @param node
     * @param pe
     * @return
     */
    protected ExtendedStatus alterProperty ( DAVTreeNode<EntityKey> node, DavProperty<?> pe ) {

        try {
            if ( node instanceof EntityDAVNode ) {
                EntityDAVNode en = (EntityDAVNode) node;

                try {
                    if ( !en.isCollection() && pe.getName().equals(Constants.EXPIRES_PROP) && pe.getValue() instanceof String ) {
                        String v = (String) pe.getValue();
                        DateTime expires = null;
                        if ( !StringUtils.isBlank(v) ) {
                            expires = new DateTime(HttpDateFormat.creationDateFormat().parse(v));
                        }
                        this.entityService.setExpirationDate(en.getId(), expires);
                        return ExtendedMultiStatusResponse.OK_STATUS;
                    }
                    else if ( pe.getName().equals(Constants.SECURITY_LABEL) && pe.getValue() instanceof String ) {
                        this.entityService.setSecurityLabel(en.getId(), (String) pe.getValue(), false);
                        return ExtendedMultiStatusResponse.OK_STATUS;
                    }
                    else if ( en.isCollection() && pe.getName().equals(Constants.ALLOW_FILE_OVERWRITE) ) {
                        boolean b = Boolean.parseBoolean((String) pe.getValue());
                        this.directoryService.setAllowFileOverwrite(en.getId(), b);
                        return ExtendedMultiStatusResponse.OK_STATUS;
                    }
                    else if ( en.isCollection() && pe.getName().equals(Constants.SEND_NOTIFICATIONS) ) {
                        boolean b = Boolean.parseBoolean((String) pe.getValue());
                        this.directoryService.setSendNotifications(en.getId(), b);
                        return ExtendedMultiStatusResponse.OK_STATUS;
                    }
                }
                catch (
                    IllegalArgumentException |
                    ParseException e ) {
                    return new ExtendedStatus(HttpServletResponse.SC_BAD_REQUEST);
                }

            }
        }
        catch ( Exception e ) {
            return toStatus(e);
        }

        return new ExtendedStatus(HttpServletResponse.SC_FORBIDDEN);
    }


    /**
     * @param e
     * @return
     */
    private ExtendedStatus toStatus ( Exception e ) {
        try {
            if ( e instanceof InconsistentSecurityLabelException ) {
                Map<String, Collection<EntityKey>> blockers = ( (InconsistentSecurityLabelException) e ).getBlockers();
                String label = ( (InconsistentSecurityLabelException) e ).getLabel();
                throw new DavException(
                    HttpServletResponse.SC_CONFLICT,
                    "Inconsistent security labels", //$NON-NLS-1$
                    e,
                    createInconsitentLabelError(label, blockers));
            }
            wrapException(e);
            return new ExtendedStatus(500);
        }
        catch ( DavException de ) {
            Element errorElem = null;
            try {
                errorElem = de.toXml(DomUtil.createDocument());
            }
            catch ( ParserConfigurationException e1 ) {
                log.error("Failed to create error element", e1); //$NON-NLS-1$
            }
            return new ExtendedStatus("HTTP/1.1", de.getErrorCode(), de.getStatusPhrase(), errorElem); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#create(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, java.lang.String, org.apache.jackrabbit.webdav.io.InputContext)
     */
    @Override
    public DAVTreeNode<EntityKey> create ( DAVTreeNode<EntityKey> parent, DAVTreeNode<EntityKey> toCreate, String name, InputContext input )
            throws DavException {

        try {
            HttpServletRequest req = unwrapRequest(input);
            HttpServletResponse resp = WebUtils.getHttpResponse(SecurityUtils.getSubject());
            checkStoreRequest(req);

            if ( toCreate instanceof OCChunkDAVNode ) {
                return uploadChunk((OCChunkDAVNode) toCreate, req, input);
            }

            if ( ! ( parent instanceof EntityDAVNode ) ) {
                throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }

            String lowerName = name.toLowerCase();
            if ( REJECT_FILES.contains(lowerName) ) {
                throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }

            for ( String prefix : REJECT_PREFIX ) {
                if ( lowerName.startsWith(prefix) ) {
                    throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            }

            EntityDAVNode en = (EntityDAVNode) parent;

            FileEntity f = new FileEntity();
            f.setLocalName(name);
            f.setContentType(input.getContentType());

            long contentLength = getContentLength(input);

            f.setFileSize(contentLength);

            if ( log.isDebugEnabled() ) {
                log.debug("File size is " + f.getFileSize()); //$NON-NLS-1$
            }

            VFSFileEntity create = this.ctx.getUploadService().create(parent.getId(), f, Channels.newChannel(req.getInputStream()), req, resp);
            if ( create == null ) {
                throw new DavException(HttpServletResponse.SC_ACCEPTED, "Incomplete upload"); //$NON-NLS-1$
            }
            setNoEntityReponseHeaders(create);
            return adapt(create, en.getPermissions(), en.isShared(), getLayout(req), en.getGrantId(), parent.getId());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException |
            IOException e ) {
            wrapException(e);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#update(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      org.apache.jackrabbit.webdav.io.InputContext)
     */
    @Override
    public DAVTreeNode<EntityKey> update ( DAVTreeNode<EntityKey> node, InputContext input ) throws DavException {

        try {
            HttpServletRequest req = unwrapRequest(input);
            HttpServletResponse resp = WebUtils.getHttpResponse(SecurityUtils.getSubject());
            checkStoreRequest(req);

            if ( node instanceof OCChunkDAVNode ) {
                return uploadChunk((OCChunkDAVNode) node, req, input);
            }

            if ( ! ( node instanceof EntityDAVNode ) ) {
                throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }

            EntityDAVNode en = (EntityDAVNode) node;
            if ( ! ( en.getEntity() instanceof FileEntity ) ) {
                throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }

            FileEntity f = new FileEntity();
            f.setContentType(input.getContentType());

            long contentLength = getContentLength(input);
            f.setFileSize(contentLength);

            try ( VFSContext vc = this.getVfs().getVFS(node.getId()).begin(true) ) {
                VFSFileEntity replaceFile = this.ctx.getUploadService()
                        .replaceFile(node.getId(), f, Channels.newChannel(req.getInputStream()), req, resp);
                if ( replaceFile == null ) {
                    throw new DavException(HttpServletResponse.SC_ACCEPTED, "Incomplete upload"); //$NON-NLS-1$
                }

                setNoEntityReponseHeaders(replaceFile);
                return adapt(vc, replaceFile, en.getPermissions(), en.isShared(), getLayout(req), en.getGrantId());
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException |
            IOException e ) {
            wrapException(e);
            return null;
        }

    }


    /**
     * 
     * Special handling of MacOSX finder, sending chunked encoded data.
     * 
     * @param input
     * @return request content length
     */
    private static long getContentLength ( InputContext input ) {
        String te = input.getProperty("Transfer-Encoding"); //$NON-NLS-1$
        String finderLength = input.getProperty("X-Expected-Entity-Length"); //$NON-NLS-1$
        long contentLength = input.getContentLength();
        if ( contentLength < 0 && "chunked".equalsIgnoreCase(te) && !StringUtils.isBlank(finderLength) ) { //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Have finder length indicator " + finderLength); //$NON-NLS-1$
            }
            return Long.parseLong(finderLength);
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Have content length " + contentLength); //$NON-NLS-1$
        }
        return contentLength;
    }


    /**
     * @param replaceFile
     */
    private static void setNoEntityReponseHeaders ( VFSFileEntity replaceFile ) {
        HttpServletResponse resp = WebUtils.getHttpResponse(SecurityUtils.getSubject());
        resp.setHeader("X-Entity-Content-Length", String.valueOf(replaceFile.getFileSize())); //$NON-NLS-1$
        resp.setHeader("X-Entity-Content-Type", String.valueOf(replaceFile.getContentType())); //$NON-NLS-1$
        if ( !StringUtils.isBlank(replaceFile.getContentEncoding()) ) {
            resp.setHeader("X-Entity-Content-Encoding", String.valueOf(replaceFile.getContentEncoding())); //$NON-NLS-1$
        }
    }


    static DAVLayout getLayout ( HttpServletRequest req ) {
        String layout = (String) req.getAttribute(Constants.LAYOUT);
        if ( layout == null ) {
            return DAVLayout.REGULAR;
        }
        return DAVLayout.valueOf(layout);
    }


    /**
     * @param req
     * @throws DavException
     */
    private static void checkStoreRequest ( HttpServletRequest req ) throws DavException {
        Enumeration<String> headers = req.getHeaderNames();

        while ( headers.hasMoreElements() ) {
            String header = headers.nextElement().toLowerCase();

            if ( header.startsWith("content-") && !PUT_CONTENT_SUPPORTED.contains(header) ) { //$NON-NLS-1$
                throw new DavException(HttpServletResponse.SC_NOT_IMPLEMENTED);
            }
        }
    }


    /**
     * @param parent
     * @param node
     * @param parent
     * @param input
     * @return
     * @throws FileshareException
     */
    protected DAVTreeNode<EntityKey> uploadChunk ( OCChunkDAVNode node, HttpServletRequest req, InputContext input )
            throws FileshareException, IOException {
        String token = node.getTarget().getEntityKey() + String.valueOf(node.getTs());
        ChunkContext chunkContext = this.ctx.getChunkUploadService().getChunkContext(token);
        HttpServletResponse resp = WebUtils.getHttpResponse(SecurityUtils.getSubject());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Storing chunk %d/%d of %s size: %d (%s)", //$NON-NLS-1$
                node.getChunkIdx(),
                node.getChunkNum(),
                node.getLocalName(),
                input.getContentLength(),
                input.getContentType()));
        }
        chunkContext.storeChunk(node.getChunkIdx(), Channels.newChannel(input.getInputStream()), input.getContentLength());

        if ( chunkContext.isComplete() ) {
            log.debug("All chunks are complete, creating real file"); //$NON-NLS-1$

            FileEntity f = new FileEntity();
            f.setContentType(input.getContentType());
            f.setLocalName(node.getLocalName());
            return createFromChunks(node, req, resp, input, f, chunkContext);
        }

        return node;
    }


    /**
     * @param node
     * @param parent
     * @param input
     * @param f
     * @param chunkContext
     * @return
     * @throws FileshareException
     * @throws IOException
     */
    private DAVTreeNode<EntityKey> createFromChunks ( OCChunkDAVNode node, HttpServletRequest req, HttpServletResponse resp, InputContext input,
            VFSFileEntity f, ChunkContext chunkContext ) throws FileshareException, IOException {
        VFSFileEntity created = this.ctx.getUploadService().retry(node.getId(), f, chunkContext, req, resp);
        OutputContext out = new OutputContextImpl(resp, resp.getOutputStream());
        DefaultDavResourceFactory.setResourceHeaders(
            out,
            adapt(created, node.getPermissions(), node.isShared(), DAVLayout.OWNCLOUD, node.getGrantId(), node.getParent().getEntityKey()),
            false);
        try ( OutputStream os = out.getOutputStream() ) {
            os.write("{ }".getBytes()); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getAcceptablePatchFormats(eu.agno3.runtime.webdav.server.DAVTreeNode)
     */
    @Override
    public Collection<String> getAcceptablePatchFormats ( DAVTreeNode<EntityKey> wrapped ) {
        return null;
    }


    @Override
    public void patch ( DAVTreeNode<EntityKey> wrapped, InputContext inputContext ) throws DavException, IOException {
        throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }


    @Override
    public Report getReport ( DAVTreeNode<EntityKey> root, ReportInfo report, Report res ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("Unhandled report " + report.getReportName()); //$NON-NLS-1$
        }
        throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY);
    }


    @Override
    public Map<DAVTreeNode<EntityKey>, Status> findPrincipalMatch ( DAVTreeNode<EntityKey> wrapped, PrincipalMatchReport pm ) throws DavException {

        // principal match has two meanings:
        // - on a collection of principals identify the principals that match the current user
        // - on other collections recursively identify resources that are owned by the user.
        // I'm not sure we want to support the second kind as it's effectively a infinity depth propfind in our case

        if ( log.isDebugEnabled() ) {
            log.debug("Princ match report " + pm.getPrincipalPropertyName()); //$NON-NLS-1$
        }
        HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());
        DAVLayout layout = getLayout(req);
        for ( SubtreeProvider sp : this.providers ) {
            if ( sp.handlesChildren(wrapped, layout) ) {
                try {
                    Map<DAVTreeNode<EntityKey>, Status> res = sp.findPrincipalMatch(this, wrapped, layout, pm);
                    if ( res != null ) {
                        return res;
                    }
                }
                catch ( FileshareException e ) {
                    wrapException(e);
                }
            }
        }

        throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY);
    }


    @Override
    public Map<DAVTreeNode<EntityKey>, Status> findPrincipalSearch ( DAVTreeNode<EntityKey> wrapped, FixedPrincipalSearchReport ps )
            throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("Princ search report " + ps); //$NON-NLS-1$
        }
        HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());
        DAVLayout layout = getLayout(req);
        for ( SubtreeProvider sp : this.providers ) {
            try {
                Map<DAVTreeNode<EntityKey>, Status> res = sp.searchPrincipal(this, wrapped, layout, ps);
                if ( res != null ) {
                    return res;
                }
            }
            catch ( FileshareException e ) {
                wrapException(e);
            }
        }
        throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getSupportedPrincipalSearchProperties(eu.agno3.runtime.webdav.server.DAVTreeNode)
     */
    @Override
    public Collection<PrincipalSearchProperty> getSupportedPrincipalSearchProperties ( DAVTreeNode<EntityKey> wrapped ) {
        return Collections.singleton(new PrincipalSearchProperty(DavPropertyName.DISPLAYNAME, "Name")); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#syncCollectionStream(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      eu.agno3.runtime.webdav.server.colsync.ColSyncReport)
     */
    @Override
    public void syncCollectionStream ( DAVTreeNode<EntityKey> wrapped, ColSyncReport<EntityKey> cs ) throws DavException, IOException {
        DAVLayout layout = getLayout(WebUtils.getHttpRequest(SecurityUtils.getSubject()));
        long start = System.currentTimeMillis();

        if ( layout != DAVLayout.NATIVE ) {
            throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY);
        }

        if ( cs.getLimit() >= 0 ) {
            throw new DavException(
                DavServletResponse.SC_INSUFFICIENT_SPACE_ON_RESOURCE,
                "Limit not supported", //$NON-NLS-1$
                null,
                createError(DavConstants.NAMESPACE, "number-of-matches-within-limits")); //$NON-NLS-1$
        }

        String token = cs.getSyncToken();
        SyncTokenData inputToken = null;
        SyncTokenData outputToken = new SyncTokenData();
        if ( !StringUtils.isBlank(token) ) {
            try {
                inputToken = SyncTokenData.parse(token);
                // make sure we never run back in time
                outputToken.setLastModified(inputToken.getLastModified());
            }
            catch ( InvalidSyncTokenException e ) {
                throw new DavException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Sync token is invalid", //$NON-NLS-1$
                    e,
                    createError(DavConstants.NAMESPACE, "valid-sync-token")); //$NON-NLS-1$
            }
            if ( log.isTraceEnabled() ) {
                log.trace("Got token " + token); //$NON-NLS-1$
            }
        }

        try {
            DAVTreeNode<EntityKey> rootNode = wrapped;
            UUID rootId = null;

            if ( rootNode.getId() instanceof NativeEntityKey ) {
                rootId = ( (NativeEntityKey) rootNode.getId() ).getId();
            }

            outputToken.setRootId(rootId);

            boolean rootModified = inputToken != null && !Objects.equals(rootId, inputToken.getRootId());
            if ( rootModified ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Root has been updated " + rootId); //$NON-NLS-1$
                }
                throw new DavException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Sync token refers to other root", //$NON-NLS-1$
                    null,
                    createError(DavConstants.NAMESPACE, "valid-sync-token")); //$NON-NLS-1$
            }

            boolean handled = false;

            if ( rootNode instanceof RootDAVNode && ( inputToken == null
                    || ( rootNode.getModificationTime() != null && rootNode.getModificationTime().isAfter(inputToken.getLastModified()) )
                    || rootModified ) ) {
                cs.emit200(rootNode);
                handled = true;
            }

            for ( SubtreeProvider subtreeProvider : this.providers ) {
                handled |= subtreeProvider.contributeChanges(this, rootNode, inputToken, outputToken, cs, layout, rootModified);
            }

            if ( !handled ) {
                throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY);
            }

            outputToken.setLastSync(start);
            cs.setResponseSyncToken(outputToken.marshall());
            if ( log.isTraceEnabled() ) {
                log.trace("Returning token " + cs.getResponseSyncToken()); //$NON-NLS-1$
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Sync took %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
            }
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            wrapException(e);
            return;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#getQueryGrammarSet(eu.agno3.runtime.webdav.server.DAVTreeNode)
     */
    @Override
    public QueryGrammerSet getQueryGrammarSet ( DAVTreeNode<EntityKey> root ) {
        return new QueryGrammerSet();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DavException
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeProvider#search(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      org.apache.jackrabbit.webdav.search.SearchInfo)
     */
    @Override
    public MultiStatus search ( DAVTreeNode<EntityKey> root, SearchInfo search ) throws DavException {
        throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }


    /**
     * @param e
     * @throws DavException
     */
    static void wrapException ( Exception e ) throws DavException {
        if ( e instanceof FileshareException ) {
            wrapFileshareException((FileshareException) e);
        }
        else if ( e instanceof UndeclaredThrowableException && e.getCause() instanceof InvocationTargetException
                && e.getCause().getCause() instanceof FileshareException ) {
            wrapFileshareException((FileshareException) e.getCause().getCause());
        }
        else if ( e instanceof IllegalArgumentException ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, e);
        }
        log.warn("Unhandled exception", e); //$NON-NLS-1$
        throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
    }


    /**
     * @param e
     * @throws DavException
     */
    private static void wrapFileshareException ( FileshareException e ) throws DavException {
        if ( e instanceof EntityNotFoundException || e instanceof GroupNotFoundException || e instanceof UserNotFoundException ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }
        else if ( e instanceof InvalidSyncTokenException ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
        }
        else if ( e instanceof AccessDeniedException ) {
            throw new DavException(HttpServletResponse.SC_FORBIDDEN);
        }
        else if ( e instanceof CannotShareToSelfException ) {
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Cannot share to self", //$NON-NLS-1$
                e,
                createError(Constants.AGNO3_NS, "cannot-share-to-self")); //$NON-NLS-1$
        }
        else if ( e instanceof NotificationException ) {
            throw new DavException(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Sending notification failed", //$NON-NLS-1$
                e,
                createError(Constants.AGNO3_NS, "notify-failure")); //$NON-NLS-1$
        }
        else if ( e instanceof QuotaExceededException ) {
            throw new DavException(DavServletResponse.SC_INSUFFICIENT_SPACE_ON_RESOURCE);
        }
        else if ( e instanceof PolicyNotFulfilledException ) {

            if ( ( (PolicyNotFulfilledException) e ).getViolation() != null ) {
                throw new DavException(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Insufficent token password", //$NON-NLS-1$
                    e,
                    createError(Constants.AGNO3_NS, ( (PolicyNotFulfilledException) e ).getViolation().getKey())); // $NON-NLS-1$

            }
            throw new DavException(HttpServletResponse.SC_FORBIDDEN, "Policy not fulfilled"); //$NON-NLS-1$

        }
        else if ( e instanceof GrantExistsException ) {
            throw new DavException(
                HttpServletResponse.SC_CONFLICT,
                "Grant exists", //$NON-NLS-1$
                e,
                createError(Constants.AGNO3_NS, "grant-exists")); //$NON-NLS-1$
        }
        else if ( e instanceof UserNotFoundException ) {
            throw new DavException(HttpServletResponse.SC_FORBIDDEN);
        }
        else if ( e instanceof ContentVirusException ) {
            Element error = createError(Constants.AGNO3_NS, "content-filter-reject"); //$NON-NLS-1$
            error.setTextContent( ( (ContentVirusException) e ).getSignature());
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Content scanner rejected", e, error); //$NON-NLS-1$
        }
        else if ( e instanceof MailingDisabledException ) {
            throw new DavException(
                HttpServletResponse.SC_FORBIDDEN,
                "Cannot send mail", //$NON-NLS-1$
                e,
                createError(Constants.AGNO3_NS, "mailing-disabled")); //$NON-NLS-1$
        }
        else if ( e instanceof EntityExpirationInvalidException ) {
            throw new DavException(
                HttpServletResponse.SC_BAD_REQUEST,
                "Illegal expiration date", //$NON-NLS-1$
                e,
                createError(Constants.AGNO3_NS, "expiration-invalid")); //$NON-NLS-1$
        }
        else if ( e instanceof UploadException ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    private Element createInconsitentLabelError ( String targetLabel, Map<String, Collection<EntityKey>> blockers ) {
        try {
            Document doc = DomUtil.createDocument();
            String ns = Constants.AGNO3_NS.getURI();
            Element root = doc.createElementNS(ns, "inconsistent-label"); //$NON-NLS-1$
            root.setAttribute("target", targetLabel); //$NON-NLS-1$

            for ( Entry<String, Collection<EntityKey>> blocker : blockers.entrySet() ) {

                int rel = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().compareLabels(targetLabel, blocker.getKey());
                if ( rel == 0 ) {
                    // should not happend
                    continue;
                }

                Element blockerElem = doc.createElementNS(ns, "blocker"); //$NON-NLS-1$
                blockerElem.setPrefix(root.getPrefix());
                blockerElem.setAttribute("label", blocker.getKey()); //$NON-NLS-1$
                blockerElem.setAttribute("count", String.valueOf(blocker.getValue().size())); //$NON-NLS-1$

                blockerElem.setAttribute(
                    "type", //$NON-NLS-1$
                    rel < 0 ? "aboveTarget" //$NON-NLS-1$
                            : "belowTarget"); //$NON-NLS-1$

                root.appendChild(blockerElem);
            }

            return root;
        }
        catch (
            DOMException |
            ParserConfigurationException e ) {
            log.warn("Failed to create error response XML", e); //$NON-NLS-1$
            return null;
        }
    }


    @Override
    public GuiServiceContext getContext () {
        return this.ctx;
    }


    @Override
    public VFSServiceInternal getVfs () {
        return this.vfs;
    }


    @Override
    public QuotaService getQuotaService () {
        return this.quotaService;
    }


    @Override
    public PolicyEvaluator getPolicyEvaluator () {
        return this.policyEvaluator;
    }


    @Override
    public BrowseServiceInternal getBrowseService () {
        return this.browseService;
    }


    @Override
    public EntityServiceInternal getEntityService () {
        return this.entityService;
    }


    @Override
    public AccessControlService getAccessControl () {
        return this.accessControl;
    }

}
