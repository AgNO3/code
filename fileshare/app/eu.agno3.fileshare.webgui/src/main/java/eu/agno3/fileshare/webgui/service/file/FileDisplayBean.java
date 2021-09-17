/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.info.MimeTypeBean;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.AllSharedTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EmptyDirectoryTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FavoritesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.GroupTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharedByTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharedToTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeersTreeNode;
import eu.agno3.fileshare.webgui.service.tree.RootTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SearchResultTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareSubjectTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SharedTreeNode;
import eu.agno3.fileshare.webgui.subject.SubjectDisplayBean;
import eu.agno3.runtime.jsf.prefs.AbstractLocaleSettingsBean;


/**
 * @author mbechler
 *
 */
@Named ( "fileDisplayBean" )
@ApplicationScoped
public class FileDisplayBean {

    /**
     * 
     */
    private static final String DROP_TARGET_CLASS = "drop-target"; //$NON-NLS-1$
    private static final String NO_SELECT_CLASS = "no-select"; //$NON-NLS-1$
    private static final String USER_ROOT_ICON = "ui-icon-home"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String DEFAULT_FILE_ICON_CLASS = "ui-icon-document"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileDisplayBean.class);
    private static final String GROUP_ROOT_ICON = "ui-icon-group"; //$NON-NLS-1$
    private static final ReadableDuration EXPIRY_WARN_DURATION = Duration.standardDays(7);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private AbstractLocaleSettingsBean localeSettings;

    @Inject
    private SubjectDisplayBean subjectDisplay;


    /**
     * 
     * @param o
     * @return extracts the entity key from an object
     */
    public static String getEntityId ( Object o ) {

        if ( o == null ) {
            return null;
        }

        if ( o instanceof VFSEntity ) {
            return ( (VFSEntity) o ).getEntityKey().toString();
        }
        else if ( o instanceof SubjectInfo ) {
            return ( (SubjectInfo) o ).getId().toString();
        }
        else if ( o instanceof PeerInfo ) {
            return ( (PeerInfo) o ).getId().toString();
        }

        throw new IllegalArgumentException("Unsupported type " + o); //$NON-NLS-1$
    }


    /**
     * 
     * @param e
     * @return whether this is a directory
     */
    public static boolean isDirectory ( VFSEntity e ) {
        return e instanceof VFSContainerEntity;
    }


    /**
     * 
     * @param e
     * @return whether this is an element of a virtual group
     */
    public static boolean isVirtualGroup ( VFSEntity e ) {
        return e != null && e.getOwner() instanceof VirtualGroup;
    }


    /**
     * @param n
     * @return whether this is a entity node that represents a hierarchy root
     */
    public static boolean isRootNode ( TreeNode n ) {
        if ( ! ( n instanceof EntityTreeNode ) ) {
            return false;
        }

        EntityTreeNode en = (EntityTreeNode) n;

        return !en.getAttachedObject().hasParent();
    }


    /**
     * @param n
     * @return whether this node is below a share root
     */
    public static boolean isSharedNode ( TreeNode n ) {
        if ( ! ( n instanceof EntityTreeNode ) ) {
            return false;
        }

        EntityTreeNode en = (EntityTreeNode) n;

        Grant g = en.getGrant();

        if ( g == null ) {
            return false;
        }

        return FileTreeConstants.SHARE_ROOT_TYPE.equals(en.getRootType()) || FileTreeConstants.SHARE_ROOT_FILE_TYPE.equals(en.getRootType());
    }


    /**
     * 
     * @param n
     * @return whether this node represents a directory
     */
    public static boolean isDirectoryNode ( TreeNode n ) {

        if ( ! ( n instanceof EntityTreeNode ) ) {
            return false;
        }

        EntityTreeNode en = (EntityTreeNode) n;

        return isDirectory(en.getAttachedObject());
    }


    /**
     * 
     * @param n
     * @return whether this node represents a directory or a fake empty dir entry
     */
    public static boolean isDirectoryOrEmptyDirNode ( TreeNode n ) {
        return n instanceof EmptyDirectoryTreeNode || isDirectoryNode(n);
    }


    /**
     * 
     * @param e
     * @return whether this is a file
     */
    public static boolean isFile ( VFSEntity e ) {
        return e instanceof VFSFileEntity;
    }


    /**
     * @param n
     * @return whether this node represents a file
     */
    public static boolean isFileNode ( TreeNode n ) {
        if ( ! ( n instanceof EntityTreeNode ) ) {
            return false;
        }

        EntityTreeNode en = (EntityTreeNode) n;

        return isFile(en.getAttachedObject());
    }


    /**
     * 
     * @param n
     * @return whether this is a entity node
     */
    public static boolean isEntityNode ( TreeNode n ) {
        if ( ! ( n instanceof EntityTreeNode ) ) {
            return false;
        }

        return true;
    }


    /**
     * 
     * @param o
     * @return whether this is a entity
     */
    public static boolean isEntity ( Object o ) {
        return o instanceof VFSEntity;
    }


    /**
     * 
     * @param n
     * @return whether this is a node that has a subject attached
     */
    public static boolean isSubjectNode ( TreeNode n ) {
        return n != null
                && ( FileTreeConstants.GROUP_ROOT_TYPE.equals(n.getType()) || FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE.equals(n.getType()) );
    }


    /**
     * 
     * @param n
     * @return whether this is a node that has a peer attached
     */
    public static boolean isPeerNode ( TreeNode n ) {
        return n != null && FileTreeConstants.PEER_ROOT_TYPE.equals(n.getType());
    }


    /**
     * 
     * @param n
     * @return the assoicated subject for the tree node
     */
    public static SubjectInfo getSubject ( TreeNode n ) {
        if ( n instanceof EntityTreeNode ) {
            EntityTreeNode en = (EntityTreeNode) n;
            if ( !en.getAttachedObject().hasParent() ) {
                return en.getAttachedObject().getOwner();
            }
        }
        else if ( n instanceof ShareSubjectTreeNode ) {
            ShareSubjectTreeNode sn = (ShareSubjectTreeNode) n;
            return sn.getAttachedObject();
        }

        return null;
    }


    /**
     * 
     * @param n
     * @return the attached peer
     */
    public static PeerInfo getPeer ( TreeNode n ) {
        if ( n instanceof PeerSharesTreeNode ) {
            PeerSharesTreeNode pn = (PeerSharesTreeNode) n;
            return pn.getAttachedObject();
        }
        return null;
    }


    /**
     * @param e
     * @return the full (visible) encoded path to the entity
     */
    public String getFullPath ( VFSEntity e ) {
        if ( e == null ) {
            return null;
        }
        try {
            List<String> segments = this.fsp.getEntityService().getFullPath(e.getEntityKey());
            return "/" + StringUtils.join(segments, '/'); //$NON-NLS-1$
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            log.warn("Failed to get entity path to " + e, ex); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * 
     * @param bt
     * @return an extra style class for the file name display
     */
    public static String getFileNameStyleClass ( BrowseTreeNode bt ) {
        return getExpirationStyleClass(bt);
    }


    /**
     * @param bt
     * @return ui-state-error if the entity expires soon
     */
    public static String getExpirationStyleClass ( BrowseTreeNode bt ) {
        if ( expiresSoon(bt) ) {
            return "ui-state-error"; //$NON-NLS-1$
        }

        return StringUtils.EMPTY;
    }


    /**
     * @param bt
     * @return whether the entity expires soon
     */
    public static boolean expiresSoon ( BrowseTreeNode bt ) {
        if ( ! ( bt instanceof EntityTreeNode ) ) {
            return false;
        }
        EntityTreeNode et = (EntityTreeNode) bt;
        VFSEntity e = et.getAttachedObject();

        if ( e == null || e.getExpires() == null ) {
            return false;
        }

        return e.getExpires().isBefore(DateTime.now().plus(EXPIRY_WARN_DURATION));
    }


    /**
     * 
     * @param bt
     * @return the formatted remaining expiry time
     */
    public String getRemainingExpiryTime ( BrowseTreeNode bt ) {
        if ( ! ( bt instanceof EntityTreeNode ) ) {
            return StringUtils.EMPTY;
        }
        EntityTreeNode et = (EntityTreeNode) bt;
        VFSEntity e = et.getAttachedObject();

        if ( e == null || e.getExpires() == null ) {
            return StringUtils.EMPTY;
        }

        return this.localeSettings.formatDateTimeRelative(e.getExpires());
    }


    /**
     * @param e
     * @return the entity display local name
     */
    public static String getLocalDisplayName ( VFSEntity e ) {
        if ( e != null && !StringUtils.isBlank(e.getLocalName()) ) {
            return e.getLocalName();
        }
        else if ( e != null ) {
            if ( e.getOwner() instanceof Group ) {
                return FileshareMessages.format("target.group-root-fmt", ( (Group) e.getOwner() ).getName()); //$NON-NLS-1$
            }

            return FileshareMessages.get("target.user-root"); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * 
     * @param n
     * @return the security label string for the node
     */
    public static String getLabelString ( BrowseTreeNode n ) {
        VFSEntity en = getAttachedEntity(n);
        if ( en != null ) {
            SecurityLabel label = en.getSecurityLabel();
            if ( label == null ) {
                return null;
            }

            return label.getLabel();
        }
        return null;
    }


    /**
     * @param n
     * @return
     */
    private static VFSEntity getAttachedEntity ( BrowseTreeNode n ) {
        if ( n instanceof EntityTreeNode ) {
            return ( (EntityTreeNode) n ).getAttachedObject();
        }
        else if ( n instanceof EmptyDirectoryTreeNode ) {
            return (VFSEntity) ( (EmptyDirectoryTreeNode) n ).getAttachedObject();
        }
        return null;
    }


    /**
     * @param n
     * @return the grant id for this node
     */
    public static String getGrantId ( BrowseTreeNode n ) {
        if ( n instanceof EntityTreeNode ) {
            Grant g = ( (EntityTreeNode) n ).getGrant();

            if ( g != null ) {
                return g.getId().toString();
            }
        }
        else if ( n instanceof EmptyDirectoryTreeNode ) {

            if ( n.getParent() instanceof EntityTreeNode ) {
                Grant g = ( (EntityTreeNode) n.getParent() ).getGrant();

                if ( g != null ) {
                    return g.getId().toString();
                }
            }
        }
        return null;
    }


    /**
     * @param e
     * @return type icon class for the entity
     */
    public static String getTypeIconCollapsed ( VFSEntity e ) {
        if ( e instanceof VFSFileEntity ) {
            return getViewIconClass(e);
        }
        else if ( e instanceof VFSContainerEntity ) {
            return "ui-icon-folder-collapsed"; //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param e
     * @return type icon class for the entity
     */
    public static String getTypeIconExpanded ( VFSEntity e ) {
        if ( e instanceof VFSFileEntity ) {
            return getViewIconClass(e);
        }
        else if ( e instanceof VFSContainerEntity ) {
            return "ui-icon-folder-open"; //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param f
     * @return the file type icon class
     */
    public static String getViewIconClass ( Object f ) {

        if ( ! ( f instanceof VFSFileEntity ) || StringUtils.isBlank( ( (VFSFileEntity) f ).getContentType()) ) {
            return DEFAULT_FILE_ICON_CLASS;
        }

        String mimeType = ( (VFSFileEntity) f ).getContentType();

        if ( mimeType.startsWith("image/") ) { //$NON-NLS-1$
            return "ui-icon-image"; //$NON-NLS-1$
        }
        else if ( mimeType.startsWith("video/") ) { //$NON-NLS-1$
            return "ui-icon-video"; //$NON-NLS-1$
        }
        else if ( mimeType.startsWith("text/") ) { //$NON-NLS-1$
            return "ui-icon-script"; //$NON-NLS-1$
        }
        else if ( mimeType.startsWith("audio/") ) { //$NON-NLS-1$
            return "ui-icon-volume-on"; //$NON-NLS-1$
        }

        return DEFAULT_FILE_ICON_CLASS;
    }


    /**
     * 
     * @param n
     * @return the entity type to display
     */
    public static String getDisplayEntityType ( BrowseTreeNode n ) {
        switch ( n.getType() ) {
        case FileTreeConstants.FILE_TYPE:
            EntityTreeNode e = (EntityTreeNode) n;
            VFSFileEntity f = (VFSFileEntity) e.getAttachedObject();
            return MimeTypeBean.getDisplayType(f.getContentType());
        case FileTreeConstants.DIR_TYPE:
            return FileshareMessages.get(FileshareMessages.DIRECTORY_TYPE_DISPLAY_NAME);
        case FileTreeConstants.USER_ROOT_TYPE:
            return FileshareMessages.get(FileshareMessages.USER_ROOT_TYPE_DISPLAY_NAME);
        case FileTreeConstants.GROUP_ROOT_TYPE:
            return FileshareMessages.get(FileshareMessages.GROUP_ROOT_TYPE_DISPLAY_NAME);
        case FileTreeConstants.SHARE_ROOT_TYPE:
        case FileTreeConstants.SHARE_ROOT_FILE_TYPE:
            return getShareDisplayType(n);
        case FileTreeConstants.SHARED_ROOT_TYPE:
            return FileshareMessages.get(FileshareMessages.SHARED_DISPLAY_NAME);
        case FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE:
            return SubjectDisplayBean.getSubjectType( ( (ShareSubjectTreeNode) n ).getAttachedObject());
        case FileTreeConstants.PEER_ROOT_TYPE:
            return SubjectDisplayBean.getPeerType( ( (PeerSharesTreeNode) n ).getAttachedObject());
        default:
            return null;
        }
    }


    /**
     * @param n
     * @return
     */
    private static String getShareDisplayType ( BrowseTreeNode n ) {
        EntityTreeNode se = (EntityTreeNode) n;
        VFSEntity sf = se.getAttachedObject();
        if ( sf instanceof VFSFileEntity ) {
            // TODO: readd permission indication
            return FileshareMessages.get(FileshareMessages.SHARE_TYPE_FILE_DISPLAY_NAME);

        }
        else if ( sf instanceof VFSContainerEntity ) {
            // TODO: readd permission indication
            return FileshareMessages.get(FileshareMessages.SHARE_TYPE_DIRECTORY_DISPLAY_NAME);
        }
        return null;
    }


    /**
     * @param n
     * @return the display name truncated to 80 characters
     */
    public String getTruncatedEntityLocalDisplayName ( BrowseTreeNode n ) {
        String res = getEntityLocalDisplayName(n);

        if ( res == null ) {
            return res;
        }

        return smartTruncateName(res, 32);
    }


    /**
     * @param res
     * @param maxLength
     * @return truncated name
     */
    public static String smartTruncateName ( String res, int maxLength ) {
        int extSep = res.lastIndexOf('.');
        String ext = extSep >= 0 ? res.substring(extSep) : StringUtils.EMPTY;
        if ( StringUtils.isBlank(res) || res.length() < maxLength ) {
            return res;
        }
        return res.substring(0, maxLength - 3 - ext.length()) + "..." + ext; //$NON-NLS-1$
    }


    /**
     * @param n
     * @return a display name for a virtual empty node
     */
    public static String getEmptyNodeDisplayName ( BrowseTreeNode n ) {

        if ( n == null ) {
            return null;
        }

        String type = n.getType();

        switch ( type ) {
        case FileTreeConstants.EMPTY_DIR_TYPE:
            if ( n.hasPermission(GrantPermission.UPLOAD) || n.hasPermission(GrantPermission.EDIT) || n.hasPermission(GrantPermission.EDIT_SELF) ) {
                return FileshareMessages.get(FileshareMessages.EMPTY_DIRECTORY_WRITEABLE_MESSAGE);
            }
            return FileshareMessages.get(FileshareMessages.EMPTY_DIRECTORY_READONLY_MESSAGE);
        case FileTreeConstants.EMPTY_GROUPS_TYPE:
            return FileshareMessages.get(FileshareMessages.EMPTY_GROUPS_MESSAGE);
        case FileTreeConstants.EMPTY_SHARES_TYPE:
            return FileshareMessages.get(FileshareMessages.EMPTY_SHARES_MESSAGE);
        case FileTreeConstants.EMPTY_FAVS_TYPE:
            return FileshareMessages.get(FileshareMessages.EMPTY_FAVORITES_MESSAGE);
        case FileTreeConstants.EMPTY_SEARCH_RESULT_TYPE:
            return FileshareMessages.get(FileshareMessages.EMPTY_SEARCH_RESULT_MESSAGE);
        case FileTreeConstants.INVALID_QUERY_TREE_NODE:
            return FileshareMessages.get(FileshareMessages.INVALID_QUERY_MESSAGE);
        }
        return null;
    }


    /**
     * 
     * @param n
     * @return the name of the entity to display
     */
    public String getEntityLocalDisplayName ( BrowseTreeNode n ) {
        if ( n instanceof EntityTreeNode || n instanceof EmptyDirectoryTreeNode ) {
            AbstractBrowseTreeNode en = (AbstractBrowseTreeNode) n;

            VFSEntity entity = (VFSEntity) en.getAttachedObject();
            if ( FileTreeConstants.USER_ROOT_TYPE.equals(n.getType()) ) {
                return FileshareMessages.get(FileshareMessages.USER_ROOT_DISPLAY_NAME);
            }
            else if ( FileTreeConstants.GROUP_ROOT_TYPE.equals(n.getType()) ) {
                if ( entity != null && entity.getOwner() instanceof Group ) {
                    return this.subjectDisplay.getSubjectDisplayName(entity.getOwner());
                }
                return null;
            }

            if ( entity != null ) {
                return entity.getLocalName();
            }
            return null;
        }

        return getVirtualDisplayName(n);
    }


    /**
     * @param n
     * @return
     */
    private String getVirtualDisplayName ( BrowseTreeNode n ) {
        if ( n instanceof GroupTreeNode ) {
            return FileshareMessages.get(FileshareMessages.GROUPS_ROOT_DISPLAY_NAME);
        }

        if ( n instanceof SharedTreeNode ) {
            return FileshareMessages.get(FileshareMessages.SHARED_DISPLAY_NAME);
        }

        if ( n instanceof PeersTreeNode ) {
            return FileshareMessages.get(FileshareMessages.PEERS_ROOT_DISPLAY_NAME);
        }

        if ( n instanceof FavoritesTreeNode ) {
            return FileshareMessages.get(FileshareMessages.FAVORITES_ROOT_DISPLAY_NAME);
        }

        if ( n instanceof AllSharedTreeNode ) {
            return FileshareMessages.get(FileshareMessages.ALL_SHARED_DISPLAY_NAME);
        }

        if ( n instanceof SearchResultTreeNode ) {
            return FileshareMessages.get(FileshareMessages.SEARCH_RESULT_DISPLAY_NAME);
        }

        if ( n instanceof RootTreeNode ) {
            return FileshareMessages.get(FileshareMessages.ROOT_DISPLAY_NAME);
        }

        if ( n instanceof ShareSubjectTreeNode ) {
            return this.subjectDisplay.getSubjectDisplayName( ( (ShareSubjectTreeNode) n ).getAttachedObject());
        }
        else if ( n instanceof PeerSharesTreeNode ) {
            return this.subjectDisplay.getPeerDisplayName( ( (PeerSharesTreeNode) n ).getAttachedObject());
        }

        return null;
    }


    /**
     * 
     * @param n
     * @return the label node text
     */
    public static String getLabelNodeDisplayName ( BrowseTreeNode n ) {
        if ( n instanceof PeerSharedByTreeNode ) {
            return FileshareMessages.get(FileshareMessages.PEER_SHARED_BY_TITLE);
        }
        else if ( n instanceof PeerSharedToTreeNode ) {
            return FileshareMessages.get(FileshareMessages.PEER_SHARED_TO_TITLE);
        }

        return null;
    }


    /**
     * 
     * @param e
     * @param recursive
     * @return whether the entity is shared
     */
    public boolean isShared ( VFSEntity e, boolean recursive ) {
        if ( e == null ) {
            return false;
        }
        if ( !e.hasParent() ) {
            return false;
        }
        else if ( !recursive ) {
            return e.hasLocalValidGrants();
        }
        else if ( e.hasGrants() ) {
            return true;
        }

        try {
            return this.fsp.getShareService().getGrantCount(e.getEntityKey()) > 0;
        }
        catch ( FileshareException ex ) {
            ExceptionHandler.handleException(ex);
            return false;
        }
    }


    /**
     * 
     * @param n
     * @return the icon class to apply when collapsed
     */
    public static String getCollapsedEntityIconClass ( BrowseTreeNode n ) {

        if ( FileTreeConstants.USER_ROOT_TYPE.equals(n.getType()) ) {
            return USER_ROOT_ICON;
        }

        if ( FileTreeConstants.GROUP_ROOT_TYPE.equals(n.getType()) ) {
            return GROUP_ROOT_ICON;
        }

        if ( n instanceof EntityTreeNode ) {
            return getTypeIconCollapsed( ( (EntityTreeNode) n ).getAttachedObject());

        }
        return n.getIcon();
    }


    /**
     * 
     * @param n
     * @return the icon class to apply when expanded
     */
    public static String getExpandedEntityIconClass ( BrowseTreeNode n ) {

        if ( FileTreeConstants.USER_ROOT_TYPE.equals(n.getType()) ) {
            return USER_ROOT_ICON;
        }

        if ( FileTreeConstants.GROUP_ROOT_TYPE.equals(n.getType()) ) {
            return GROUP_ROOT_ICON;
        }

        if ( n instanceof EntityTreeNode ) {
            return getTypeIconExpanded( ( (EntityTreeNode) n ).getAttachedObject());

        }
        return n.getIcon();
    }


    /**
     * 
     * @param n
     * @return the state dependend icon class to apply
     */
    public static String getEntityIconClass ( BrowseTreeNode n ) {
        if ( n.isExpanded() ) {
            return getExpandedEntityIconClass(n);
        }

        return getCollapsedEntityIconClass(n);
    }


    /**
     * 
     * @param n
     * @return the style class to apply for drag and drop integration
     */
    public static String getEntityDropClass ( BrowseTreeNode n ) {
        if ( n instanceof GroupTreeNode || n instanceof SharedTreeNode ) {
            return NO_SELECT_CLASS;
        }
        return DROP_TARGET_CLASS;
    }


    /**
     * 
     * @param n
     * @return whether the given node should be collapsible
     */
    public static String isCollabsible ( BrowseTreeNode n ) {
        return Boolean.TRUE.toString();
    }


    /**
     * 
     * @param n
     * @return style class to mark hidden element message
     */
    public String getHiddenStyleClass ( BrowseTreeNode n ) {
        if ( ! ( n instanceof AbstractBrowseTreeNode ) ) {
            return StringUtils.EMPTY;
        }

        AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
        int totalHidden = bn.getHiddenTotal();

        if ( totalHidden == 0 ) {
            return "no-hidden"; //$NON-NLS-1$
        }

        if ( bn.getHiddenPolicy() > 0 ) {
            return "policy-hidden ui-state-error"; //$NON-NLS-1$
        }

        return "flagged-hidden ui-state-error"; //$NON-NLS-1$
    }


    /**
     * 
     * @param n
     * @return a short message describing the hidden elements
     */
    public String getHiddenMessage ( BrowseTreeNode n ) {
        if ( ! ( n instanceof AbstractBrowseTreeNode ) ) {
            return StringUtils.EMPTY;
        }

        AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
        int totalHidden = bn.getHiddenTotal();

        if ( totalHidden == 0 ) {
            return StringUtils.EMPTY;
        }

        if ( bn.getHiddenFlagged() == 0 && bn.getHiddenFilter() == 0 ) {
            return FileshareMessages.format("hidden.num_restrictedFmt", totalHidden); //$NON-NLS-1$
        }

        return FileshareMessages.format("hidden.num_hiddenFmt", totalHidden); //$NON-NLS-1$
    }


    /**
     * 
     * @param n
     * @return a long message describing the hidden elements
     */
    public String getHiddenDetailMessage ( BrowseTreeNode n ) {
        if ( ! ( n instanceof AbstractBrowseTreeNode ) ) {
            return StringUtils.EMPTY;
        }

        AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
        int totalHidden = bn.getHiddenTotal();

        if ( totalHidden == 0 ) {
            return StringUtils.EMPTY;
        }

        if ( bn.getHiddenFlagged() == 0 && bn.getHiddenFilter() == 0 ) {
            return FileshareMessages.format("hidden.num_restrictedLongFmt", bn.getHiddenPolicy()); //$NON-NLS-1$
        }
        else if ( bn.getHiddenPolicy() == 0 ) {
            return FileshareMessages.format("hidden.num_hiddenLongFmt", bn.getHiddenFlagged()); //$NON-NLS-1$
        }

        return FileshareMessages.format("hidden.num_restrictedBothLongFmty", bn.getHiddenFlagged(), bn.getHiddenPolicy()); //$NON-NLS-1$

    }
}
